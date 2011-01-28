package biz.shadowservices.DegreesToolbox;

import biz.shadowservices.DegreesToolbox.R;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

public class BalancePreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        // from http://christophersaunders.ca/blog/2011/01/11/preventing-screen-keyboard-saving-passwords-entered-user/
        Preference passwordPref = findPreference("password");
        passwordPref.setOnPreferenceClickListener(this);
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

}
