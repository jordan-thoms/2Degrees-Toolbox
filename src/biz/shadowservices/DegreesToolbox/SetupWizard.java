package biz.shadowservices.DegreesToolbox;

import biz.shadowservices.DegreesToolbox.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetupWizard extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_wizard);
        Button goButton = (Button) findViewById(R.id.setupWizardGoButton);
        goButton.setOnClickListener(goButtonListener);
    	EditText password = (EditText)findViewById(R.id.passwordSetupWizard);
    	password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        // This gives us the masking that you see in your password fields
    	password.setTransformationMethod(new PasswordTransformationMethod());

	}
    private OnClickListener goButtonListener = new OnClickListener() {
    	public void onClick(View v) {
        	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        	Editor editor = sp.edit();
        	EditText username = (EditText)findViewById(R.id.usernameSetupWizard);
        	EditText password = (EditText)findViewById(R.id.passwordSetupWizard);
        	editor.putString("username", username.getText().toString());
        	editor.putString("password", password.getText().toString());
        	editor.commit();
        	finish();
    	}
    };

    @Override
    public void onResume() {
    	super.onResume();
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	EditText username = (EditText)findViewById(R.id.usernameSetupWizard);
    	EditText password = (EditText)findViewById(R.id.passwordSetupWizard);
    	username.setText(sp.getString("username", ""));
    	password.setText(sp.getString("password", ""));
    }
}
