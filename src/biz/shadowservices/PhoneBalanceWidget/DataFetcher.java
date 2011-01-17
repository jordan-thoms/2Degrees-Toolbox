package biz.shadowservices.PhoneBalanceWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataFetcher {
	public double result;
	private static String TAG = "DataFetchThread";
	public void updateData(Context context) throws DataFetcherLoginDetailsException {
		// Open database
		PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
    	// Find the URL of the page to send login data to.
		Log.d(TAG, "Finding Action ");
    	HttpGetter loginPageGet = new HttpGetter("https://secure.2degreesmobile.co.nz/web/ip/login");
    	Document loginPage = Jsoup.parse(loginPageGet.execute(), "https://secure.2degreesmobile.co.nz/web/ip/login");
    	Element loginForm = loginPage.getElementsByAttributeValue("name", "loginFrm").first();
    	String loginAction = loginForm.attr("action");
    	// Send login form
    	List<NameValuePair> loginValues = new ArrayList <NameValuePair>();
    	loginValues.add(new BasicNameValuePair("externalURLRedirect", ""));
    	loginValues.add(new BasicNameValuePair("hdnAction", "login"));
    	loginValues.add(new BasicNameValuePair("hdnAuthenticationType", "M"));
    	loginValues.add(new BasicNameValuePair("hdnlocale", ""));
    	
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);
        if(username != null && password != null) {
    		loginValues.add(new BasicNameValuePair("userid", username));
    		loginValues.add(new BasicNameValuePair("password", password));
        } else {
    		throw new DataFetcherLoginDetailsException("Username or password not set.");
        }
		Log.d(TAG, "Sending Login ");
    	HttpPoster sendLoginPoster = new HttpPoster(loginAction, loginValues);
    	// Parse result

	    	Document homePage = Jsoup.parse(sendLoginPoster.execute());
	    	Element accountSummary = homePage.getElementById("accountSummary");
	    	Element accountSummaryTable = accountSummary.getElementsByClass("tableBillSummary").first();
	    	Elements rows = accountSummaryTable.getElementsByTag("tr");
	    	db.delete("cache", "", null);
	    	for (Element row : rows) {
	    		Log.d(TAG, "Starting row");
	    		Log.d(TAG, row.html());
	    		Double value;
	    		String units;
	    		try {
	    			Element amount = row.getElementsByClass("tableBillamount").first();
		    		String amountHTML = amount.html();
		    		Log.d(TAG, amountHTML);
		    		String[] amountParts = amountHTML.split("&nbsp;", 2);
		    		Log.d(TAG, amountParts[0]);
		    		Log.d(TAG, amountParts[1]);
		    		value = Double.parseDouble(amountParts[0]);
		    		units = amountParts[1];
	    		} catch (NullPointerException e) {
	    			Log.d(TAG, "Failed to parse amount from row");
	    			value = null;
		    		units = null;
	    		}
	    		Element details = row.getElementsByClass("tableBilldetail").first();
	    		String name = details.getElementsByTag("strong").first().text();
	    		/* String expiresDetails = details.getElementsByTag("em").first().text();
	    		Pattern pattern = Pattern.compile("\\((\\d*) expiring on (.*)\\)");
	    		Matcher matcher = new Matcher(expiresDetails);
	    		String expires
	    		if (matcher.find()) {
	    			
	    		} */
	    		ContentValues values = new ContentValues();
	    		values.put("name", name);
	    		values.put("value", value);
	    		values.put("units", units);
	    		db.insert("cache", "value", values );
	    	}
    	db.close();
	}
}
class DataFetcherLoginDetailsException extends Exception {
	public DataFetcherLoginDetailsException(String error) {
		super(error);
	}
}