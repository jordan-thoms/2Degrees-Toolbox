package biz.shadowservices.DegreesToolbox;

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
	public static final String LASTMONTHCHARGES = "Your last month's charges";
	private static String TAG = "2DegreesDataFetcher";
	public boolean isOnline(Context context) {
		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo info = cm.getActiveNetworkInfo();
		 if (info == null) {
			 return false;
		 } else {
			 return info.isConnectedOrConnecting();
		 }
	}
	public boolean isWifi(Context context) {
		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		 if (info == null) {
			 return false;
		 } else {
			 return info.isConnectedOrConnecting();
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

	public void updateData(Context context, boolean force) throws DataFetcherLoginDetailsException, ClientProtocolException, IOException {
		// check for internet connectivity
		if (!isOnline(context)) {
			Log.d(TAG, "We do not seem to be online. Not updating.");
			return;
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if (!force) {
			if (sp.getBoolean("loginFailed", false) == true) {
				Log.d(TAG, "Previous login failed. Not updating.");
				return;
			}
			if(sp.getBoolean("autoupdates", true) == false) {
				Log.d(TAG, "Automatic updates not enabled. Not updating.");
				return;
			}
			if (isWifi(context) && !sp.getBoolean("wifiUpdates", true)) {
				Log.d(TAG, "On wifi, and wifi auto updates not allowed. Not updating");
				return;
			} else if (!isWifi(context)){
				Log.d(TAG, "We are not on wifi.");
				if (!isRoaming(context) && !sp.getBoolean("2DData", true)) {
					Log.d(TAG, "Automatic updates on 2Degrees data not enabled. Not updating.");
					return;
				} else if (isRoaming(context) && !sp.getBoolean("roamingData", false)) {
						Log.d(TAG, "Automatic updates on roaming mobile data not enabled. Not updating.");
						return;
				}
				
			}
		} else {
			Log.d(TAG, "Update Forced");
		}
		//Open database
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			String username = sp.getString("username", null);
			String password = sp.getString("password", null);
			if(username == null || password == null) {
				throw new DataFetcherLoginDetailsException(DataFetcherLoginDetailsException.USERNAMEPASSWORDNOTSET, "Username/password not set");
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
				// Determine if this is a pre-pay or post-paid account.
				boolean postPaid;
				if (homePage.getElementById("p_p_id_PostPaidHomePage_WAR_Homepage_") == null) {
					Log.d(TAG, "Pre-pay account or no account.");
					postPaid = false;
				} else {
					Log.d(TAG, "Post-paid account.");
					postPaid = true;
				}
				
				Element accountSummary = homePage.getElementById("accountSummary");
				if (accountSummary == null) {
					Log.d(TAG, "Login failed.");
					throw new DataFetcherLoginDetailsException(DataFetcherLoginDetailsException.LOGINFAILED, "Login failed");
				}
				db.delete("cache", "", null);
				if (postPaid) {
					Element accountBalanceSummaryTable = accountSummary.getElementsByClass("tableBillSummary").first();
					Elements rows = accountBalanceSummaryTable.getElementsByTag("tr");
					int rowno = 0;
					for (Element row : rows) {
						if (rowno > 1) {
							break;
						}
						//Log.d(TAG, "Starting row");
						//Log.d(TAG, row.html());
						Double value;
						try {
							Element amount = row.getElementsByClass("tableBillamount").first();
							String amountHTML = amount.html();
							Log.d(TAG, amountHTML.substring(1));
							value = Double.parseDouble(amountHTML.substring(1));
						} catch (Exception e) {
							Log.d(TAG, "Failed to parse amount from row.");
							value = null;
						}
						Element details = row.getElementsByClass("tableBilldetail").first();
						String name = details.ownText();
						Element expires = details.getElementsByTag("em").first();
						String expiresDetails = "";
						if (expires != null) {
							 expiresDetails = expires.text();
						} 
						Log.d(TAG, expiresDetails);
						Pattern pattern;
						pattern = Pattern.compile("\\(payment is due (.*)\\)");
						Matcher matcher = pattern.matcher(expiresDetails);
						String expiresDate = null;
						if (matcher.find()) {
							/*Log.d(TAG, "matched expires");
							Log.d(TAG, "group 0:" + matcher.group(0));
							Log.d(TAG, "group 1:" + matcher.group(1));
							Log.d(TAG, "group 2:" + matcher.group(2)); */
							String expiresDateString = matcher.group(1);
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
						String expirev = null;
						ContentValues values = new ContentValues();
						values.put("name", name);
						values.put("value", value);
						values.put("units", "$NZ");
						values.put("expires_value", expirev );
						values.put("expires_date", expiresDate);
						db.insert("cache", "value", values );
						rowno++;
					}
				}
				Element accountSummaryTable = accountSummary.getElementsByClass("tableAccountSummary").first();
				Elements rows = accountSummaryTable.getElementsByTag("tr");
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
					Element expires = details.getElementsByTag("em").first();
					String expiresDetails = "";
					if (expires != null) {
						 expiresDetails = expires.text();
					} 
					Log.d(TAG, expiresDetails);
					Pattern pattern;
					if (postPaid == false) {
						pattern = Pattern.compile("\\(([\\d\\.]*) ?\\w*? ?expiring on (.*)\\)");
					} else {
						pattern = Pattern.compile("\\(([\\d\\.]*) ?\\w*? ?will expire on (.*)\\)");
					}
					Matcher matcher = pattern.matcher(expiresDetails);
					Double expiresValue = null;
					String expiresDate = null;
					if (matcher.find()) {
						/*Log.d(TAG, "matched expires");
						Log.d(TAG, "group 0:" + matcher.group(0));
						Log.d(TAG, "group 1:" + matcher.group(1));
						Log.d(TAG, "group 2:" + matcher.group(2)); */
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
					prefedit.putBoolean("loginFailed", false);
			        prefedit.putBoolean("networkError", false);
					prefedit.commit();
				}
			}
		} finally {
			db.close();
		}
	}
}
class DataFetcherLoginDetailsException extends Exception {
	private static final long serialVersionUID = 3744365132866903296L;
	private int errorType;
	public final static int USERNAMEPASSWORDNOTSET = 0;
	public final static int LOGINFAILED = 1;
	public DataFetcherLoginDetailsException(int errorType, String message) {
		super(message);
		this.errorType = errorType;
	}
	public int getErrorType() {
		return errorType;
	}
}