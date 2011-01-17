package biz.shadowservices.PhoneBalanceWidget;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PhoneBalanceMain extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button testButton = (Button) findViewById(R.id.button);
        testButton.setOnClickListener(updateButtonListener);
    }
    
    private OnClickListener updateButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		/* PhoneBalanceDBOpenHelper dbhelper = new PhoneBalanceDBOpenHelper(v.getContext());
    		SQLiteDatabase db = dbhelper.getWritableDatabase();
    		//db.delete("preferences", "name = 'username' OR name='password'", whereArgs)
    		TextView t = (TextView) findViewById(R.id.text);
    		t.setText("A"); */
    		Intent openPreferences = new Intent(v.getContext(), BalancePreferencesActivity.class);
    		startActivity(openPreferences);
    	}
    };
}