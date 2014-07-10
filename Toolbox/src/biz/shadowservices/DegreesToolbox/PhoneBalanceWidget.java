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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PhoneBalanceWidget extends AppWidgetProvider {
	private static String TAG = "2DegreesAppWidgetProvider";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateWidgetService.class));
    }
    public void onEnabled(Context context) {
    	setAlarm(context);
    }
    static public void setAlarm(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		long interval;
		String stringInterval = sp.getString("updateFreq", "half_day");
		if (stringInterval.equals("hour")) {
			Log.d(TAG, "Hourly updates");
			interval = AlarmManager.INTERVAL_HOUR;
		} else if (stringInterval.equals("half_day")) {
			Log.d(TAG, "Half day updates");
			interval = AlarmManager.INTERVAL_HALF_DAY;
		} else if (stringInterval.equals("day")){
			Log.d(TAG, "daily updates");
			interval = AlarmManager.INTERVAL_DAY;
		} else {
			Log.d(TAG, "other interval??? - using half day");
			interval = AlarmManager.INTERVAL_HALF_DAY;
		}
    	Intent intent = new Intent("biz.shadowservices.DegreesToolbox.WIDGET_UPDATE");
    	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    	AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(System.currentTimeMillis());
    	calendar.add(Calendar.MINUTE, 10);
    	Log.d(TAG, "Setting alarm");
    	alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), interval, pendingIntent);
    }
    public void onDisabled(Context context) {
    	// We need to check if there are any widgets still on the screen.
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        boolean cancelAlarm = true;

        ComponentName provider = new ComponentName(context, PhoneBalanceWidget.class);
	    if(manager.getAppWidgetIds(provider).length > 0) {
	    	cancelAlarm = false;
	    }
	    if (cancelAlarm) {
	    	Intent intent = new Intent("biz.shadowservices.DegreesToolbox.WIDGET_UPDATE");
	    	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	    	AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(pendingIntent);
	    	Log.d(TAG, "Cancelled alarm");
	    } else {
	    	Log.d(TAG, "Alarm not canceled");
	    }
    }
}
