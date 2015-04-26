package net.darktrojan.ringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class ModeChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int newMode = intent.getIntExtra(MainActivity.INTENT_EXTRA_NEW_MODE, -1);
		if (newMode == -1) {
			return;
		}

		Log.d("ModeChangeReceiver", "Broadcast fired, changing mode to " + newMode);
		AudioManager am = (AudioManager) (context.getSystemService(Context.AUDIO_SERVICE));
		am.setRingerMode(newMode);

		ChangeManager.setNextChange(context);
	}
}
