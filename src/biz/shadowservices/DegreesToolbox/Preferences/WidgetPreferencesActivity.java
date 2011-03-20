package biz.shadowservices.DegreesToolbox.Preferences;

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.WidgetInstance;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class WidgetPreferencesActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_preferences);
	}
    @Override
	public void onResume() {
    	super.onResume();
		WidgetInstance widget = (WidgetInstance) getIntent().getSerializableExtra("widget");
		TextView widgetName = (TextView) findViewById(R.id.WidgetName);
		widgetName.setText(widget.toString());
	}
}
