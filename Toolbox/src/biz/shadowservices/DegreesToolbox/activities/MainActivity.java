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
package biz.shadowservices.DegreesToolbox.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import biz.shadowservices.DegreesToolbox.util.DBOpenHelper;
import biz.shadowservices.DegreesToolbox.net.DataFetcher;
import biz.shadowservices.DegreesToolbox.net.DataFetcher.FetchResult;
import biz.shadowservices.DegreesToolbox.util.DateFormatters;
import biz.shadowservices.DegreesToolbox.util.PackTreeNode;
import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.widget.UpdateWidgetService;
import biz.shadowservices.DegreesToolbox.util.Values;
import biz.shadowservices.DegreesToolbox.preferences.BalancePreferencesActivity;

public class MainActivity extends Activity {
	private static String TAG = "2DegreesPhoneBalanceMainActivity";
	private UpdateReciever reciever;

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
    	case R.id.refresh_button:
    		forceUpdate();
    		return true;
    	case R.id.openPreferences:
    		Intent openPreferences = new Intent(this, BalancePreferencesActivity.class);
    		startActivityForResult(openPreferences, 2);
    		return true;
    	case R.id.openLog:
    		startActivity(new Intent(this, LogViewActivity.class));
    		return true;
    	case R.id.callCustomerService:
    		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0800022022")));
    		return true;
    	case R.id.topup:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Choose a topup method:");
    		builder.setItems(new CharSequence[] {
    				"Voucher topup"
    		}, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		    	switch(item) {
    		    	case 0:
    		    		String encodedHash = Uri.encode("#");
    		    		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:*100*2" + encodedHash)));
    		    		break;
    		    	}
    		    }
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    		return true;
    	case R.id.openAbout:
    		//Open the about dialog
    		try {
				AboutDialog.create(this).show();
			} catch (NameNotFoundException e) {
//				GATracker.getInstance().trackEvent("Exceptions", e.getMessage() + "Creating about dialog", StackTraceUtil.getStackTrace(e), 0);
			}
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
			Toast.makeText(this, "Username or password empty", Toast.LENGTH_SHORT).show();
			startActivityForResult(new Intent(this, SetupWizard.class), 1);
		}
    	refreshData();
    	reciever = new UpdateReciever(this);
    	// Register the reciever, so if an update happens while we are in the activity
    	// it will be updated
    	registerReceiver(reciever, new IntentFilter(UpdateWidgetService.NEWDATA));
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == 1) {
    		forceUpdate();
    	} else if (requestCode == 2) {
    		if (resultCode == BalancePreferencesActivity.RESULT_FORCE_UPDATE) {
    			forceUpdate();
    		}
    	}
    }
    private void forceUpdate() {
		progressDialog = ProgressDialog.show(this, null , " Loading. Please wait ... ", true);
		progressDialog.show();
		Intent update = new Intent(this, UpdateWidgetService.class);
		update.putExtra("biz.shadowservices.PhoneBalanceWidget.forceUpdates", true);
		startService(update);
//    	GATracker.getInstance().trackEvent("Actions", "Manual Refresh", "MainActivity", 0);
    
    }
    @Override
    public void onPause() {
    	super.onPause();
    	// Unregister the broadcast reciever, since we are now no longer interested in updates happening.
    	unregisterReceiver(reciever);
    	if(progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
    private OnClickListener buyPackListener = new OnClickListener() {
    	public void onClick(View v) {
    		valuePackMenuNodeView(Values.purchaseValuePacks);
    	}
    };
    private OnClickListener refreshListener = new OnClickListener() {
    	public void onClick(View v) {
    		MainActivity.this.forceUpdate();
    	}
    };
    private void valuePackMenuNodeView(final PackTreeNode node) {
    	if (node instanceof PackTreeNode.PackTreeLeaf) {
    		// We have reached a leaf in the menu, ask for confirmation to send.
    		askToSend((PackTreeNode.PackTreeLeaf) node);
    		return;
    	}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(node.getQuestionText());
		builder.setItems(node.getChildrenCharSequence(), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	//Get the selected item's node in the tree
		    	PackTreeNode selectedNode = node.getAt(item);
		    	// Recursively open the node.
		    	valuePackMenuNodeView(selectedNode);
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    public void askToSend(final PackTreeNode.PackTreeLeaf leaf) {
    	AlertDialog.Builder confirmDialog = new AlertDialog.Builder(MainActivity.this);
    	confirmDialog.setMessage(leaf.getConfirmText() + leaf.getTitle() + " by sending '" + leaf.getMessage() + "' to 233?")
    		.setCancelable(false)
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					send233SMS(leaf.getMessage());
//			    	GATracker.getInstance().trackEvent("Purchase", leaf.getTitle(), "", leaf.getValue());
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
		DBOpenHelper dbhelper = new DBOpenHelper(this);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query("cache", new String[] {"name","value", "units", "expires_value", "expires_date", "plan_startamount", "plan_name"} , null, null,null,null,null);
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
			String updateStatusStr = sp.getString("updateStatus", "SUCCESS");
			FetchResult updateStatus = FetchResult.valueOf(updateStatusStr);
			Log.d(TAG, updateStatus.toString());
			switch(updateStatus) {
			case LOGINFAILED:
			case USERNAMEPASSWORDNOTSET:
				col1.setText("Last Update -- Login failed, set the correct username/password details in menu->preferences and then press back.");
				break;
			case NETWORKERROR:
				col1.setText("Last Update -- Network Error.");
				break;
			case NOTONLINE:
				col1.setText("Last Update -- Not Online");
				break;
			case SUCCESS:
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
				break;
			case NOTALLOWED:
				col1.setText("Last Update -- Not allowed by settings.");
				break;
			}				
			firstLine.addView(col1,  col1LayoutParams);
			firstLine.addView(col3, col3LayoutParams);
			layout.addView(firstLine);
			View ruler = new View(this);
			ruler.setBackgroundColor(0xFF9C9C9C);
			layout.addView(ruler, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, 2));
		}
		for (int i=0; i<cursor.getCount(); i++) {
			RelativeLayout firstLine = new RelativeLayout(this);
			firstLine.setPadding(0,5,5,0);
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
				if(Math.abs((cursor.getDouble(1) - Values.INCLUDED)) < 0.01) {
					valueStringBuilder.append("Included");
				} else {
					valueStringBuilder.append(cursor.getString(1));
				}
				valueStringBuilder.append(" ");
			}
			if(cursor.getString(2) != null) {
				valueStringBuilder.append(cursor.getString(2));
			}
			col3.setText(valueStringBuilder.toString());
			firstLine.addView(col3, col3LayoutParams);
			layout.addView(firstLine);
			//if (!cursor.isNull(4)) {
			String planName = "";
			ProgressBar bar = null;
			LayoutParams progressBarParams= null;
			if (!cursor.isNull(5)) {
				Log.d(TAG, cursor.getString(0) + " Not null: " + cursor.getInt(5) );
				double currentVal = cursor.getDouble(1);
				double startVal = cursor.getDouble(5);
				planName = cursor.getString(6) + " Pack: ";
				bar = (ProgressBar)getLayoutInflater().inflate(R.layout.pack_progress_bar, null);
				bar.setMax((int)startVal);
				Log.d(TAG, String.valueOf(startVal - currentVal));
				bar.setProgress((int)(startVal - currentVal));
				progressBarParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
			TextView row2 = new TextView(this);
			row2.setPadding(10,0,5,5);
			SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
			Date expiryDate;
			String expiresInfo = "";
			String expiresMiddle = " expiring on ";
			if (cursor.getString(0).equals(DataFetcher.LASTMONTHCHARGES)) {
				expiresMiddle = " payment is due ";
			}
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
								expiresInfo += " " + cursor.getString(2) + expiresMiddle + expiryDateString ;
							} else {
								expiresInfo = expiresMiddle + expiryDateString ;
							}
						} else {
							expiresInfo = expiresMiddle  + expiryDateString ;
						}
					}
				}	
			} catch (ParseException e) {
				Log.e(TAG, "Could not parse date from DB.");
			}
			if (expiresInfo.length() > 0) {
				row2.setText(planName + expiresInfo);
				layout.addView(row2);
			}
			if (!cursor.isNull(5)) {
				layout.addView(bar, progressBarParams);
			}
			cursor.moveToNext(); 
			View ruler = new View(this);
			ruler.setBackgroundColor(0xFF9C9C9C);
			layout.addView(ruler, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, 2));
		}
		cursor.close();
		db.close();
    }
       
    public class UpdateReciever extends BroadcastReceiver {

        private MainActivity activity;

        public UpdateReciever(MainActivity activity) {
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
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("233", null, message, null, null);
    	Log.d(TAG, "sent message: " + message);
    }    
}