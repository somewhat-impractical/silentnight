package net.darktrojan.ringer;

import android.app.ActionBar;
import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;

import net.darktrojan.ringer.ChangeManager.ModeChange;

public class MainActivity extends Activity {

	static final String INTENT_CHANGE_MODE = "net.darktrojan.ringer.CHANGE_MODE";
	static final String INTENT_EXTRA_NEW_MODE = "newMode";

	ChangeAdapter ca;
	DataSetObserver observer;
	Switch actionBarSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		actionBarSwitch = new Switch(this);
		actionBarSwitch.setPaddingRelative(0, 0, 16, 0);
		actionBarSwitch.setChecked(ChangeManager.getIsRunning(this));
		actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				ChangeManager.setIsRunning(MainActivity.this, checked);
			}
		});

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
					ActionBar.LayoutParams.WRAP_CONTENT,
					ActionBar.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_VERTICAL | Gravity.END
			));
		}

		ca = new ChangeAdapter(this);
		ListView listView = (ListView) (this.findViewById(R.id.listView1));
		listView.setAdapter(ca);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				ModeChange item = (ModeChange) adapterView.getItemAtPosition(position);
				TimePickerFragment tpf = TimePickerFragment.createInstance(ca, item);
				tpf.show(getFragmentManager(), "tpf");
			}
		});

		observer = new DataSetObserver() {
			public void onChanged() {
				Log.v("MainActivity", "Dataset changed");
				PieChart pieChart = (PieChart) (MainActivity.this.findViewById(R.id.pie_chart));
				pieChart.refresh();
			}
		};
	}

	@Override
	public void onPause() {
		super.onPause();
		ca.unregisterDataSetObserver(observer);
	}

	@Override
	public void onResume() {
		super.onResume();
		PieChart pieChart = (PieChart) (this.findViewById(R.id.pie_chart));
		pieChart.refresh();
		ca.registerDataSetObserver(observer);
	}
}
