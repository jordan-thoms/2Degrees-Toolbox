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
package biz.shadowservices.DegreesToolbox.widget;



import java.util.Date;

import biz.shadowservices.DegreesToolbox.net.DataFetcher;
import biz.shadowservices.DegreesToolbox.net.DataFetcher.FetchResult;

import biz.shadowservices.DegreesToolbox.util.DateFormatters;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateWidgetService extends IntentService {
	// This is the service which handles updating the widgets.
	private static String TAG = "2DegreesUpdateWidgetService";
	public static String NEWDATA = "BalanceWidgetNewDataAvailable12";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateWidgetService() {
        super("UpdateWidgetService");
    }

    protected void onHandleIntent(Intent intent) {
    	Log.d(TAG, "Starting service");
        boolean force = intent.getBooleanExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", false);

        //Build update
        WidgetUpdater updater = new WidgetUpdater();
        Log.d(TAG, "Building updates");
        updater.widgetLoading(this);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String updateDateString = sp.getString("updateDate", "");
        boolean update = true;
        if (!force) {
            try {
                Date now = new Date();
                Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
                long diff = now.getTime() - lastUpdate.getTime();
                long mins = diff / (1000 * 60);
                if (mins < Integer.parseInt(sp.getString("freshTime", "30"))) {
                    update = false;
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed when deciding whether to update");
            }
        }
        DataFetcher dataFetcher = new DataFetcher();
        FetchResult result = null;
        if(update) {
            result = dataFetcher.updateData(this, force);
            // Login failed - set error for the activity so it can display the information
            Editor edit = sp.edit();
            edit.putString("updateStatus", result.toString());
            edit.commit();
            Log.d(TAG, "Building updates -- data updated. Result: " +  result.toString());
        } else {
            Log.d(TAG, "Building updates -- data fresh, not updated");
            result = FetchResult.SUCCESS;
        }

        updater.updateWidgets(this, force, result);
        Log.d(TAG, "Sent updates");
        Intent myIntent = new Intent(NEWDATA);
        sendBroadcast(myIntent);
        // We now dispatch to GA.
        // Wrap up in a catch all since this has been having problems
        try {
//    			GATracker.getInstance(getApplication()).incrementActivityCount();
//    			GATracker.getInstance().dispatch();
//    			GATracker.getInstance().decrementActivityCount();
        } catch (Exception e) {
//            getExceptionReporter().reportException(Thread.currentThread(), e, "GA Tracking in updateWidgetService");
        }
    }

}
