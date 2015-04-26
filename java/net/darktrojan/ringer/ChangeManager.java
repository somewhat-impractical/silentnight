package net.darktrojan.ringer;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

public class ChangeManager {

	static final String CHANGE_FILE = "changes";

	static final String PREF_FILE = "prefs";
	static final String RUNNING = "running";

	static String[] ringer_modes;

	static ArrayList<ModeChange> mChanges = new ArrayList<ModeChange>();

	static void setChangeSchedule(ModeChange[] changes) {
		mChanges.clear();

		for (ModeChange mc : changes) {
			Log.v("ChangeManager", mc.toString());
			mChanges.add(mc);
		}
	}

	static void refreshSort() {
		ModeChange[] changes = mChanges.toArray(new ModeChange[mChanges.size()]);
		Arrays.sort(changes, new ModeChangeComparator());
		setChangeSchedule(changes);
	}

	@SuppressLint("DefaultLocale")
	private static void setChange(Context context, long millis, int mode) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(millis);
		Log.d("ChangeManager", "Setting up change to " + ringer_modes[mode].toLowerCase() + " at " + calendarToString(c));

		Resources resources = context.getResources();
		String[] ringer_modes = context.getResources().getStringArray(R.array.ringer_modes);
		String toastText = resources.getString(R.string.change_set_toast, ringer_modes[mode].toLowerCase(), DateFormat.getTimeFormat(context).format(c.getTime()));
		Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();

		Intent finishIntent;
		PendingIntent pendingIntent;
		AlarmManager alarmManager;

		finishIntent = new Intent(MainActivity.INTENT_CHANGE_MODE);
		finishIntent.putExtra(MainActivity.INTENT_EXTRA_NEW_MODE, mode);
		pendingIntent = PendingIntent.getBroadcast(context, 0, finishIntent, PendingIntent.FLAG_ONE_SHOT);
		alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
		alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
	}

	static void setNextChange(Context context) {
		if (mChanges.isEmpty()) {
			readFromPref(context);
		}

		Calendar now = Calendar.getInstance();
		ModeChange nextChange = null;
		long nextChangeTime = -1;

		for (ModeChange mc : mChanges) {
			Calendar changeTime = Calendar.getInstance();
			changeTime.set(Calendar.HOUR_OF_DAY, mc.hour);
			changeTime.set(Calendar.MINUTE, mc.minute);
			changeTime.set(Calendar.SECOND, 0);
			changeTime.set(Calendar.MILLISECOND, 0);
			Log.v("ChangeManager", "Checking " + calendarToString(changeTime));
			if (changeTime.after(now)) {
				nextChange = mc;
				nextChangeTime = changeTime.getTimeInMillis();
				break;
			}
		}

		if (nextChange == null) {
			for (ModeChange mc : mChanges) {
				Calendar changeTime = Calendar.getInstance();
				changeTime.set(Calendar.HOUR_OF_DAY, mc.hour);
				changeTime.set(Calendar.MINUTE, mc.minute);
				changeTime.set(Calendar.SECOND, 0);
				changeTime.set(Calendar.MILLISECOND, 0);
				changeTime.add(Calendar.DAY_OF_MONTH, 1);
				Log.v("ChangeManager", "Checking " + calendarToString(changeTime));
				if (changeTime.after(now)) {
					nextChange = mc;
					nextChangeTime = changeTime.getTimeInMillis();
					break;
				}
			}
		}

		if (nextChange != null) {
			setChange(context, nextChangeTime, nextChange.mode);
		}
	}

	static void cancelChanges(Context context) {
		Log.d("ChangeManager", "Cancelling pending change");

		Intent cancelIntent;
		PendingIntent pendingIntent;
		AlarmManager alarmManager;

		cancelIntent = new Intent(MainActivity.INTENT_CHANGE_MODE);
		pendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_ONE_SHOT);
		alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
		alarmManager.cancel(pendingIntent);

		Resources resources = context.getResources();
		String toastText = resources.getString(R.string.change_cancelled_toast);
		Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
	}

	static void saveToPref(Context context) {
		StringBuilder sb = new StringBuilder();
		for (ModeChange mc : mChanges) {
			sb.append(mc.toPrefString());
		}
		SharedPreferences prefs = context.getSharedPreferences(CHANGE_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CHANGE_FILE, sb.toString());
		editor.apply();

		Log.v("ChangeManager", "Saving to prefs: " + sb.toString());
	}

	static void readFromPref(Context context) {
		ChangeManager.ringer_modes = context.getResources().getStringArray(R.array.ringer_modes);

		SharedPreferences prefs = context.getSharedPreferences(CHANGE_FILE, 0);
		String data = prefs.getString(CHANGE_FILE, "1,7,30\n2,8,30\n1,22,30\n0,23,30");

		Log.v("ChangeManager", "Pref data: " + data);

		String[] lines = data.trim().split("\n");
		ModeChange[] changes = new ModeChange[lines.length];
		for (int i = 0; i < lines.length; i++) {
			changes[i] = ModeChange.fromPrefString(lines[i]);
		}
		Arrays.sort(changes, new ModeChangeComparator());
		setChangeSchedule(changes);
	}

	static void setIsRunning(Context context, boolean running) {
		if (running) {
			setNextChange(context);
		} else {
			cancelChanges(context);
		}
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(RUNNING, running);
		editor.apply();
	}

	static boolean getIsRunning(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		return prefs.getBoolean(RUNNING, false);
	}

	@SuppressLint("DefaultLocale")
	private static String calendarToString(Calendar c) {
		return String.format(
				"%d-%02d-%02d %02d:%02d",
				c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
		);
	}

	static class ModeChange {
		int hour, minute, mode;

		public ModeChange(int hour, int minute, int mode) {
			this.hour = hour;
			this.minute = minute;
			this.mode = mode;
		}

		@SuppressLint("DefaultLocale")
		@Override
		public String toString() {
			return String.format("ModeChange: to %s, at %2d:%02d", ChangeManager.ringer_modes[this.mode], this.hour, this.minute);
		}

		public String toPrefString() {
			return this.mode + "," + this.hour + "," + this.minute + "\n";
		}

		public static ModeChange fromPrefString(String string) {
			String[] parts = string.split(",");
			int mode = Integer.parseInt(parts[0]);
			int hour = Integer.parseInt(parts[1]);
			int minute = Integer.parseInt(parts[2]);
			return new ModeChange(hour, minute, mode);
		}
	}

	static class ModeChangeComparator implements Comparator<ModeChange> {
		@Override
		public int compare(ModeChange a, ModeChange b) {
			if (a.hour < b.hour) {
				return -1;
			}
			if (a.hour > b.hour) {
				return 1;
			}
			return a.minute - b.minute;
		}
	}
}