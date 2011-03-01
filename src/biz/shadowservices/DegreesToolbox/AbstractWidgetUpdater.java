package biz.shadowservices.DegreesToolbox;

import java.util.Date;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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
    	fillRemoteViews(updateViews, context, widgetId, error);

        return updateViews;
    }
    protected abstract void  fillRemoteViews(RemoteViews updateViews, Context context, int widgetId, int error);
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
    protected abstract ComponentName getComponentName(Context context);
    public void updateWidgets(Context context, boolean force, int error) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = getComponentName(context);
	    int [] widgetIds = manager.getAppWidgetIds(provider);
	    for (int widget : widgetIds) {
	        RemoteViews updateViews = this.buildUpdate(context, widget, force, error);
		     // Push update to home screen
		    Log.d(TAG, "Pushing updates for 2x1 widget");
		    manager.updateAppWidget(widget, updateViews);
		}

    }
    RemoteViews buildLoadingUpdate(Context context) {
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), getLayoutId());
    	fillRemoteViewsLoading(updateViews, context);
        return updateViews;
    }
    protected abstract void  fillRemoteViewsLoading(RemoteViews updateViews, Context context);
    public void widgetLoading(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = getComponentName(context);
        RemoteViews updateViews = this.buildLoadingUpdate(context);
		// Push update to home screen
		Log.d(TAG, "Pushing updates for 2x1 widget");
		manager.updateAppWidget(provider, updateViews);

    }

}
