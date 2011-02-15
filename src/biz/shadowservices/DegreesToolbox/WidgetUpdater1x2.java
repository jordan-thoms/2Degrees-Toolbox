package biz.shadowservices.DegreesToolbox;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetUpdater1x2 extends AbstractWidgetUpdater {
	private static int LINELIMIT = 17;
	private static String TAG = "2DegreesPhoneBalanceWidget2x1";
	@Override
	protected void fillRemoteViews(RemoteViews updateViews, Context context, int widgetId) {
		List<String> lines = buildLines(context);
		if (lines.size() > 0) {
			updateViews.setTextViewText(R.id.widget1x2_line1, lines.get(0));
			if (lines.size() > 1) {
				updateViews.setTextViewText(R.id.widget1x2_line2, lines.get(1));
				if(lines.size() > 2) {
					updateViews.setTextViewText(R.id.widget1x2_line3, lines.get(2));
					for (int i = 3; i < lines.size(); i++) {
						if((lines.get(1) + lines.get(3)).length() < LINELIMIT) {
							updateViews.setTextViewText(R.id.widget1x2_right1, lines.get(i));
							lines.remove(i);
						}
					}
					for (int i = 3; i < lines.size(); i++) {
						if((lines.get(2) + lines.get(3)).length() < LINELIMIT) {
							updateViews.setTextViewText(R.id.widget1x2_right2, lines.get(i));
						}
					}
				}
			}
		}
		updateViews.setTextViewText(R.id.widget1x2_lastupdate, getUpdateDateString(context));
		Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget1x2_widget, pending);

	}
	@Override
	protected int getLayoutId() {
		return R.layout.balance_widget_1x2;
	}
    private List<String> buildLines(Context context) {
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor result = db.query("cache", new String[] {"value", "units"} , null, null,null,null,null);
		List<String> lines = new ArrayList<String>();
		try {
			result.moveToFirst();
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
		} finally {
	        result.close();
	        db.close();
		}
		return lines;
    }

}
