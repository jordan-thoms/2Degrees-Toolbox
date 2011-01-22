package biz.shadowservices.PhoneBalanceWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataFetcher {
	public double result;
	private static String TAG = "DataFetchThread";
	public boolean isOnline(Context context) {
		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo info = cm.getActiveNetworkInfo();
		 if (info == null) {
			 return false;
		 } else {
			 return info.isConnectedOrConnecting();
		 }
	}

	public void updateData(Context context) throws DataFetcherLoginDetailsException, ClientProtocolException, IOException {
		// check for internet connectivity
		if (!isOnline(context)) {
			Log.d(TAG, "We do not seem to be online. Not updating.");
			return;
		}
		//Open database
		PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 
			String username = sp.getString("username", null);
			String password = sp.getString("password", null);
			if(username == null || password == null) {
				throw new DataFetcherLoginDetailsException("Username or password not set.");
			}

			// Find the URL of the page to send login data to.
			Log.d(TAG, "Finding Action. ");
			HttpGetter loginPageGet = new HttpGetter("https://secure.2degreesmobile.co.nz/web/ip/login");
			String loginPageString = loginPageGet.execute();
			if (loginPageString != null) {
				Document loginPage = Jsoup.parse(loginPageString, "https://secure.2degreesmobile.co.nz/web/ip/login");
				Element loginForm = loginPage.getElementsByAttributeValue("name", "loginFrm").first();
				String loginAction = loginForm.attr("action");
				// Send login form
				List<NameValuePair> loginValues = new ArrayList <NameValuePair>();
				loginValues.add(new BasicNameValuePair("externalURLRedirect", ""));
				loginValues.add(new BasicNameValuePair("hdnAction", "login"));
				loginValues.add(new BasicNameValuePair("hdnAuthenticationType", "M"));
				loginValues.add(new BasicNameValuePair("hdnlocale", ""));
	
				loginValues.add(new BasicNameValuePair("userid", username));
				loginValues.add(new BasicNameValuePair("password", password));
				Log.d(TAG, "Sending Login ");
				HttpPoster sendLoginPoster = new HttpPoster(loginAction, loginValues);
				// Parse result
	
				Document homePage = Jsoup.parse(sendLoginPoster.execute());
				
				Element accountSummary = homePage.getElementById("accountSummary");
				if (accountSummary == null) {
					throw new DataFetcherLoginDetailsException("Login Failed.");
				}
				Element accountSummaryTable = accountSummary.getElementsByClass("tableBillSummary").first();
				Elements rows = accountSummaryTable.getElementsByTag("tr");
				db.delete("cache", "", null);
				for (Element row : rows) {
					//Log.d(TAG, "Starting row");
					//Log.d(TAG, row.html());
					Double value;
					String units;
					try {
						Element amount = row.getElementsByClass("tableBillamount").first();
						String amountHTML = amount.html();
						//Log.d(TAG, amountHTML);
						String[] amountParts = amountHTML.split("&nbsp;", 2);
						//Log.d(TAG, amountParts[0]);
						//Log.d(TAG, amountParts[1]);
						value = Double.parseDouble(amountParts[0]);
						units = amountParts[1];
					} catch (NullPointerException e) {
						//Log.d(TAG, "Failed to parse amount from row.");
						value = null;
						units = null;
					}
					Element details = row.getElementsByClass("tableBilldetail").first();
					String name = details.getElementsByTag("strong").first().text();
					String expiresDetails = details.getElementsByTag("em").first().text();
					Log.d(TAG, expiresDetails);
	
					Pattern pattern = Pattern.compile("\\(([\\d\\.]*) ?\\w*? ?expiring on (.*)\\)");
					Matcher matcher = pattern.matcher(expiresDetails);
					Double expiresValue = null;
					String expiresDate = null;
					if (matcher.find()) {
						//Log.d(TAG, "matched expires");
						//Log.d(TAG, "group 0:" + matcher.group(0));
						//Log.d(TAG, "group 1:" + matcher.group(1));
						//Log.d(TAG, "group 2:" + matcher.group(2));
						try {
							expiresValue = Double.parseDouble(matcher.group(1));
						} catch (NumberFormatException e) {
							expiresValue = null;
						}
						String expiresDateString = matcher.group(2);
						Date expiresDateObj;
						if (expiresDateString != null) {
							if (expiresDateString.length() > 0) {
								try {
									expiresDateObj = DateFormatters.EXPIRESDATE.parse(expiresDateString);
									expiresDate = DateFormatters.ISO8601DATEONLYFORMAT.format(expiresDateObj);
								} catch (java.text.ParseException e) {
									Log.d(TAG, "Could not parse date: " + expiresDateString);
								}
							}	
						}
					}
					ContentValues values = new ContentValues();
					values.put("name", name);
					values.put("value", value);
					values.put("units", units);
					values.put("expires_value", expiresValue);
					values.put("expires_date", expiresDate);
					db.insert("cache", "value", values );
					SharedPreferences.Editor prefedit = sp.edit();
					Date now = new Date();
					prefedit.putString("updateDate", DateFormatters.ISO8601FORMAT.format(now));
					prefedit.commit();
				}
			}
		} finally {
			db.close();
		}
	}
}
class DataFetcherLoginDetailsException extends Exception {
	public DataFetcherLoginDetailsException(String error) {
		super(error);
	}
}