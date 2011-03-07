package biz.shadowservices.DegreesToolbox;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service implements Runnable {
	// This is the service which handles updating the widgets.
	private static String TAG = "2DegreesUpdateWidgetService";
	public static String NEWDATA = "BalanceWidgetNewDataAvailable12";
	public static final int NONE = 0;
	public static final int USERNAMEPASSWORD = 1;
	public static final int LOGINFAILED = 2;
	public static final int NETWORK = 3;
	// List of widget updaters to call.
	public static List<AbstractWidgetUpdater> widgetUpdaters = new ArrayList<AbstractWidgetUpdater>();
    /**
     * Flag if there is an update thread already running. We only launch a new
     * thread if one isn't already running.
     */
    private static boolean isThreadRunning = false;
    private static Object lock = new Object();
    private boolean force = false;
    public class LocalBinder extends Binder {
        UpdateWidgetService getService() {
            return UpdateWidgetService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
    static {
    	// Populate the list of widget updaters - in a static initaliser block since it only needs
    	// to happen once.
    	widgetUpdaters.add(new WidgetUpdater1x2());
    	widgetUpdaters.add(new WidgetUpdater2x2());    	
    }
    public void onStart(Intent intent, int startId) {
    	Log.d(TAG, "Starting service");
    	force = intent.getBooleanExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", false);
    	// Locking to make sure we only run one thread at a time.
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
		for (AbstractWidgetUpdater updater : widgetUpdaters) {
			updater.widgetLoading(this);
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); 
		String updateDateString = sp.getString("updateDate", "");
		boolean update = true;
		if (!force) {
			try {
				Date now = new Date();
				Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
			    long diff = now.getTime() - lastUpdate.getTime();
			    long mins = diff / (1000 * 60);
			    if (mins < Integer.parseInt(sp.getString("freshTime", "30"))) {
			    	update = false;
			    }
			} catch (Exception e) {
				Log.d(TAG, "Failed when deciding whether to update");
			}
		}
		int error =  NONE;
    	DataFetcher dataFetcher = new DataFetcher();
		if(update) {
	    	try {
				dataFetcher.updateData(this, force);
			} catch (DataFetcherLoginDetailsException e) {
				 Log.d(TAG, e.getMessage());
				 switch (e.getErrorType()) {
				 case DataFetcherLoginDetailsException.LOGINFAILED:
					 error = LOGINFAILED;
					 break;
				 case DataFetcherLoginDetailsException.USERNAMEPASSWORDNOTSET:
					 error = USERNAMEPASSWORD;
					 break;
				 }
		         // Login failed - set the error text for the activity
		         Editor edit = sp.edit();
		         edit.putBoolean("loginFailed", true);
		         edit.commit();
			} catch (ClientProtocolException e) {
				 Log.d(TAG, e.getMessage());
				 error = NETWORK;
		         Editor edit = sp.edit();
		         edit.putBoolean("networkError", true);
		         edit.commit();
			} catch (IOException e) {
				 Log.d(TAG, e.getMessage());
				 error = NETWORK;
		         Editor edit = sp.edit();
		         edit.putBoolean("networkError", true);
		         edit.commit();
			}
		    Log.d(TAG, "Building updates -- data updated");
		} else {
		    Log.d(TAG, "Building updates -- data fresh, not updated");
		}
		Log.d(TAG, Integer.toString(error));

		for (AbstractWidgetUpdater updater : widgetUpdaters) {
			updater.updateWidgets(this, force, error);
		}

    	Log.d(TAG, "Sent updates");
    	Intent myIntent = new Intent(NEWDATA);
    	sendBroadcast(myIntent);
    	isThreadRunning = false;
    	// Stop the service. A lot of apps leave their widget update services running, which is completely unnecessary!
    	stopSelf();
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	

}
