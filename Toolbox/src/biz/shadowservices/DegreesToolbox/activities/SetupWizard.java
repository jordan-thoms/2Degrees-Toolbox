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

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.R.id;
import biz.shadowservices.DegreesToolbox.R.layout;
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

public class SetupWizard extends BaseActivity {
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
