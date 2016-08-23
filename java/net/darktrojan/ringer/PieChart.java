package net.darktrojan.ringer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.darktrojan.ringer.ChangeManager.ModeChange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PieChart extends View {

	final static String LOG_TAG = "PieChart";

	RectF radiusRect, innerRadiusRect;
	Path nowMarkerPath, labelPath0, labelPath1, labelPath2;
	float w, h, cX, cY, radius, innerRadius, innerRadius2, hourAngle, minuteAngle, labelOffset;
	int iconSize;
	Paint piePaint, iconPaint, changePaint, vibratePaint, silentPaint, labelPaint;
	List<Path> arcs = new ArrayList<>();
	List<Paint> colours = new ArrayList<>();

	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		piePaint.setColor(Color.WHITE);
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

		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setColor(Color.WHITE);
		labelPaint.setTextAlign(Paint.Align.CENTER);
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

		radiusRect = new RectF(-radius, -radius, radius, radius);
		innerRadiusRect = new RectF(-innerRadius, -innerRadius, innerRadius, innerRadius);

		iconSize = (int) (radius * 0.18);

		Calendar now = Calendar.getInstance();
		hourAngle = (float) (now.get(Calendar.HOUR_OF_DAY) * -15);
		minuteAngle = now.get(Calendar.MINUTE) * -0.25f;

		nowMarkerPath = new Path();
		nowMarkerPath.moveTo(0, radius * -0.95f);
		nowMarkerPath.lineTo(radius * 0.075f, radius * -0.825f);
		nowMarkerPath.lineTo(radius * -0.075f, radius * -0.825f);
		nowMarkerPath.close();

		float labelRadius = radius * 0.55f;
		labelPaint.setTextSize(radius * 0.125f);
		labelOffset = radius * -0.1f;

		labelPath0 = new Path();
		labelPath0.addCircle(0, 0, labelRadius, Path.Direction.CW);
		labelPath1 = new Path();
		labelPath1.addCircle(0, 0, labelRadius - 35, Path.Direction.CW);
		labelPath2 = new Path();
		labelPath2.addCircle(0, 0, labelRadius - 70, Path.Direction.CW);

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
//			Log.d(LOG_TAG, "from " + start + " to " + end);
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
		Log.v(LOG_TAG, "Chart refreshed");
		loadChart();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(cX, cY);
		canvas.save();
		canvas.rotate(hourAngle + minuteAngle);

		for (int i = 0; i < this.arcs.size(); i++) {
			canvas.drawPath(this.arcs.get(i), this.colours.get(i));
		}

		canvas.drawCircle(0, 0, radius, piePaint);
		for (int i = 0; i < 24; i++) {
			canvas.drawLine(0, -radius, 0, i % 6 == 0 ? -innerRadius2 : -innerRadius, piePaint);
			canvas.rotate(15.0f);
		}

		canvas.rotate(45);
		canvas.drawTextOnPath("EVENING", labelPath1, 0, labelOffset - 35, labelPaint);

		canvas.rotate(90);
		canvas.drawTextOnPath("NIGHT", labelPath2, 0, labelOffset - 70, labelPaint);

		canvas.rotate(90);
		canvas.drawTextOnPath("MORNING", labelPath1, 0, labelOffset - 35, labelPaint);

		canvas.rotate(90);
		canvas.drawTextOnPath("AFTERNOON", labelPath0, 0, labelOffset, labelPaint);

		canvas.restore();
		canvas.drawPath(nowMarkerPath, labelPaint);
	}
}