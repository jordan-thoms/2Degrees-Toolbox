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



import java.io.IOException;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import de.quist.app.errorreporter.ReportingService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateWidgetService extends ReportingService implements Runnable {
	// This is the service which handles updating the widgets.
	private static String TAG = "2DegreesUpdateWidgetService";
	public static String NEWDATA = "BalanceWidgetNewDataAvailable12";
	public static final int NONE = 0;
	public static final int USERNAMEPASSWORD = 1;
	public static final int LOGINFAILED = 2;
	public static final int NETWORK = 3;
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
    	Values.widgetUpdaters.add(new WidgetUpdater1x2());
    	Values.widgetUpdaters.add(new WidgetUpdater2x2());    	
    }
	 // This is the old onStart method that will be called on the pre-2.0
	 // platform.  On 2.0 or later we override onStartCommand() so this
	 // method will not be called.
	 @Override
	 public void onStart(Intent intent, int startId) {
	     handleCommand(intent);
	 }
    public int onStartCommand(Intent intent, int startId) {
    	handleCommand(intent);
    	return START_NOT_STICKY;
    }
    private void handleCommand(Intent intent) {
    	Log.d(TAG, "Starting service");
    	if (intent != null) {
    		force = intent.getBooleanExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", false);
    	}
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
		for (AbstractWidgetUpdater updater : Values.widgetUpdaters) {
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
    	DataFetcher dataFetcher = new DataFetcher(getExceptionReporter());
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

		for (AbstractWidgetUpdater updater : Values.widgetUpdaters) {
			updater.updateWidgets(this, force, error);
		}

    	Log.d(TAG, "Sent updates");
    	Intent myIntent = new Intent(NEWDATA);
    	sendBroadcast(myIntent);
    	// We now dispatch to GA.
        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
    	tracker.dispatch();
    	isThreadRunning = false;
    	// Stop the service. A lot of apps leave their widget update services running, which is completely unnecessary!
    	stopSelf();
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	

}
