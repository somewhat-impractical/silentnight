package net.darktrojan.ringer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.darktrojan.ringer.ChangeManager.ModeChange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PieChart extends View {

	RectF radiusRect, innerRadiusRect;
	float w, h, cX, cY, radius, innerRadius, innerRadius2, halfRadius, angle, minuteAngle;
	int iconSize;
	Paint piePaint, iconPaint, changePaint, vibratePaint, silentPaint;
	Drawable sun, moon, sunrise, sunset;
	List<Path> arcs = new ArrayList<Path>();
	List<Paint> colours = new ArrayList<Paint>();

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		piePaint.setColor(0xFFFFFFFF);
		piePaint.setStyle(Paint.Style.STROKE);
		piePaint.setStrokeWidth(3f);

		iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		iconPaint.setColor(0xFFFFCC00);
		iconPaint.setStyle(Paint.Style.FILL);

		changePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		changePaint.setColor(0xFFFF6600);
		changePaint.setStyle(Paint.Style.FILL);

		vibratePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		vibratePaint.setColor(0xFFFFCC00);
		vibratePaint.setStyle(Paint.Style.FILL);

		silentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		silentPaint.setColor(0xFFFF6600);
		silentPaint.setStyle(Paint.Style.FILL);

		this.sun = context.getResources().getDrawable(R.drawable.ic_sun);
		this.moon = context.getResources().getDrawable(R.drawable.ic_moon);
		this.sunrise = context.getResources().getDrawable(R.drawable.ic_sunrise);
		this.sunset = context.getResources().getDrawable(R.drawable.ic_sunset);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode == MeasureSpec.EXACTLY) {
			setMeasuredDimension(widthSize, (int) (widthSize * 0.8));
		} else {
			//noinspection SuspiciousNameCombination
			setMeasuredDimension(heightSize, heightSize);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.w = w;
		this.h = h;

		loadChart();
	}

	void loadChart() {
		cX = w / 2;
		cY = h / 2;
		radius = Math.min(cX, cY) - 4;
		innerRadius = radius * 0.9f;
		innerRadius2 = radius * .85f;
		halfRadius = radius * 0.55f;

		radiusRect = new RectF(-radius, -radius, radius, radius);
		innerRadiusRect = new RectF(-innerRadius, -innerRadius, innerRadius, innerRadius);

		iconSize = (int) (radius * 0.18);

		Calendar now = Calendar.getInstance();
		angle = (float) (now.get(Calendar.HOUR_OF_DAY) * -15);
		minuteAngle = now.get(Calendar.MINUTE) * -0.25f;

		loadArcs();
	}

	void loadArcs() {
		ArrayList<ModeChange> changes = ChangeManager.mChanges;
		int size = changes.size();

		arcs.clear();
		colours.clear();

		if (size == 0) {
			return;
		}

		ModeChange start, end;
		start = changes.get(size - 1);

		for (ModeChange change : changes) {
			end = change;
//			Log.d("PieChart", "from " + start + " to " + end);
			if (start.mode != AudioManager.RINGER_MODE_NORMAL && (end.hour != start.hour || end.minute != start.minute)) {
				arcs.add(getTimeArc(start.hour, start.minute, end.hour, end.minute));
				colours.add(start.mode == AudioManager.RINGER_MODE_SILENT ? silentPaint : vibratePaint);
			}
			start = end;
		}
	}

	Path getTimeArc(int fromHour, int fromMinute, int toHour, int toMinute) {
		float fromAngle = 270 + fromHour * 15 + fromMinute * 0.25f;
		float toAngle = 270 + toHour * 15 + toMinute * 0.25f;

		// before midnight - after midnight
		if (fromAngle > toAngle) {
			fromAngle -= 360;
		}
		float sweep = toAngle - fromAngle;

		Path p = new Path();
		p.arcTo(innerRadiusRect, fromAngle, sweep);
		p.arcTo(radiusRect, toAngle, -sweep);
		return p;
	}

	void refresh() {
		Log.v("PieChart", "Chart refreshed");
		loadChart();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(cX, cY);
		canvas.rotate(angle + minuteAngle);

		for (int i = 0; i < this.arcs.size(); i++) {
			canvas.drawPath(this.arcs.get(i), this.colours.get(i));
		}

		canvas.drawCircle(0, 0, radius, piePaint);
		for (int i = 0; i < 24; i++) {
			canvas.drawLine(0, -radius, 0, i % 6 == 0 ? -innerRadius2 : -innerRadius, piePaint);
			canvas.rotate(15.0f);
		}

		canvas.save();
		canvas.rotate(-angle - minuteAngle, 0, halfRadius);
		// canvas.drawRect(-iconSize, halfRadius - iconSize, iconSize, halfRadius + iconSize, iconPaint);
		sun.setBounds(-iconSize, (int) halfRadius - iconSize, iconSize, (int) halfRadius + iconSize);
		sun.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.rotate(-angle - minuteAngle, halfRadius, 0);
		//canvas.drawRect(halfRadius -iconSize, -iconSize, halfRadius + iconSize, iconSize, iconPaint);
		sunrise.setBounds((int) halfRadius - iconSize, -iconSize, (int) halfRadius + iconSize, iconSize);
		sunrise.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.rotate(-angle - minuteAngle, 0, -halfRadius);
		//canvas.drawRect(-iconSize, -halfRadius - iconSize, iconSize, -halfRadius + iconSize, iconPaint);
		moon.setBounds(-iconSize, (int) -halfRadius - iconSize, iconSize, (int) -halfRadius + iconSize);
		moon.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.rotate(-angle - minuteAngle, -halfRadius, 0);
		//canvas.drawRect(-halfRadius -iconSize, -iconSize, -halfRadius + iconSize, iconSize, iconPaint);
		sunset.setBounds((int) -halfRadius - iconSize, -iconSize, (int) -halfRadius + iconSize, iconSize);
		sunset.draw(canvas);
		canvas.restore();
	}
}