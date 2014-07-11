/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package biz.shadowservices.DegreesToolbox.net;

import java.util.Date;
import java.util.List;

import biz.shadowservices.DegreesToolbox.util.DBLog;
import biz.shadowservices.DegreesToolbox.util.DateFormatters;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

interface TwoDegreesService {
    @FormUrlEncoded
    @POST("/login")
    LoginResponse login(@Field("phonenumber") String phoneNumber, @Field("password") String password,
                        @Field("operation") String operation);

    class LoginResponse {
        Boolean success;
        @SerializedName("phonenumber")
        String phoneNumber;
        @SerializedName("authtoken")
        String authToken;
    }


    @FormUrlEncoded
    @POST("/getBalance")
    BalanceResponse getBalance(@Field("phonenumber") String phoneNumber, @Field("authtoken") String authToken,
                               @Field("operation") String operation);

    class BalanceResponse {
        Boolean success;

        Data values;

        class Data {
            @SerializedName("General Cash")
            String generalCash;

            @SerializedName("ExpiryDate")
            String cashExpiryDate;

            @SerializedName("ExpiryValue")
            String cashExpiryValue;

            @SerializedName("widget_mins")
            String widgetMins;

            @SerializedName("widget_data")
            String widgetData;

            @SerializedName("widget_text")
            String widgetTexts;
        }

        class Tag {
            @SerializedName("tagname")
            String tagName;

            @SerializedName("tagvalue")
            String tagValue;
        }

        @SerializedName("allminutes")
        List<Tag> minutes;

        @SerializedName("alltexts")
        List<Tag> texts;

        @SerializedName("alldata")
        List<Tag> data;
    }

}

public class DataFetcher {
    // This class handles the actual fetching of the data from 2Degrees.
    public double result;
    public static final String LASTMONTHCHARGES = "Your last month's charges";
    private static String TAG = "2DegreesDataFetcher";
    public enum FetchResult {
        SUCCESS,
        NOTONLINE,
        LOGINFAILED,
        USERNAMEPASSWORDNOTSET,
        NETWORKERROR,
        NOTALLOWED
    }
    public DataFetcher() {
    }
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            return info.isConnected();
        }
    }
    public boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info == null) {
            return false;
        } else {
            return info.isConnected();
        }
    }
    public boolean isRoaming(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (info == null) {
            return false;
        } else {
            return info.isRoaming();
        }
    }
    public boolean isAutoSyncEnabled() {
        return ContentResolver.getMasterSyncAutomatically();
    }

    public FetchResult updateData(Context context, boolean force) {
        // check for internet connectivity
        try {
            if (!isOnline(context)) {
                Log.d(TAG, "We do not seem to be online. Skipping Update.");
                return FetchResult.NOTONLINE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (!force) {
            FetchResult result = shouldUpdate(context, sp);
            if (result != null) {
                return result;
            }
        } else {
            Log.d(TAG, "Update Forced");
        }

        try {
            String username = sp.getString("username", null);
            String password = sp.getString("password", null);
            if(username == null || password == null) {
                DBLog.insertMessage(context, "i", TAG, "Username or password not set.");
                return FetchResult.USERNAMEPASSWORDNOTSET;
            }

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://mobile.2degreesmobile.co.nz/index.php")
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();

            TwoDegreesService service = restAdapter.create(TwoDegreesService.class);
            TwoDegreesService.LoginResponse response = service.login(username, password, "login");


            TwoDegreesService.BalanceResponse balances = service.getBalance(response.phoneNumber, response.authToken, "getbalance");



            SharedPreferences.Editor prefedit = sp.edit();
            Date now = new Date();
            prefedit.putString("updateDate", DateFormatters.ISO8601FORMAT.format(now));
            prefedit.putBoolean("loginFailed", false);
            prefedit.putBoolean("networkError", false);
            prefedit.commit();
            DBLog.insertMessage(context, "i", TAG, "Update Successful");
            return FetchResult.SUCCESS;
        } catch (RetrofitError e) {
            DBLog.insertMessage(context, "w", TAG, "Network error: " + e.getMessage());
            return FetchResult.NETWORKERROR;
        }
    }

    public FetchResult shouldUpdate(Context context, SharedPreferences sp) {
        try {
            if (sp.getBoolean("loginFailed", false) == true) {
                Log.d(TAG, "Previous login failed. Skipping Update.");
                DBLog.insertMessage(context, "i", TAG, "Previous login failed. Skipping Update.");
                return FetchResult.LOGINFAILED;
            }
            if (sp.getBoolean("autoupdates", true) == false) {
                Log.d(TAG, "Automatic updates not enabled. Skipping Update.");
                DBLog.insertMessage(context, "i", TAG, "Automatic updates not enabled. Skipping Update.");
                return FetchResult.NOTALLOWED;
            }
            if (!isAutoSyncEnabled() && sp.getBoolean("obeyAutoSync", true) && sp.getBoolean("obeyBackgroundData", true)) {
                Log.d(TAG, "Auto sync not enabled. Skipping Update.");
                DBLog.insertMessage(context, "i", TAG, "Auto sync not enabled. Skipping Update.");
                return FetchResult.NOTALLOWED;
            }
            if (isWifi(context) && !sp.getBoolean("wifiUpdates", true)) {
                Log.d(TAG, "On wifi, and wifi auto updates not allowed. Skipping Update");
                DBLog.insertMessage(context, "i", TAG, "On wifi, and wifi auto updates not allowed. Skipping Update");
                return FetchResult.NOTALLOWED;
            } else if (!isWifi(context)) {
                Log.d(TAG, "We are not on wifi.");
                if (!isRoaming(context) && !sp.getBoolean("2DData", true)) {
                    Log.d(TAG, "Automatic updates on 2Degrees data not enabled. Skipping Update.");
                    DBLog.insertMessage(context, "i", TAG, "Automatic updates on 2Degrees data not enabled. Skipping Update.");
                    return FetchResult.NOTALLOWED;
                } else if (isRoaming(context) && !sp.getBoolean("roamingData", false)) {
                    Log.d(TAG, "Automatic updates on roaming mobile data not enabled. Skipping Update.");
                    DBLog.insertMessage(context, "i", TAG, "Automatic updates on roaming mobile data not enabled. Skipping Update.");
                    return FetchResult.NOTALLOWED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
