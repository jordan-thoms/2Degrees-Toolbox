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

public class WidgetUpdater2x2 extends AbstractWidgetUpdater {
	private static int LINELIMIT = 16;
	private static String TAG = "2DegreesPhoneBalanceWidget2x2";
	@Override
	protected void fillRemoteViews(RemoteViews updateViews, Context context, int widgetId) {
		List<String> lines = buildLines(context);
		int[] lineViews = { R.id.widget2x2_line2, R.id.widget2x2_line3, R.id.widget2x2_line4, R.id.widget2x2_line5, R.id.widget2x2_line6,  R.id.widget2x2_line7,  R.id.widget2x2_line8,  R.id.widget2x2_line9 };
		int lineNo = 0;
		for (String line : lines) {
			if (lineNo >= lineViews.length) {
				break;
			}
			updateViews.setTextViewText(lineViews[lineNo], line);
			lineNo++;
		}
		updateViews.setTextViewText(R.id.widget2x2_line1, getUpdateDateString(context));
		Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget2x2_widget, pending);
	}
	@Override
	protected int getLayoutId() {
		return R.layout.balance_widget_2x2;
	}
    private List<String> buildLines(Context context) {
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor result = db.query("cache", new String[] {"name", "value", "units"} , null, null,null,null,null);
		/* Map<String, String> stringMapping = new HashMap<String, String>();
		stringMapping.put("Prepay Credit", "");
		stringMapping.put("Your spend since last bill", ""); */
		
		List<String> lines = new ArrayList<String>();
		try {
			result.moveToFirst();
			for (int i=0; i<result.getCount(); i++) {
				if (result.getString(0) != null) {
					if (result.getString(1) != null) {
						if (!result.getString(2).equals("$NZ")) {
							lines.add(Math.round(result.getDouble(1)) + " " + result.getString(2));
						} else {
							lines.add("$" + result.getString(1));
						}
						Log.d(TAG, result.getString(1) + " " + result.getString(2));
					} else {
						lines.add(String.valueOf(Math.round(result.getDouble(1))));
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
