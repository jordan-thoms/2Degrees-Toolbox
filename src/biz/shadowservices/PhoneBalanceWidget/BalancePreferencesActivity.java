package biz.shadowservices.PhoneBalanceWidget;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class BalancePreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
