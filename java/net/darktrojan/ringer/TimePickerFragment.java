package net.darktrojan.ringer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import net.darktrojan.ringer.ChangeManager.ModeChange;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	ChangeAdapter ca = null;
	ModeChange mc = null;

	public static TimePickerFragment createInstance(ChangeAdapter ca, ModeChange mc) {
		TimePickerFragment tpf = new TimePickerFragment();
		tpf.ca = ca;
		tpf.mc = mc;
		return tpf;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int hour, minute;
		if (this.mc == null) {
			// This should never happen.
			final Calendar c = Calendar.getInstance();
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
		} else {
			hour = mc.hour;
			minute = mc.minute;
		}
		return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Log.v("TimePickerFragment", "onTimeSet");
		if (this.mc != null) {
			this.mc.hour = hourOfDay;
			this.mc.minute = minute;
		}

		Context context = getActivity();

		ChangeManager.refreshSort();
		ChangeManager.saveToPref(context);
		if (ChangeManager.getIsRunning(context)) {
			ChangeManager.setNextChange(context);
		}

		if (this.ca != null) {
			this.ca.notifyDataSetChanged();
		}
	}
}
