package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class BarChartDrawer implements ChartDrawer, ValueAnimator.AnimatorUpdateListener {
	ChartData _chartData;

	long _minValue;
	long _maxValue;
	int _minVisibleIndex;
	int _maxVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;

	public BarChartDrawer(ChartData chartData) {
		_chartData = chartData;

		_minValue = chartData.getXPoints()[0];
		_maxValue = chartData.getXPoints()[chartData.getXPoints().length - 1];
		_minVisibleIndex = 0;
		_maxVisibleIndex = chartData.getXPoints().length - 1;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {

	}

	@Override
	public Canvas draw(Canvas canvas) {
		return null;
	}

	@Override
	public Canvas drawChartForGlobalRange(Canvas canvas) {
		return null;
	}

	@Override
	public void drawChosenPointHighlight(Canvas canvas, int index) {

	}

	@Override
	public void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener) {
		long width = _maxValue - _minValue;
		long startPos = (long) Math.floor(start * width) + _minValue;
		long endPos = (long) Math.ceil(end * width) + _minValue;

		long distanceToScreenBorder = (long) Math.ceil (((endPos - startPos) * _chartAreaWidthMarginPx) / _chartAreaWidthPx);

		_minVisibleIndex = MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(), startPos - distanceToScreenBorder);
		_maxVisibleIndex = MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(),  endPos + distanceToScreenBorder);

		_mappedXPoints  = mapXPoints(startPos, endPos);
	}

	@Override
	public void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx) {
		_chartAreaWidthPx = endX - startX;
		_chartAreaWidthMarginPx = chartAreaWidthMarginPx;
	}

	@Override
	public void setLinesAndAnimate(ValueAnimator.AnimatorUpdateListener listener) {

	}

	@Override
	public boolean isInSetLinesTransition() {
		return false;
	}

	@Override
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

	@Override
	public float getTouchedPointPosition(int index) {
		return _mappedXPoints[index - _minVisibleIndex];
	}

	@Override
	public void setPlateFillColor(int color) {

	}

	@Override
	public void setPrimaryBgColor(int color) {

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

	private float[] mapXPoints(long xMin, long xMax) {
		long calculatedArea = xMax - xMin;
		float[] mappedXPoints = new float[_maxVisibleIndex - _minVisibleIndex + 1];
		for (int i = 0, j = _minVisibleIndex; i < mappedXPoints.length; i++, j++) {
			float percentage = (float) (_chartData.getXPoints()[j] - xMin) / (float) calculatedArea;
			mappedXPoints[i] = _chartAreaWidthMarginPx + _chartAreaWidthPx * percentage;
		}
		return mappedXPoints;
	}

}
