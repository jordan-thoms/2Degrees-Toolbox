package biz.shadowservices.DegreesToolbox.activities;

import biz.shadowservices.DegreesToolbox.util.GATracker;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.quist.app.errorreporter.ReportingActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends SherlockFragmentActivity {
    
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

}
