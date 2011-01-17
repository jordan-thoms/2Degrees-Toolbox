package biz.shadowservices.PhoneBalanceWidget;

import org.jsoup.nodes.Element;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    	Log.d(TAG, "Building updates");
    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
    	DataFetcher dataFetcher = new DataFetcher();
    	try {
			dataFetcher.updateData(context);
			PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(context);
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			Cursor result = db.query("cache", new String[] {"value"} , "name = 'Prepay Credit'", null,null,null,null);
			result.moveToFirst();
			Double balance = result.getDouble(0);
	    	Log.d(TAG, balance.toString());
	        updateViews.setTextViewText(R.id.widgettext, balance.toString()); 
		} catch (DataFetcherLoginDetailsException e) {
	        updateViews.setTextViewText(R.id.widgettext, e.getMessage()); 
		}
    	//
        /* Random generator = new Random();
        int tmp = generator.nextInt();
        */
        Intent viewMainIntent = new Intent(context, PhoneBalanceMain.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, viewMainIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget, pending);

        return updateViews;
    }

}
