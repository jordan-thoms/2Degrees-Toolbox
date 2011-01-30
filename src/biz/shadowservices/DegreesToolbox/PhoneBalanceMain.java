package biz.shadowservices.DegreesToolbox;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import biz.shadowservices.DegreesToolbox.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Toast;

public class PhoneBalanceMain extends Activity {
	private static String TAG = "PhoneBalanceMain";
	private UpdateReciever reciever;
	private final int TXTPACK = 0;
	private final int NATDATA = 1;
	private final int BBZDATA = 2;
	private final int TALKPACK = 3;	
	private final CharSequence[] SMSCategory = {"$10 2000 Texts", "National Data", "Zone Data", "Talk Packs"};
	private final CharSequence[] SMSNATNames = {"$6 50MB National Data"};
	private final String[] SMSNATContent = {"BUY 50MB"};
	private final CharSequence[] SMSBBNames = {"$20 1GB BB Data", "$50 3GB BB Data", "$150 12GB BB Data"};
	private final String[] SMSBBContent = {"BUY 1GB", "BUY 3GB", "BUY 12GB"};
	private final CharSequence[] SMSTalkNames = {"$30 Everyone100", "$10 China120", "$10 India120"};
	private final String[] SMSTalkContent = {"buy every100", "buy china120", "buy india120"};

	ProgressDialog progressDialog = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button buyPackButton = (Button) findViewById(R.id.buyPackButton);
        buyPackButton.setOnClickListener(buyPackListener);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.forceUpdate:
    		progressDialog = ProgressDialog.show(this, " " , " Loading. Please wait ... ", true);
    		progressDialog.show();
    		Intent update = new Intent(this, UpdateWidgetService.class);
    		update.putExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", true);
    		startService(update);
    		return true;
    	case R.id.openPreferences:
    		Intent openPreferences = new Intent(this, BalancePreferencesActivity.class);
    		startActivity(openPreferences);
    		return true;
    	case R.id.callCustomerService:
    		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800022022")));
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    @Override
    public void onResume() {
    	super.onResume();
    	// Check if username/password is not set.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); 
		String username = sp.getString("username", "");
		String password = sp.getString("password", "");
		if (username.equals("") || password.equals("")) {
			// Username/password is not set, launch setup wizard
			Toast.makeText(this, "Username or password empty", 3);
			startActivityForResult(new Intent(this, SetupWizard.class), 1);
		}
    	refreshData();
    	reciever = new UpdateReciever(this);
    	registerReceiver(reciever, new IntentFilter(UpdateWidgetService.NEWDATA));
    }
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == 1) {
    		progressDialog = ProgressDialog.show(this, " " , " Loading. Please wait ... ", true);
    		progressDialog.show();
    		Intent update = new Intent(this, UpdateWidgetService.class);
    		update.putExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", true);
    		startService(update);
    	}
    }
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterReceiver(reciever);
    	if(progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
    private OnClickListener buyPackListener = new OnClickListener() {
    	public void onClick(View v) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
    		builder.setTitle("Choose a value pack to buy:");
    		builder.setItems(SMSCategory, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		    	switch(item) {
    		    	case TXTPACK:
    		    		askToSend(SMSCategory[TXTPACK], "buy 10txt");
    		    		break;
    		    	case NATDATA:
    		    		chooseSMSToSend(SMSNATNames, SMSNATContent);
    		    		break;
    		    	case BBZDATA:
    		    		chooseSMSToSend(SMSBBNames, SMSBBContent);
    		    		break;
    		    	case TALKPACK:
    		    		chooseSMSToSend(SMSTalkNames, SMSTalkContent);
    		    	}
    		    }
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    };
    private void chooseSMSToSend(final CharSequence[] names, final String[] content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose a value pack to buy:");
		builder.setItems(names, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, final int item) {
		    	askToSend(names[item], content[item]);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    public void askToSend(final CharSequence name, final String content) {
    	AlertDialog.Builder confirmDialog = new AlertDialog.Builder(PhoneBalanceMain.this);
    	confirmDialog.setMessage("Are you sure you wish to purchase " + name + " by sending '" + content + "' to 233?")
    		.setCancelable(false)
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					send233SMS(content);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {						
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
    	AlertDialog confirm = confirmDialog.create();
    	confirm.show();

    }
    public void refreshData() {
        // Load, display data.
		PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("cache", new String[] {"name","value", "units", "expires_value", "expires_date"} , null, null,null,null,null);
		cursor.moveToFirst(); 
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.lineslayout);
		layout.removeAllViews();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		{ 
			RelativeLayout firstLine = new RelativeLayout(this);
			firstLine.setPadding(1,5,5,2);
			TextView col1 = new TextView(this);
			col1.setTypeface(Typeface.DEFAULT_BOLD);
			TextView col3 = new TextView(this);
			col3.setTypeface(Typeface.DEFAULT_BOLD);
			RelativeLayout.LayoutParams col3LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			col3LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			RelativeLayout.LayoutParams col1LayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			col1LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//col1LayoutParams.addRule(RelativeLayout.LEFT_OF, col3.getId() );
			if(!sp.getBoolean("loginFailed", false)) {
				if (!sp.getBoolean("networkError", false)) {
					col1.setText("Last Update");
					String updateDateString = sp.getString("updateDate", "");
					String updateDate = "";
					try {
						Date lastUpdate = DateFormatters.ISO8601FORMAT.parse(updateDateString);
						updateDate = DateFormatters.DATETIME.format(lastUpdate);
					} catch (Exception e) {
						updateDate = "Unknown";
					}
					col3.setText(updateDate);
				} else {
					col1.setText("Last Update -- Network Error.");				
				}

			} else {
				col1.setText("Last Update -- Login failed, set the correct details in menu->preferences and then go menu->force update.");				
			}
				
			firstLine.addView(col1,  col1LayoutParams);
			firstLine.addView(col3, col3LayoutParams);
			layout.addView(firstLine);
		}
		for (int i=0; i<cursor.getCount(); i++) {
			RelativeLayout firstLine = new RelativeLayout(this);
			firstLine.setPadding(0,5,5,2);
			TextView col1 = new TextView(this);
			col1.setTypeface(Typeface.DEFAULT_BOLD);
			TextView col3 = new TextView(this);
			col3.setTypeface(Typeface.DEFAULT_BOLD);
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
			col2.setPadding(10,0,5,5);
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
       
    public class UpdateReciever extends BroadcastReceiver {

        private PhoneBalanceMain activity;

        public UpdateReciever(PhoneBalanceMain activity) {
            this.activity = activity;
        }

        public void onReceive(Context context, Intent intent) {
            if(activity != null) {
            	if(activity.progressDialog != null) {
            		activity.progressDialog.dismiss();
            		activity.progressDialog = null;
            	}
            	activity.refreshData();
            }

        }
    }
    private void send233SMS(String message)
    {        
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, PhoneBalanceMain.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("233", null, message, pi, null);
    	Log.d(TAG, "sent message: " + message);
    }    

}