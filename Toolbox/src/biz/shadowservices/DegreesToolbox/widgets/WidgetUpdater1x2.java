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
import java.util.List;

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.R.id;
import biz.shadowservices.DegreesToolbox.R.layout;
import biz.shadowservices.DegreesToolbox.activities.MainActivity;
import biz.shadowservices.DegreesToolbox.data.DBOpenHelper;
import biz.shadowservices.DegreesToolbox.data.Line;
import biz.shadowservices.DegreesToolbox.data.Values;
import biz.shadowservices.DegreesToolbox.data.DataFetcher.FetchResult;
import biz.shadowservices.DegreesToolbox.service.UpdateWidgetService;
import biz.shadowservices.DegreesToolbox.util.Util;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetUpdater1x2 extends AbstractWidgetUpdater {
	// Handle updates for the 1x2 widget.
	private static int LINELIMIT = 17;
	private static String TAG = "2DegreesPhoneBalanceWidget2x1";
	@Override
	protected void fillRemoteViews(RemoteViews updateViews, Context context, int widgetId, FetchResult error) {
		Log.d(TAG, "FillRemoteViews, error code: " + error);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		int backgroundId = sp.getInt("widgetSettings[" + widgetId + "][backgroundId]", 0);


    	switch (error) {
    	case LOGINFAILED:
			updateViews.setTextViewText(R.id.widget1x2_line1, "Login failed");
			break;
    	case USERNAMEPASSWORDNOTSET:
			updateViews.setTextViewText(R.id.widget1x2_line1, "Username or password not set");
			break;
    	default:
    		// Clear widget
    		int[] lineIds = {R.id.widget1x2_line1, R.id.widget1x2_line2, R.id.widget1x2_line3, R.id.widget1x2_right1, R.id.widget1x2_right2 };
    		for (int line : lineIds) {
    			updateViews.setTextViewText(line, "");
    			updateViews.setInt(line, "setTextColor", sp.getInt("widgetSettings[" + widgetId + "][textColor]", 0xffffffff));
    		}
			updateViews.setInt(R.id.widget1x2_lastupdate, "setTextColor", sp.getInt("widgetSettings[" + widgetId + "][textColor]", 0xffffffff));
    		List<Line> lines = buildLines(context);
    		
    		if (lines.size() > 0) {
    			updateViews.setTextViewText(R.id.widget1x2_line1, lines.get(0).getLineContent());
    			if (lines.size() > 1) {
    				updateViews.setTextViewText(R.id.widget1x2_line2, lines.get(1).getLineContent());
    				if(lines.size() > 2) {
    					updateViews.setTextViewText(R.id.widget1x2_line3, lines.get(2).getLineContent());
    					for (int i = 3; i < lines.size(); i++) {
    						if((lines.get(1).getLineContent() + lines.get(3).getLineContent()).length() < LINELIMIT) {
    							updateViews.setTextViewText(R.id.widget1x2_right1, lines.get(i).getLineContent());
    							lines.remove(i);
    						}
    					}
    					for (int i = 3; i < lines.size(); i++) {
    						if((lines.get(2).getLineContent() + lines.get(3).getLineContent()).length() < LINELIMIT) {
    							updateViews.setTextViewText(R.id.widget1x2_right2, lines.get(i).getLineContent());
    						}
    					}
    				}
    			}
    		}
        	break;
    	}

		updateViews.setTextViewText(R.id.widget1x2_lastupdate, getUpdateDateString(context));
		Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        Intent updateIntent = new Intent(context, UpdateWidgetService.class);
        updateIntent.putExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", true);
        
        if (sp.getBoolean("press_widget_open_app", true)) {
            updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, openAppPending);
        } else {
            updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, PendingIntent.getService(context, 0, updateIntent, 0));
        }
        
        updateViews.setOnClickPendingIntent(R.id.widget1x2_refreshButton, PendingIntent.getService(context, 0, updateIntent, 0));

        if (!sp.getBoolean("show_refresh_button", true)) {
        	updateViews.setViewVisibility(R.id.widget1x2_refreshButton, View.GONE);
        }
	}
	@Override
	protected int getLayoutId() {
		return R.layout.balance_widget_1x2;
	}
    private List<Line> buildLines(Context context) {
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor result = db.query("cache", new String[] {"value", "units"} , null, null,null,null,null);
		List<Line> lines = new ArrayList<Line>();
		try {
			result.moveToFirst();
			for (int i=0; i<result.getCount(); i++) {
				if (result.getString(0) != null) {
					if (result.getInt(0) != 0) {
						if (result.getString(1) != null) {
							if (!result.getString(1).equals("$NZ")) {
								if(!(Math.abs((result.getDouble(0) - Values.INCLUDED)) < 0.001)) {
									lines.add(new Line(Math.round(result.getDouble(0)) + " " + result.getString(1)));
								}
							} else {
								if(result.getDouble(0) > 100) {
									lines.add(new Line("$" + Math.round(result.getDouble(0))));
								} else {
									lines.add(new Line(Util.money.format(result.getDouble(0))));
								}
							}
							Log.d(TAG, result.getString(0) + " " + result.getString(1));
						} else {
							lines.add(new Line(String.valueOf(Math.round(result.getDouble(0)))));
						}
					}
				}
				result.moveToNext();
			}
		} finally {
	        result.close();
	        db.close();
		}
		return lines;
    }
	@Override
	protected ComponentName getComponentName(Context context) {
		return new ComponentName(context, PhoneBalanceWidget.class);
	}
	
	@Override
	protected String getFriendlyName() {
		return "1x2 Widget";
	}
	@Override
	protected void fillRemoteViewsLoading(RemoteViews updateViews,
			Context context) {
		updateViews.setTextViewText(R.id.widget1x2_lastupdate, "Loading");
		Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, pending);
	}

}
