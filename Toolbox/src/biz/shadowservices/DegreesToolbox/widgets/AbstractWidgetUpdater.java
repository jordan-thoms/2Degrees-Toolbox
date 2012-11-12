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
package biz.shadowservices.DegreesToolbox.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biz.shadowservices.DegreesToolbox.data.DataFetcher.FetchResult;
import biz.shadowservices.DegreesToolbox.util.DateFormatters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public abstract class AbstractWidgetUpdater {
	// This contains the common code for updating all of the different widget sizes.
	private static String TAG = "2DegreesAbstractWidgetUpdater";
    RemoteViews buildUpdate(Context context, int widgetId, boolean force, FetchResult error) {
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), getLayoutId());
    	fillRemoteViews(updateViews, context, widgetId, error);

        return updateViews;
    }
    protected abstract String getFriendlyName();
    protected abstract void  fillRemoteViews(RemoteViews updateViews, Context context, int widgetId, FetchResult error);
    protected abstract int getLayoutId();
    public String getUpdateDateString(Context context) {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	if (sp.getBoolean("last_update_shown", true)) {
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
    	} else {
    		return "";
    	}
    }
    public List<WidgetInstance> getWidgets(Context context) {
    	ArrayList<WidgetInstance> widgets = new ArrayList<WidgetInstance>();
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = getComponentName(context);
	    int [] widgetIds = manager.getAppWidgetIds(provider);
	    for (int widget : widgetIds) {
	    	widgets.add(new WidgetInstance(widget, getFriendlyName()));
		}
	    return widgets;
    }
    protected abstract ComponentName getComponentName(Context context);
    
    public void updateWidgets(Context context, boolean force, FetchResult error) {
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
