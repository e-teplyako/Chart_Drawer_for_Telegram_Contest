package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import com.teplyakova.april.telegramcontest.Animators.LocalYMinMaxByLineAnimator;
import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;
import java.util.HashMap;
import java.util.HashSet;

public class IndependentLineChartDrawer extends AbsLineChartDrawer {
	private HashMap<LineData, Integer> _localMins = new HashMap<>();
	private HashMap<LineData, Integer> _localMaxes = new HashMap<>();

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
	}
	@Override
	public Canvas draw(Canvas canvas) {
		for (LineDrawer drawer : _lineDrawers) {
			canvas = drawer.draw(canvas, _localMins.get(drawer.getLine()), _localMaxes.get(drawer.getLine()));
		}
		return canvas;
	}

	@Override
	public void drawChosenPointHighlight(Canvas canvas, int index) {
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
	public void onAnimationUpdate(ValueAnimator animation) {

	}
}
