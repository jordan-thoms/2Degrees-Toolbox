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
package biz.shadowservices.DegreesToolbox;

import java.io.IOException;
import java.lang.reflect.Method;
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

import de.quist.app.errorreporter.ExceptionReporter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataFetcher {
	// This class handles the actual fetching of the data from 2Degrees.
	public double result;
	public static final String LASTMONTHCHARGES = "Your last month's charges";
	private static String TAG = "2DegreesDataFetcher";
	private ExceptionReporter exceptionReporter;
	public enum FetchResult {
		SUCCESS,
		NOTONLINE,
		LOGINFAILED,
		USERNAMEPASSWORDNOTSET,
		NETWORKERROR,
		NOTALLOWED
	}
	public DataFetcher(ExceptionReporter e) {
		exceptionReporter = e;
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
	public boolean isBackgroundDataEnabled(Context context) {
		ConnectivityManager mgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return(mgr.getBackgroundDataSetting());
	}
	public boolean isAutoSyncEnabled() {
		// Get the autosync setting, if on a phone which has one.
		// There are better ways of doing this than reflection, but it's easy in this case
		// since then we can keep linking against the 1.6 SDK.
		if (android.os.Build.VERSION.SDK_INT >= 5) {
			Class<ContentResolver> contentResolverClass = ContentResolver.class;
			try {
				Method m = contentResolverClass.getMethod("getMasterSyncAutomatically", null);
				Log.d(TAG, m.toString());
				Log.d(TAG, m.invoke(null, null).toString());
				boolean bool = ((Boolean)m.invoke(null, null)).booleanValue();
				return bool;
			} catch (Exception e) {
				Log.d(TAG, "could not determine if autosync is enabled, assuming yes");
				return true;
			}
		} else {
			return true;
		}
	}
	public FetchResult updateData(Context context, boolean force) {
		//Open database
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();

		// check for internet connectivity
		try {
			if (!isOnline(context)) {
				Log.d(TAG, "We do not seem to be online. Skipping Update.");
				return FetchResult.NOTONLINE;
			}
		} catch (Exception e) {
			exceptionReporter.reportException(Thread.currentThread(), e, "Exception during isOnline()");
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if (!force) {
			try {
				if (sp.getBoolean("loginFailed", false) == true) {
					Log.d(TAG, "Previous login failed. Skipping Update.");
					DBLog.insertMessage(context, "i", TAG, "Previous login failed. Skipping Update.");
					return FetchResult.LOGINFAILED;
				}
				if(sp.getBoolean("autoupdates", true) == false) {
					Log.d(TAG, "Automatic updates not enabled. Skipping Update.");
					DBLog.insertMessage(context, "i", TAG, "Automatic updates not enabled. Skipping Update.");
					return FetchResult.NOTALLOWED;
				}
				if (!isBackgroundDataEnabled(context) && sp.getBoolean("obeyBackgroundData", true)) {
					Log.d(TAG, "Background data not enabled. Skipping Update.");
					DBLog.insertMessage(context, "i", TAG, "Background data not enabled. Skipping Update.");
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
				} else if (!isWifi(context)){
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
				exceptionReporter.reportException(Thread.currentThread(), e, "Exception while finding if to update.");
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
					return FetchResult.LOGINFAILED;
				}
				db.delete("cache", "", null);
				/* This code fetched some extra details for postpaid users, but on reflection they aren't that useful.
				 * Might reconsider this.
				 *
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
						String expiresDetails = "";
						String expiresDate = null;
						String name = null;
						try {
							Element details = row.getElementsByClass("tableBilldetail").first();
							name = details.ownText();
							Element expires = details.getElementsByTag("em").first();
							if (expires != null) {
								 expiresDetails = expires.text();
							} 
							Log.d(TAG, expiresDetails);
							Pattern pattern;
							pattern = Pattern.compile("\\(payment is due (.*)\\)");
							Matcher matcher = pattern.matcher(expiresDetails);
							if (matcher.find()) {
								/*Log.d(TAG, "matched expires");
								Log.d(TAG, "group 0:" + matcher.group(0));
								Log.d(TAG, "group 1:" + matcher.group(1));
								Log.d(TAG, "group 2:" + matcher.group(2)); *
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
						} catch (Exception e) {
							Log.d(TAG, "Failed to parse details from row.");
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
				} */
				Element accountSummaryTable = accountSummary.getElementsByClass("tableAccountSummary").first();
				Elements rows = accountSummaryTable.getElementsByTag("tr");
				for (Element row : rows) {
					// We are now looking at each of the rows in the data table.
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
						if (amountParts[0].contains("Included") ||  amountParts[0].equals("All You Need")) {
							value = Values.INCLUDED;
						} else {
							try {
								value = Double.parseDouble(amountParts[0]);	
							} catch (NumberFormatException e) {
								exceptionReporter.reportException(Thread.currentThread(), e, "Decoding value.");
								value = 0.0;
							}
						}
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
				}
				
				if(postPaid == false) {
					Log.d(TAG, "Getting Value packs...");
					// Find value packs
					HttpGetter valuePacksPageGet = new HttpGetter("https://secure.2degreesmobile.co.nz/group/ip/prevaluepack");
					String valuePacksPageString = valuePacksPageGet.execute();
					//DBLog.insertMessage(context, "d", "",  valuePacksPageString);
					if(valuePacksPageString != null) {
						Document valuePacksPage = Jsoup.parse(valuePacksPageString);
						Elements enabledPacks = valuePacksPage.getElementsByClass("yellow");
						for (Element enabledPack : enabledPacks) {
							Element offerNameElemt = enabledPack.getElementsByAttributeValueStarting("name", "offername").first();
							String offerName = offerNameElemt.val();
							DBLog.insertMessage(context, "d", "", "Got element: " + offerName);
							ValuePack[] packs = Values.valuePacks.get(offerName);
							if (packs == null) {
								DBLog.insertMessage(context, "d", "", "Offer name: " + offerName + " not matched.");
							} else {
								for (ValuePack pack: packs) {
								//	Cursor csr = db.query("cache", null, "name = '" + pack.type.id + "'", null, null, null, null);
							//		if (csr.getCount() == 1) {
						//				csr.moveToFirst();
										ContentValues values = new ContentValues();
										// Not sure why adding on the previous value?
										//values.put("plan_startamount", csr.getDouble(4) + pack.value);
										//DBLog.insertMessage(context, "d", "", "Pack " + pack.type.id + " start value set to " + csr.getDouble(4) + pack.value);
										values.put("plan_startamount", pack.value);
										values.put("plan_name", offerName);
										DBLog.insertMessage(context, "d", "", "Pack " + pack.type.id + " start value set to " + pack.value);
										db.update("cache", values, "name = '" + pack.type.id + "'", null);
							//		} else {
							//			DBLog.insertMessage(context, "d", "", "Pack " + pack.type.id + " Couldn't find item to add to");
							//		}
								//	csr.close();
								}
							}
						}
					}
				}

				
				SharedPreferences.Editor prefedit = sp.edit();
				Date now = new Date();
				prefedit.putString("updateDate", DateFormatters.ISO8601FORMAT.format(now));
				prefedit.putBoolean("loginFailed", false);
		        prefedit.putBoolean("networkError", false);
				prefedit.commit();
				DBLog.insertMessage(context, "i", TAG, "Update Successful");
				return FetchResult.SUCCESS;

			}
		} catch (ClientProtocolException e) {
			DBLog.insertMessage(context, "w", TAG, "Network error: " + e.getMessage());
			return FetchResult.NETWORKERROR;
		} catch (IOException e) {
			DBLog.insertMessage(context, "w", TAG, "Network error: " + e.getMessage());
			return FetchResult.NETWORKERROR;
		}
		finally {
			db.close();
		}
		return null;
	}
}
