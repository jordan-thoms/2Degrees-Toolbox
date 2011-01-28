package biz.shadowservices.DegreesToolbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Element;

import biz.shadowservices.DegreesToolbox.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class PhoneBalanceWidget extends AppWidgetProvider {
	private static String TAG = "PhoneBalanceWidget";
	private static int LINELIMIT = 17;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateWidgetService.class));
    }
    static RemoteViews buildUpdate(Context context, boolean force) {
    	Boolean loginFailed = false;
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
    	DataFetcher dataFetcher = new DataFetcher();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 

		String updateDateString = sp.getString("updateDate", "");
		boolean update = true;
		if (!force) {
			try {
				Date now = new Date();
				Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
			    long diff = now.getTime() - lastUpdate.getTime();
			    long mins = diff / (1000 * 60);
			    if (mins < 60) {
			    	update = false;
			    }
			} catch (Exception e) {
				Log.d(TAG, "Failed when deciding whether to update");
			}
		}
		boolean failed = false;
		if(update) {
	    	try {
				dataFetcher.updateData(context, force);
			} catch (DataFetcherLoginDetailsException e) {
				 Log.d(TAG, e.getMessage());
		         updateViews.setTextViewText(R.id.line1, e.getMessage());
		         updateViews.setTextViewText(R.id.lastupdate, "");
		         loginFailed = true;
		         // Login failed - set the error text for the activity
		         Editor edit = sp.edit();
		         edit.putBoolean("loginFailed", true);
		         edit.commit();
		         failed = true;
			} catch (ClientProtocolException e) {
				 Log.d(TAG, e.getMessage());
		         updateViews.setTextViewText(R.id.line3, "Network Error");
		         updateViews.setTextViewText(R.id.right2, "");
		         Editor edit = sp.edit();
		         edit.putBoolean("networkError", true);
		         edit.commit();
		         failed = true;
			} catch (IOException e) {
				 Log.d(TAG, e.getMessage());
		         updateViews.setTextViewText(R.id.line3, "Network Error");
		         updateViews.setTextViewText(R.id.right2, "");
		         Editor edit = sp.edit();
		         edit.putBoolean("networkError", true);
		         edit.commit();

		         failed = true;
			}
		    Log.d(TAG, "Building updates -- data updated");
		} else {
		    Log.d(TAG, "Building updates -- data fresh, not updated");
		}
		if (!failed) {
			PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(context);
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			Cursor result = db.query("cache", new String[] {"value", "units"} , null, null,null,null,null);
			try {
				result.moveToFirst();
				List<String> lines = new ArrayList<String>();
				for (int i=0; i<result.getCount(); i++) {
					if (result.getString(0) != null) {
						if (result.getString(1) != null) {
							if (!result.getString(1).equals("$NZ")) {
								lines.add(Math.round(result.getDouble(0)) + " " + result.getString(1));
							} else {
								lines.add("$" + result.getString(0));
							}
							Log.d(TAG, result.getString(0) + " " + result.getString(1));
						} else {
							lines.add(String.valueOf(Math.round(result.getDouble(0))));
						}
					}
					result.moveToNext();
				}
				if (lines.size() > 0) {
					updateViews.setTextViewText(R.id.line1, lines.get(0));
					if (lines.size() > 1) {
						updateViews.setTextViewText(R.id.line2, lines.get(1));
						if(lines.size() > 2) {
							updateViews.setTextViewText(R.id.line3, lines.get(2));
							for (int i = 3; i < lines.size(); i++) {
								if((lines.get(1) + lines.get(3)).length() < LINELIMIT) {
									updateViews.setTextViewText(R.id.right1, lines.get(i));
									lines.remove(i);
								}
							}
							for (int i = 3; i < lines.size(); i++) {
								if((lines.get(2) + lines.get(3)).length() < LINELIMIT) {
									updateViews.setTextViewText(R.id.right2, lines.get(i));
								}
							}
						}
					}
				}
	
			} finally {
			        result.close();
			        db.close();
			}
		}
		if (!loginFailed) {
			updateDateString = sp.getString("updateDate", "");
			try {
				Date now = new Date();
				Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
				if (DateFormatters.isSameDay(now, lastUpdate)) {
					updateViews.setTextViewText(R.id.lastupdate, DateFormatters.LASTUPDATETIME.format(lastUpdate)); 
				} else {
					updateViews.setTextViewText(R.id.lastupdate, DateFormatters.LASTUPDATEDATE.format(lastUpdate)); 				
				}
			} catch (Exception e) {
				 Log.d(TAG, e.getMessage());
		         updateViews.setTextViewText(R.id.lastupdate, "E"); 
			}
		}
		Intent viewIntent;
	//	if(loginFailed) {
	  //       viewIntent = new Intent(context, BalancePreferencesActivity.class);
	//	} else {
			 viewIntent = new Intent(context, PhoneBalanceMain.class);
	//	}
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget, pending);

        return updateViews;
    }

}
