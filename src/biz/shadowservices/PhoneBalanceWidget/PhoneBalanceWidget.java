package biz.shadowservices.PhoneBalanceWidget;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Element;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class PhoneBalanceWidget extends AppWidgetProvider {
	private static String TAG = "PhoneBalanceWidget";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateWidgetService.class));
    }
    static RemoteViews buildUpdate(Context context) {
    	Boolean loginFailed = false;
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
    	DataFetcher dataFetcher = new DataFetcher();
    	try {
			dataFetcher.updateData(context);
	    	Log.d(TAG, "Building updates -- data updated");
			PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(context);
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			Cursor result = db.query("cache", new String[] {"value", "units"} , null, null,null,null,null);
			try {
				result.moveToFirst();
				int[] lines = new int[] { R.id.line1, R.id.line2, R.id.line3, R.id.line4 };
				int limit = 3;
				int pos = 0;
				for (int i=0; i<result.getCount(); i++) {
					if (i > limit) {
						break;
					}
					if (result.getString(0) != null) {
						if (result.getString(1) != null) {
							updateViews.setTextViewText(lines[pos], result.getString(0) + " " + result.getString(1));
							Log.d(TAG, result.getString(0) + " " + result.getString(1));
						} else {
							updateViews.setTextViewText(lines[pos], result.getString(0));
						}
						pos++;
					} else {
						limit++;
					}
					result.moveToNext();
				}
			} finally {
		        result.close();
		        db.close();
			}
		} catch (DataFetcherLoginDetailsException e) {
			 Log.d(TAG, e.getMessage());
	         updateViews.setTextViewText(R.id.line1, e.getMessage());
	         loginFailed = true;
		} catch (ClientProtocolException e) {
			 Log.d(TAG, e.getMessage());
	         updateViews.setTextViewText(R.id.line4, "Network Error"); 
		} catch (IOException e) {
			 Log.d(TAG, e.getMessage());
	         updateViews.setTextViewText(R.id.line4, "Network Error"); 
		} 
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 
		String updateDateString = sp.getString("updateDate", "");
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
		Intent viewIntent;
		if(loginFailed) {
	         viewIntent = new Intent(context, BalancePreferencesActivity.class);
		} else {
			 viewIntent = new Intent(context, PhoneBalanceMain.class);
		}
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget, pending);

        return updateViews;
    }

}
