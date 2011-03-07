package biz.shadowservices.DegreesToolbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReciever extends BroadcastReceiver {
	//Recieves intents for updates and starts the update service.
	public static String TAG = "2DegreesUpdateReciever";
	@Override
	public void onReceive(Context context, Intent intent) {
        // To prevent any ANR timeouts, we perform the update in a service
		Log.d(TAG, "Started from alarm");
        context.startService(new Intent(context, UpdateWidgetService.class));
	}

}
