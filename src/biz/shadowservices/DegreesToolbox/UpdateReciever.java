package biz.shadowservices.DegreesToolbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateWidgetService.class));
	}

}
