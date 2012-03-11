package biz.shadowservices.DegreesToolbox;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GATracker {
	// Partially from http://stackoverflow.com/questions/3216692/google-analytics-in-android-app-dealing-with-multiple-activities
    private static GATracker INSTANCE;
    
    private int referenceCount = 0;
    private String apiKey;
    private Context context;
    private GoogleAnalyticsTracker tracker;
    private static final String TAG = "2DegreesTracker";

    /**
     * NOTE: you should use your Application context, not your Activity context, in order to avoid memory leaks.
     */
    private GATracker( String apiKey, Application context ) {
        this.apiKey = apiKey;
        this.context = context;
        this.tracker = GoogleAnalyticsTracker.getInstance();
    }


    public synchronized void incrementActivityCount() {
        if( referenceCount==0 ) {
        	GoogleAnalyticsTracker.getInstance().start(apiKey,context);
        	Log.d(TAG, "GA Tracker started");
        }

        ++referenceCount;
    }


    /**
     * This should be called once in onDestrkg() for each of your activities that use GoogleAnalytics.
     * These methods are not synchronized and don't generally need to be, so if you want to do anything
     * unusual you should synchronize them yourself.
     */
    public synchronized void decrementActivityCount() {
        referenceCount = Math.max(referenceCount-1, 0);

        if( referenceCount==0 ) {
            tracker.stop();
        	Log.d(TAG, "GA Tracker stopped.");
        }
    }

    public  synchronized void trackPageView(String url) {
    	this.tracker.trackPageView(url);
    }
    
    public  synchronized  void trackEvent(String category, String action, String label, int value ) {
    	this.tracker.trackEvent(category, action, label, value);
    }
    public synchronized void dispatch() {
    	this.tracker.dispatch();
    	Log.d(TAG, "GA Tracker dispatched.");
    }
    /**
     * Get or create an instance of GoogleAnalyticsSessionManager
     */
    public synchronized static GATracker getInstance( Application application ) {
        if( INSTANCE == null )
            INSTANCE = new GATracker( "UA-24340103-1" ,application);
        return INSTANCE;
    }

    /**
     * Only call this if you're sure an instance has been previously created using #getInstance(Application)
     */
    public synchronized static GATracker getInstance() {
        return INSTANCE;
    }
}
