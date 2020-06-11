package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.Animators.LocalYMinMaxByLineAnimator;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;
import java.util.HashMap;
import java.util.HashSet;

public class IndependentLineChartDrawer extends AbsLineChartDrawer {
	private HashMap<LineData, Integer> _localMins = new HashMap<>();
	private HashMap<LineData, Integer> _localMaxes = new HashMap<>();

	private Paint _dividerPaint;

	public IndependentLineChartDrawer(ChartData chartData) {
		if (chartData.getLines().length > 2)
			throw new IllegalArgumentException("Number of lines: " +
					chartData.getLines().length +
					" Independent Line Chart can only hold 2 lines");

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

		for (LineData line : chartData.getLines()) {
			int localYMin = MathUtils.getLocalMin(line.getPoints(), _minVisibleIndex, _maxVisibleIndex);
			int localYMax = MathUtils.getLocalMax(line.getPoints(), _minVisibleIndex, _maxVisibleIndex);
			_localMins.put(line, localYMin);
			_localMaxes.put(line, localYMax);
		}
		_dividerPaint = new Paint();
	}
	@Override
	public Canvas draw(Canvas canvas) {
		for (LineDrawer drawer : _lineDrawers) {
			canvas = drawer.draw(canvas, _localMins.get(drawer.getLine()), _localMaxes.get(drawer.getLine()));
		}
		return canvas;
	}

	@Override
	public void setAntiAlias(boolean aa) {
		for (LineDrawer drawer : _lineDrawers) {
			drawer.setAntiAlias(aa);
		}
	}

	@Override
	public void drawChosenPointHighlight(Canvas canvas, int index) {
		drawChosenPointLine(canvas, getTouchedPointPosition(index));
		for (LineDrawer drawer : _lineDrawers) {
			drawer.drawChosenPointCircle(canvas, index, _localMins.get(drawer.getLine()), _localMaxes.get(drawer.getLine()));
		}
	}

	@Override
	void setMinMaxYAndAnimate(ValueAnimator.AnimatorUpdateListener listener) {
		for (LineData line : _chartData.getActiveLines()) {
			int localYMin = MathUtils.getLocalMin(line.getPoints(), _minVisibleIndex, _maxVisibleIndex);
			int localYMax = MathUtils.getLocalMax(line.getPoints(), _minVisibleIndex, _maxVisibleIndex);
			LocalYMinMaxByLineAnimator animator = new LocalYMinMaxByLineAnimator(line, _localMins, _localMaxes);
			animator.start(_localMins.get(line), localYMin, _localMaxes.get(line), localYMax, listener, this);
		}
	}

	@Override
	void drawChosenPointLine(Canvas canvas, float pointPosition) {
		canvas.drawLine(pointPosition, _startY, pointPosition, _endY, _dividerPaint);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {

	}

	@Override
	public void setDividerColor(int color) {
		_dividerPaint.setColor(color);
	}
}
