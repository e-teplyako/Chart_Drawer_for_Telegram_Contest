package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.UI.ThemedDrawer;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.HashSet;

public abstract class AbsLineChartDrawer implements ChartDrawer, ValueAnimator.AnimatorUpdateListener {
	ChartData _chartData;
	HashSet<LineDrawer> _lineDrawers;

	long _minValue;
	long _maxValue;
	int _minVisibleIndex;
	int _maxVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;


	public abstract Canvas draw(Canvas canvas);

	public abstract void drawChosenPointHighlight(Canvas canvas, int index);

	public void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener) {
		long width = _maxValue - _minValue;
		long startPos = (long) Math.floor(start * width) + _minValue;
		long endPos = (long) Math.ceil(end * width) + _minValue;

		long distanceToScreenBorder = (long) Math.ceil (((endPos - startPos) * _chartAreaWidthMarginPx) / _chartAreaWidthPx);

		_minVisibleIndex = MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(), startPos - distanceToScreenBorder);
		_maxVisibleIndex = MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(),  endPos + distanceToScreenBorder);

		_mappedXPoints  = mapXPoints(startPos, endPos);
		for (LineDrawer drawer : _lineDrawers) {
			drawer.setMappedXPoints(_mappedXPoints);
			drawer.setMinMaxIndexes(_minVisibleIndex, _maxVisibleIndex);
		}

		setMinMaxYAndAnimate(listener);
	}

	abstract void setMinMaxYAndAnimate(ValueAnimator.AnimatorUpdateListener listener);

	public void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx) {
		_chartAreaWidthPx = endX - startX;
		_chartAreaWidthMarginPx = chartAreaWidthMarginPx;

		for (LineDrawer drawer : _lineDrawers) {
			drawer.setMargins(startY, endY);
		}
	}

	public void setLinesAndAnimate(ValueAnimator.AnimatorUpdateListener listener) {
		for (LineDrawer drawer : _lineDrawers) {
			if (_chartData.isLineActive(drawer.getLine())) {
				drawer.animateAlpha(true, listener);
			}
			else
				drawer.animateAlpha(false, listener);
		}

		setMinMaxYAndAnimate(listener);
	}

	public int getTouchedPointIndex(float x) {
		int position = MathUtils.getIndexOfNearestElement(_mappedXPoints, x);
		while (_chartData.getXPoints()[position + _minVisibleIndex] < _minValue) {
			position++;
		}
		while (_chartData.getXPoints()[position + _minVisibleIndex] > _maxValue) {
			position--;
		}
		return position + _minVisibleIndex;
	}

	public float getTouchedPointPosition(int index) {
		return _mappedXPoints[index - _minVisibleIndex];
	}

	private float[] mapXPoints(long xMin, long xMax) {
		long calculatedArea = xMax - xMin;
		float[] mappedXPoints = new float[_maxVisibleIndex - _minVisibleIndex + 1];
		for (int i = 0, j = _minVisibleIndex; i < mappedXPoints.length; i++, j++) {
			float percentage = (float) (_chartData.getXPoints()[j] - xMin) / (float) calculatedArea;
			mappedXPoints[i] = _chartAreaWidthMarginPx + _chartAreaWidthPx * percentage;
		}
		return mappedXPoints;
	}

	public boolean isInSetLinesTransition() {
		boolean result = false;
		for (LineDrawer drawer : _lineDrawers) {
			if (drawer.isInSetLinesTransion())
				result = true;
		}
		return result;
	}

	@Override
	public void setPlateFillColor(int color) {

	}

	@Override
	public void setPrimaryBgColor(int color) {
		for (LineDrawer drawer : _lineDrawers) {
			drawer.setPrimaryBgColor(color);
		}
	}

	@Override
	public void setSliderBgColor(int color) {

	}

	@Override
	public void setSliderHandlerColor(int color) {

	}

	@Override
	public void setDividerColor(int color) {

	}

	@Override
	public void setMainTextColor(int color) {

	}

	@Override
	public void setLabelColor(int color) {

	}

	@Override
	public void setOpaquePlateColor(int color) {

	}
}