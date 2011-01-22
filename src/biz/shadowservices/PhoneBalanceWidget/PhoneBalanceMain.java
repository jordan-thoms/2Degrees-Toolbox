package biz.shadowservices.PhoneBalanceWidget;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class PhoneBalanceMain extends Activity {
	private static String TAG = "PhoneBalanceMain";
	private static final int MSG_FINISHED = 1;
	ProgressDialog progressDialog;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button configureButton = (Button) findViewById(R.id.configurebutton);
        configureButton.setOnClickListener(configureButtonListener);
        Button refreshButton = (Button) findViewById(R.id.refreshbutton);
        refreshButton.setOnClickListener(refreshButtonListener);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	refreshData();
    }
    private void refreshData() {
        // Load, display data.
		PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("cache", new String[] {"name","value", "units", "expires_value", "expires_date"} , null, null,null,null,null);
		cursor.moveToFirst(); 
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.lineslayout);
		layout.removeAllViews();
		for (int i=0; i<cursor.getCount(); i++) {
			RelativeLayout firstLine = new RelativeLayout(this);
			TextView col1 = new TextView(this);
			TextView col3 = new TextView(this);
			RelativeLayout.LayoutParams col3LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			col3LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			RelativeLayout.LayoutParams col1LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			col1LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//col1LayoutParams.addRule(RelativeLayout.LEFT_OF, col3.getId() );
			col1.setText(cursor.getString(0));
			firstLine.addView(col1,  col1LayoutParams);
			StringBuilder valueStringBuilder = new StringBuilder();
			if(cursor.getString(1) != null) {
				valueStringBuilder.append(cursor.getString(1));
				valueStringBuilder.append(" ");
			}
			if(cursor.getString(2) != null) {
				valueStringBuilder.append(cursor.getString(2));
			}
			col3.setText(valueStringBuilder.toString());
			firstLine.addView(col3, col3LayoutParams);
			layout.addView(firstLine);
			TextView col2 = new TextView(this);
			SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
			Date expiryDate;
			String expiresInfo = "";
			try {
				String date = cursor.getString(4);
				if (date != null) {
					if(date.length() > 0) {
						expiryDate = iso.parse(date);
						SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
						String expiryDateString = output.format(expiryDate);
						expiresInfo = cursor.getString(3);
						if (expiresInfo != null ) {
							if (expiresInfo.length() > 0) {
								expiresInfo += " " + cursor.getString(2) + " expiring on " + expiryDateString ;
							} else {
								expiresInfo = " expiring on " + expiryDateString ;
							}
						} else {
							expiresInfo = " expiring on " + expiryDateString ;
						}
					}
				}	
			} catch (ParseException e) {
				Log.e(TAG, "Could not parse date from DB.");
			}
			col2.setText(expiresInfo);
			layout.addView(col2);
			cursor.moveToNext(); 
		}
		cursor.close();
		db.close();
    }
    private OnClickListener configureButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		Intent openPreferences = new Intent(v.getContext(), BalancePreferencesActivity.class);
    		startActivity(openPreferences);
    	}
    };
    private OnClickListener refreshButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		progressDialog = ProgressDialog.show( v.getContext(), " " , " Loading. Please wait ... ", true);
    		progressDialog.show();
    		UpdateThread thread = new UpdateThread(v.getContext());
    		thread.start();
    	}
    };
    private class UpdateThread extends Thread {
    	private Context context;
    	public UpdateThread(Context c) {
    		context = c;
    	}
    	public void run() {
    		Log.d(TAG, "Refreshing content");
        	Log.d(TAG, "Building updates");
            RemoteViews updateViews = PhoneBalanceWidget.buildUpdate(context);
            Log.d(TAG, updateViews.toString());
            //Push update to home screen
        	Log.d(TAG, "Pushing updates");
            ComponentName thisWidget = new ComponentName(context, PhoneBalanceWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, updateViews);
        	Log.d(TAG, "Sent updates");
        	handler.sendEmptyMessage(MSG_FINISHED);
    	}
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FINISHED:
            		progressDialog.dismiss();
                    // What to do when ready, example:
                    refreshData();
                    break;
                }
            }
        };

}