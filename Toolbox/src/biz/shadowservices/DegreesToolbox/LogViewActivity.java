package biz.shadowservices.DegreesToolbox;

import biz.shadowservices.DegreesToolbox.util.StackTraceUtil;
import de.quist.app.errorreporter.ExceptionReporter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LogViewActivity extends BaseActivity {
	private static String TAG = "2DegreesPhoneBalanceMainActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view);
        Button clearLogButton = (Button) findViewById(R.id.clear_log_button);
        clearLogButton.setOnClickListener(clearLog);
        Button sendLogButton = (Button) findViewById(R.id.send_log_button);
        sendLogButton.setOnClickListener(sendLog);
        
	}
    @Override
    public void onResume() {
    	super.onResume();
    	refreshLog();
    }
    
    private void refreshLog() {
    	// Load the log
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.logLinesLayout);
		layout.removeAllViews();
		DBOpenHelper dbHelper = new DBOpenHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor logLines = db.query("log", null, null, null, null, null, "id DESC");
		logLines.moveToFirst();
		for (int i=0; i<logLines.getCount(); i++) {
			StringBuilder text = new StringBuilder(logLines.getString(1).replace("T", " "));
			text.append("  ");
			text.append(logLines.getString(4));
			TextView textView = new TextView(this);
			textView.setText(text);
			layout.addView(textView);
			logLines.moveToNext();
		}
		logLines.close();
		db.close();
    }
    OnClickListener clearLog = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DBOpenHelper dbHelper = new DBOpenHelper(v.getContext());
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete("log", null, null);
			LogViewActivity.this.refreshLog();
			db.close();
		}
	};
	OnClickListener sendLog = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			DBOpenHelper dbHelper = new DBOpenHelper(v.getContext());
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor logLines = db.query("log", null, null, null, null, null, "id DESC");
			logLines.moveToFirst();
			StringBuilder messageBody = new StringBuilder();
			messageBody.append("Please write a message here describing your issue");
			messageBody.append("-----------------------------");
			PackageInfo pInfo;
			try {
				pInfo = v.getContext().getPackageManager().getPackageInfo(v.getContext().getPackageName(), PackageManager.GET_META_DATA);
				String versionInfo = pInfo.versionName;
				messageBody.append("Log from 2degrees toolbox:" + versionInfo + "\n");
			} catch (NameNotFoundException e) {
				GATracker.getInstance().trackEvent("Exceptions", e.getMessage() + "Name not found - onclick email log send", StackTraceUtil.getStackTrace(e), 0);
			}
			
			for (int i=0; i<logLines.getCount(); i++) {
				messageBody.append(logLines.getString(1).replace("T", " "));
				messageBody.append("  ");
				messageBody.append(logLines.getString(4));
				messageBody.append("\n");
			}
			
			// http://stackoverflow.com/questions/2197741/how-to-send-email-from-my-android-application/2197841#2197841
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"jordan@thoms.net.nz"});
			i.putExtra(Intent.EXTRA_SUBJECT, "Log From 2degrees toolbox");
			i.putExtra(Intent.EXTRA_TEXT   , messageBody.toString());
			try {
			    startActivity(Intent.createChooser(i, "Send mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
			    Toast.makeText(LogViewActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			}
			logLines.close();
			db.close();
		}
	};
}
