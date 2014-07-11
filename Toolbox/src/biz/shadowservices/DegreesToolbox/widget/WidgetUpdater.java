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
package biz.shadowservices.DegreesToolbox.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biz.shadowservices.DegreesToolbox.util.DBOpenHelper;
import biz.shadowservices.DegreesToolbox.net.DataFetcher.FetchResult;
import biz.shadowservices.DegreesToolbox.util.DateFormatters;
import biz.shadowservices.DegreesToolbox.util.Line;
import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.util.Util;
import biz.shadowservices.DegreesToolbox.util.Values;
import biz.shadowservices.DegreesToolbox.activities.MainActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetUpdater {
    // This contains the common code for updating all of the different widget sizes.
    private static String TAG = WidgetUpdater.class.getSimpleName();

    RemoteViews buildUpdate(Context context, int widgetId, boolean force, FetchResult error) {
        Log.d(TAG, "Building updates");
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
        fillRemoteViews(updateViews, context, widgetId, error);

        return updateViews;
    }

    protected void fillRemoteViews(RemoteViews updateViews, Context context, int widgetId, FetchResult error) {
        Log.d(TAG, "FillRemoteViews, error code: " + error);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        switch (error) {
            case LOGINFAILED:
                updateViews.setTextViewText(R.id.balance_widget_status, "Login failed");
                break;
            case USERNAMEPASSWORDNOTSET:
                updateViews.setTextViewText(R.id.balance_widget_status, "Username or password not set");
                break;
            default:
                // Clear widget
                List<Line> lines = buildLines(context);
//
//                if (lines.size() > 0) {
//                    updateViews.setTextViewText(R.id.widget1x2_line1, lines.get(0).getLineContent());
//                    if (lines.size() > 1) {
//                        updateViews.setTextViewText(R.id.widget1x2_line2, lines.get(1).getLineContent());
//                        if(lines.size() > 2) {
//                            updateViews.setTextViewText(R.id.widget1x2_line3, lines.get(2).getLineContent());
//                            for (int i = 3; i < lines.size(); i++) {
//                                if((lines.get(1).getLineContent() + lines.get(3).getLineContent()).length() < LINELIMIT) {
//                                    updateViews.setTextViewText(R.id.widget1x2_right1, lines.get(i).getLineContent());
//                                    lines.remove(i);
//                                }
//                            }
//                            for (int i = 3; i < lines.size(); i++) {
//                                if((lines.get(2).getLineContent() + lines.get(3).getLineContent()).length() < LINELIMIT) {
//                                    updateViews.setTextViewText(R.id.widget1x2_right2, lines.get(i).getLineContent());
//                                }
//                            }
//                        }
//                    }
//                }
                break;
        }

//        updateViews.setTextViewText(R.id.widget_lastupdate, getUpdateDateString(context));
        Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        Intent updateIntent = new Intent(context, UpdateWidgetService.class);
        updateIntent.putExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", true);

//        updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, openAppPending);
//
//        updateViews.setOnClickPendingIntent(R.id.widget1x2_refreshButton, PendingIntent.getService(context, 0, updateIntent, 0));

    }

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

    protected ComponentName getComponentName(Context context) {
        return new ComponentName(context, PhoneBalanceWidget.class);
    }

    public void updateWidgets(Context context, boolean force, FetchResult error) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = getComponentName(context);
        int [] widgetIds = manager.getAppWidgetIds(provider);
        for (int widget : widgetIds) {
            RemoteViews updateViews = this.buildUpdate(context, widget, force, error);
            // Push update to home screen
            Log.d(TAG, "Pushing updates for widget");
            manager.updateAppWidget(widget, updateViews);
        }

    }
    RemoteViews buildLoadingUpdate(Context context) {
        Log.d(TAG, "Building updates");
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
        fillRemoteViewsLoading(updateViews, context);
        return updateViews;
    }

    protected void fillRemoteViewsLoading(RemoteViews updateViews,
                                          Context context) {
//        updateViews.setTextViewText(R.id.widget1x2_lastupdate, "Loading");
        Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
//        updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, pending);
    }

    public void widgetLoading(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = getComponentName(context);
        RemoteViews updateViews = this.buildLoadingUpdate(context);
        // Push update to home screen
        Log.d(TAG, "Pushing updates for widget");
        manager.updateAppWidget(provider, updateViews);

    }

}
