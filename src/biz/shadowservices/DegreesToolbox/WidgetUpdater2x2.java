package biz.shadowservices.DegreesToolbox;

import java.util.ArrayList;
import java.util.Date;
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
	protected void fillRemoteViews(RemoteViews updateViews, Context context, int widgetId, int error) {

    	switch (error) {
    	case UpdateWidgetService.NONE:
    	case UpdateWidgetService.NETWORK:
    		List<Line> lines = buildLines(context);
    		int[] lineViews = { R.id.widget2x2_line2, R.id.widget2x2_line3, R.id.widget2x2_line4, R.id.widget2x2_line5, R.id.widget2x2_line6,  R.id.widget2x2_line7,  R.id.widget2x2_line8,  R.id.widget2x2_line9 };
    		Log.d(TAG, Integer.toString(lines.size()));
    		for (int n = 0; n < lineViews.length; n++) {
    			if(n >= lines.size()) {
    				break;
    			}
    			updateViews.setTextViewText(lineViews[n], lines.get(n).getLineContent());
    			if(lines.get(n).getSize() != -1) {
    				updateViews.setFloat(lineViews[n], "setTextSize", lines.get(n).getSize());
    			} else {
    				updateViews.setFloat(lineViews[n], "setTextSize", Util.dpToPx(context, 9));
    			}
    		}
    		updateViews.setTextViewText(R.id.widget2x2_line1, getUpdateDateString(context));
    		break;
    	case UpdateWidgetService.LOGINFAILED:
			updateViews.setTextViewText(R.id.widget2x2_line1, "Login failed");
			break;
    	case UpdateWidgetService.USERNAMEPASSWORD:
			updateViews.setTextViewText(R.id.widget2x2_line1, "Username or password not set");
			break;
    	}
		Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget2x2_widget, pending);
	}
	@Override
	protected int getLayoutId() {
		return R.layout.balance_widget_2x2;
	}
    private List<Line> buildLines(Context context) {
    	int expSize = Util.dpToPx(context, 7);
		DBOpenHelper dbhelper = new DBOpenHelper(context);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		Cursor result = db.query("cache", new String[] {"name", "value", "units", "expires_date"} , null, null,null,null,null);
		
		List<Line> lines = new ArrayList<Line>();
		try {
			result.moveToFirst();
			for (int i=0; i<result.getCount(); i++) {
				AbbreviationMapping abbrMapping = new AbbreviationMapping();
				StringBuilder lineBuilder = new StringBuilder();
				String abbr = abbrMapping.getAbbr(result.getString(0));
				lineBuilder.append(abbr);
				if (abbr.length() > 0 && result.getString(1) != null) {
					lineBuilder.append(": ");
				}
				if (result.getString(1) != null) {
					if (result.getString(2) != null) {
						if (!result.getString(2).equals("$NZ")) {
							lineBuilder.append(Math.round(result.getDouble(1)) + " " + result.getString(2));
						} else {
							lineBuilder.append("$" + result.getString(1));
						}
						Log.d(TAG, result.getString(1) + " " + result.getString(2));
					} else {
						lineBuilder.append(String.valueOf(Math.round(result.getDouble(1))));
					}
				}
				lines.add(new Line(lineBuilder.toString()));
				try {
					if (result.getString(3).length() > 0)  {
						Date expiryDate = DateFormatters.ISO8601DATEONLYFORMAT.parse(result.getString(3));
						String output = DateFormatters.SHORTDATE.format(expiryDate);
						Line expLine = new Line("  exp:" + output);
						expLine.setSize(expSize);
						lines.add(expLine);
					}
				} catch (Exception e) {
					
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
