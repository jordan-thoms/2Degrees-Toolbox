package biz.shadowservices.DegreesToolbox;

import biz.shadowservices.DegreesToolbox.TitleBar.ActivityHelper;
import de.quist.app.errorreporter.ReportingActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends ReportingActivity {
	/* Mostly from google IO app */
    final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Need to do this for every activity that uses google analytics
        GATracker.getInstance(getApplication()).incrementActivityCount();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Example of how to track a pageview event
        GATracker.getInstance().trackPageView(getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Purge analytics so they don't hold references to this activity
//        Tracker.getInstance().dispatch();

        // Need to do this for every activity that uses google analytics
        GATracker.getInstance().decrementActivityCount();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActivityHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mActivityHelper.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActivityHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Returns the {@link ActivityHelper} object associated with this activity.
     */
    protected ActivityHelper getActivityHelper() {
        return mActivityHelper;
    }

}
