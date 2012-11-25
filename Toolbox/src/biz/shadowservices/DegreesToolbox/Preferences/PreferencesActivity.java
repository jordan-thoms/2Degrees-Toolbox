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
package biz.shadowservices.DegreesToolbox.Preferences;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.activities.MainActivity;
import biz.shadowservices.DegreesToolbox.activities.SetupWizard;
import biz.shadowservices.DegreesToolbox.widgets.AbstractWidgetProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	// Generate the constant for the result code which indicates that an update should be requested.  
	public static final int RESULT_FORCE_UPDATE = RESULT_FIRST_USER + 1;
	private static String TAG = "2DegreesPreferencesActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);
        // from http://christophersaunders.ca/blog/2011/01/11/preventing-screen-keyboard-saving-passwords-entered-user/
        Preference usernamePref = findPreference("username");
        usernamePref.setOnPreferenceChangeListener(this);
        Preference passwordPref = findPreference("password");
        passwordPref.setOnPreferenceClickListener(this);
        passwordPref.setOnPreferenceChangeListener(this);
        
        Preference freshTimePref = findPreference("freshTime");
        freshTimePref.setOnPreferenceChangeListener(numberCheckListener);
        
        Preference logoutButton = findPreference("logoutButton");
        logoutButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(preference.getContext()); 
				Editor e = sp.edit();
				e.clear();
				e.commit();
				Intent intent = new Intent(preference.getContext(), SetupWizard.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
				return true;
			}
		});
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onPreferenceClick(Preference preference){
        if(preference.getKey().equals("password")){
            EditTextPreference pref = (EditTextPreference) preference;
            EditText field = pref.getEditText();
            // This informs the keyboard not to show up the autocomplete.
            // Ensure that you have set this input type, otherwise users
            // may complain that your application is saving their passwords.
            field.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            // This gives us the masking that you see in your password fields
            field.setTransformationMethod(new PasswordTransformationMethod());
        }
        // We still return false so that the system will handle everything else.
        // We just needed to ensure that if this preference is the password pref
        // that it's properly protected from prying eyes.
        return false;
    }
    protected void onPause() {
    	super.onPause();
    	AbstractWidgetProvider.setAlarm(this);
    }
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if (key.equals("username") || key.equals("password")) {
			// If the changed preference is the username or password, set the result so that when we exit,
			// an update will automatically happen.
			Log.d(TAG, "Will force update on exit");
    		setResult(RESULT_FORCE_UPDATE);
		}
		return true;
	}
    /**
	 * Checks that a preference is a valid numerical value
	 */
	Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {

	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue) {
	        //Check that the string is an integer.
	        return numberCheck(newValue);
	    }
	};

	private boolean numberCheck(Object newValue) {
	    if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
	        return true;
	    }
	    else {
	        Toast.makeText(PreferencesActivity.this, newValue+" "+getResources().getString(R.string.is_an_invalid_number), Toast.LENGTH_SHORT).show();
	        return false;
	    }
	}

}
