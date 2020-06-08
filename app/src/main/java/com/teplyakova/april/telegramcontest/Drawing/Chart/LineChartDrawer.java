package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.Animators.LocalYMinMaxAnimator;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.HashSet;

public class LineChartDrawer extends AbsLineChartDrawer {
	private int _localYMin;
	private int _localYMax;

	private Paint _dividerPaint;

	public LineChartDrawer(ChartData chartData) {
		_chartData = chartData;
		_lineDrawers = new HashSet<>();
		for (LineData line : chartData.getLines()) {
			_lineDrawers.add(new LineDrawer(line));
		}

		_minValue = chartData.getXPoints()[0];
		_maxValue = chartData.getXPoints()[chartData.getXPoints().length - 1];
		_minVisibleIndex = 0;
		_maxVisibleIndex = chartData.getXPoints().length - 1;

		for (LineDrawer drawer : _lineDrawers) {
			drawer.setMinMaxIndexes(_minVisibleIndex, _maxVisibleIndex);
			drawer.setStrokeWidth(5);
		}

		_dividerPaint = new Paint();
		_dividerPaint.setStrokeWidth(2);
	}

	public Canvas draw(Canvas canvas) {
		for (LineDrawer drawer : _lineDrawers) {
				canvas = drawer.draw(canvas, _localYMin, _localYMax);
		}
		return canvas;
	}

	@Override
	public void setAntiAlias(boolean aa) {
		for (LineDrawer drawer : _lineDrawers) {
			drawer.setAntiAlias(aa);
		}
	}

	public void drawChosenPointHighlight(Canvas canvas, int index) {
		drawChosenPointLine(canvas, getTouchedPointPosition(index));
		for (LineDrawer drawer : _lineDrawers) {
			drawer.drawChosenPointCircle(canvas, index, _localYMin, _localYMax);
		}
	}

	void setMinMaxYAndAnimate(ValueAnimator.AnimatorUpdateListener listener) {
		int endMin = MathUtils.getLocalMin(_chartData.getActiveLines(), _minVisibleIndex, _maxVisibleIndex);
		int endMax = MathUtils.getLocalMax(_chartData.getActiveLines(), _minVisibleIndex, _maxVisibleIndex);
		LocalYMinMaxAnimator animator = new LocalYMinMaxAnimator();
		animator.start(_localYMin, endMin, _localYMax, endMax, listener, this);
	}

	@Override
	void drawChosenPointLine(Canvas canvas, float pointPosition) {
		canvas.drawLine(pointPosition, _startY, pointPosition, _endY, _dividerPaint);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		_localYMin = (int) animation.getAnimatedValue(LocalYMinMaxAnimator.MIN);
		_localYMax = (int) animation.getAnimatedValue(LocalYMinMaxAnimator.MAX);
	}

	@Override
	public void setDividerColor(int color) {
		_dividerPaint.setColor(color);
	}
}
