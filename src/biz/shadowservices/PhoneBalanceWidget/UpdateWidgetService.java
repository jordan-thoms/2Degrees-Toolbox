package biz.shadowservices.PhoneBalanceWidget;



import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UpdateWidgetService extends Service implements Runnable {
	private static String TAG = "UpdateWidgetService";
    /**
     * Flag if there is an update thread already running. We only launch a new
     * thread if one isn't already running.
     */
    private static boolean isThreadRunning = false;
    private static Object lock = new Object();

    public class LocalBinder extends Binder {
        UpdateWidgetService getService() {
            return UpdateWidgetService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    public void onStart(Intent intent, int startId) {
    	Log.d(TAG, "Starting service");
    	synchronized (lock) {
    		if(!isThreadRunning) {
    	    	Log.d(TAG, "Thread not running, starting.");
    			isThreadRunning = true;
    			new Thread(this).start();
    		} else {
    	    	Log.d(TAG, "Thread already running, not doing anything.");
    		}

    	}
    }
	@Override
	public void run() {
		//Build update
    	Log.d(TAG, "Building updates");
        RemoteViews updateViews = PhoneBalanceWidget.buildUpdate(this);
        Log.d(TAG, updateViews.toString());
        //Push update to home screen
    	Log.d(TAG, "Pushing updates");
        ComponentName thisWidget = new ComponentName(this, PhoneBalanceWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, updateViews);
    	Log.d(TAG, "Sent updates");
    	isThreadRunning = false;
    	stopSelf();
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	

}
