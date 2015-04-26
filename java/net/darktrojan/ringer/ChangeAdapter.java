package net.darktrojan.ringer;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.darktrojan.ringer.ChangeManager.ModeChange;

import java.util.Calendar;

class ChangeAdapter extends ArrayAdapter<ModeChange> {

	private static final int AUDIBLE_COLOUR = 0xffffffff;
	private static final int VIBRATE_COLOUR = 0xffffcc00;
	private static final int SILENT_COLOUR = 0xffff6600;

	private static final int[] COLOURS = {SILENT_COLOUR, VIBRATE_COLOUR, AUDIBLE_COLOUR};

	public ChangeAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2, android.R.id.text1, ChangeManager.mChanges);

		if (ChangeManager.mChanges.isEmpty()) {
			ChangeManager.readFromPref(context);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup vg;
		if (convertView == null) {
			vg = (ViewGroup) super.getView(position, null, parent);
		} else {
			vg = (ViewGroup) (convertView);
		}

		ModeChange item = ChangeManager.mChanges.get(position);

		TextView text1 = (TextView) vg.findViewById(android.R.id.text1);
		text1.setTextColor(COLOURS[item.mode]);
		text1.setText(parent.getResources().getStringArray(R.array.ringer_modes)[item.mode]);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, item.hour);
		c.set(Calendar.MINUTE, item.minute);
		TextView text2 = (TextView) vg.findViewById(android.R.id.text2);
		text2.setText(DateFormat.getTimeFormat(parent.getContext()).format(c.getTime()));

		return vg;
	}
}
