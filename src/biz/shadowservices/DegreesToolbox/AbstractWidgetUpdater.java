package biz.shadowservices.DegreesToolbox;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public abstract class AbstractWidgetUpdater {
	private static String TAG = "2DegreesAbstractWidgetUpdater";
    RemoteViews buildUpdate(Context context, int widgetId, boolean force, int error) {
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), getLayoutId());
    	fillRemoteViews(updateViews, context, widgetId);

        return updateViews;
    }
    protected abstract void  fillRemoteViews(RemoteViews updateViews, Context context, int widgetId);
    protected abstract int getLayoutId();
    public String getUpdateDateString(Context context) {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String updateDateString = sp.getString("updateDate", "");
    	try {
			Date now = new Date();
			Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
			if (DateFormatters.isSameDay(now, lastUpdate)) {
				return DateFormatters.LASTUPDATETIME.format(lastUpdate); 
			} else {
				return DateFormatters.LASTUPDATEDATE.format(lastUpdate); 				
			}
		} catch (Exception e) {
			 Log.d(TAG, e.getMessage());
			 return "";
		}
    }

}
