package net.darktrojan.ringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BootReceiver", "Device booted, setting next change");
		ChangeManager.readFromPref(context);
		if (ChangeManager.getIsRunning(context)) {
			ChangeManager.setNextChange(context);
		}
	}
}