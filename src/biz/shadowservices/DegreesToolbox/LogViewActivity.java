package biz.shadowservices.DegreesToolbox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogViewActivity extends BaseActivity {
	private static String TAG = "2DegreesPhoneBalanceMainActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);
        getActivityHelper().setupActionBar("2Degrees Toolbox", 0);
	}
    @Override
    public void onResume() {
    	super.onResume();
    	// Track the view
    	GATracker.getInstance().trackPageView("/logView");
    	// Load the log
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.logLinesLayout);
		layout.removeAllViews();
		DBOpenHelper dbHelper = new DBOpenHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor logLines = db.query("log", null, null, null, null, null, "date_time DESC");
		for (int i=0; i<logLines.getCount(); i++) {
			StringBuilder text = new StringBuilder(logLines.getString(1));
			text.append("  ");
			text.append(logLines.getString(3));
			text.append("  ");
			text.append(logLines.getString(4));
			TextView textView = new TextView(this);
			textView.setText(text);
			layout.addView(textView);
			logLines.moveToNext();
		}
    }

}
