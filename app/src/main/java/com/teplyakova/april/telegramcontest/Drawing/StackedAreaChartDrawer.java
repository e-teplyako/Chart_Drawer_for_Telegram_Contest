package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class StackedAreaChartDrawer implements ChartDrawer {
	private ChartData _chartData;
	private long _minValue;
	private long _maxValue;
	private int _firstVisibleIndex;
	private int _lastVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;
	private float _endY;
	private float _startY;

	private Paint _areaPaint;
	private ArrayList<Area> _areas = new ArrayList<>();

	public StackedAreaChartDrawer(ChartData chartData) {
		_chartData = chartData;

		_minValue = chartData.getXPoints()[0];
		_maxValue = chartData.getXPoints()[chartData.getXPoints().length - 1];
		_firstVisibleIndex = 0;
		_lastVisibleIndex = chartData.getXPoints().length - 1;

		for (LineData lineData : chartData.getLines())
		{
			Area area = new Area();
			area.Line = lineData;
			area.MappedPointsY = new float[lineData.getPoints().length];
			area.PosYCoefficient = 1;
			_areas.add(area);
		}
		calculatePercentages();
		setupPaint();
	}

	@Override
	public Canvas draw(Canvas canvas) {
		return drawAreas(canvas);
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

		_firstVisibleIndex = MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(), startPos - distanceToScreenBorder);
		_lastVisibleIndex = MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(),  endPos + distanceToScreenBorder);
		_mappedXPoints = mapXPoints(startPos, endPos);
		mapYPoints(getMaxPosYCoefficient());
	}

	@Override
	public void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx) {
		_chartAreaWidthPx = endX - startX;
		_chartAreaWidthMarginPx = chartAreaWidthMarginPx;
		_endY = endY;
		_startY = startY;
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
		while (_chartData.getXPoints()[position + _firstVisibleIndex] < _minValue) {
			position++;
		}
		while (_chartData.getXPoints()[position + _firstVisibleIndex] > _maxValue) {
			position--;
		}
		return position + _firstVisibleIndex;
	}

	@Override
	public float getTouchedPointPosition(int index) {
		return _mappedXPoints[index - _firstVisibleIndex];
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

	private void setupPaint() {
		_areaPaint = new Paint();
		_areaPaint.setStyle(Paint.Style.FILL);
		_areaPaint.setStrokeCap(Paint.Cap.SQUARE);
		_areaPaint.setAntiAlias(true);
	}

	private void preparePaths() {
		float[] previous = new float[_mappedXPoints.length];
		Arrays.fill(previous, _endY);
		Path path = new Path();
		path.moveTo(_mappedXPoints[_mappedXPoints.length - 1], _endY);
		for (int j = 0; j < _areas.size(); j++) {
			for (int i = previous.length - 1; i >= 0; i--) {
				path.lineTo(_mappedXPoints[i], previous[i]);
			}
			for (int i = 0; i < _areas.get(j).MappedPointsY.length; i++) {
				path.lineTo(_mappedXPoints[i], _areas.get(j).MappedPointsY[i]);
			}
			_areas.get(j).Path = path;
			path = new Path();
			path.moveTo(_mappedXPoints[_mappedXPoints.length - 1],
					_areas.get(j).MappedPointsY[_areas.get(j).MappedPointsY.length - 1]);
			previous = _areas.get(j).MappedPointsY;
		}
	}

	private float[] mapXPoints(long xMin, long xMax) {
		long calculatedArea = xMax - xMin;
		float[] mappedXPoints = new float[_lastVisibleIndex - _firstVisibleIndex + 1];
		for (int i = 0, j = _firstVisibleIndex; i < mappedXPoints.length; i++, j++) {
			float percentage = (float) (_chartData.getXPoints()[j] - xMin) / (float) calculatedArea;
			mappedXPoints[i] = _chartAreaWidthMarginPx + _chartAreaWidthPx * percentage;
		}
		return mappedXPoints;
	}

	private void mapYPoints(float coefficient) {
		int[] previous = new int[_lastVisibleIndex - _firstVisibleIndex + 1];
		for (Area area : _areas) {
			area.MappedPointsY = new float[_lastVisibleIndex - _firstVisibleIndex + 1];
			for (int i = 0, j = _firstVisibleIndex; i < area.MappedPointsY.length; i++, j++) {
				float percentage = (area.Percentages[j] * area.PosYCoefficient + previous[i]) / 100;
				area.MappedPointsY[i] = _endY - coefficient * (_endY - _startY) * percentage;
				previous[i] += area.Percentages[j] * area.PosYCoefficient;
			}
		}
		preparePaths();
	}

	private void calculatePercentages() {
		float[] sums = new float[_chartData.getXPoints().length];
		for (Area area : _areas) {
			area.Percentages = new float[sums.length];
			for (int i = 0; i < area.Percentages.length; i++) {
				sums[i] += area.Line.getPoints()[i];
			}
		}

		for (int i = 0; i < sums.length; i++) {
			for (Area area : _areas) {
				area.Percentages[i] = area.Line.getPoints()[i] / sums[i] *100;
			}
		}

		for (Area area : _areas) {
			Log.e(getClass().getSimpleName(), "06.04 " + area.Line.getName() + ": " + area.Percentages[0]);
			Log.e(getClass().getSimpleName(), "05.04 " + area.Line.getName() + ": " + area.Percentages[1]);
		}
	}

	private float getMaxPosYCoefficient() {
		float max = 0;
		for (Area area : _areas) {
			if (area.PosYCoefficient > max)
				max = area.PosYCoefficient;
		}
		return max;
	}

	private Canvas drawAreas(Canvas canvas) {
		for (Area area : _areas) {
			_areaPaint.setColor(area.Line.getColor());
			canvas.drawPath(area.Path, _areaPaint);
		}

		return canvas;
	}
}
