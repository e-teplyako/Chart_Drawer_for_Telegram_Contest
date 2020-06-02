package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BarChartDrawer implements ChartDrawer, ValueAnimator.AnimatorUpdateListener {
	ChartData _chartData;

	long _minValue;
	long _maxValue;
	int _minVisibleIndex;
	int _maxVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;
	private float _endY;
	private float _startY;
	private Paint _barPaint;
	private Paint _highlightPaint;
	private HashSet<Bar> _bars = new HashSet<>();
	private Path[] _paths;

	public BarChartDrawer(ChartData chartData) {
		_chartData = chartData;

		_minValue = chartData.getXPoints()[0];
		_maxValue = chartData.getXPoints()[chartData.getXPoints().length - 1];
		_minVisibleIndex = 0;
		_maxVisibleIndex = chartData.getXPoints().length - 1;

		for (LineData lineData : chartData.getLines())
		{
			Bar bar = new Bar();
			bar.Line = lineData;
			bar.MappedPointsY = new float[lineData.getPoints().length];
			bar.PosYCoefficient = 1;
			bar.PosYCoefficientStart = 1;
			bar.PosYCoefficientEnd = 1;
			_bars.add(bar);
		}
		setupPaint();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {

	}

	@Override
	public Canvas draw(Canvas canvas) {
		return drawBars(canvas);
	}

	@Override
	public Canvas drawChartForGlobalRange(Canvas canvas) {
		return drawBars(canvas);
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
		mapYPoints(getMaxPosYCoefficient(), MathUtils.getMax(_chartData.getActiveLines()));
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

	private void mapYPoints(float coefficient, long yMax) {
		int[] previous = new int[_maxVisibleIndex - _minVisibleIndex + 1];
		for (Bar bar : _bars) {
			if (bar.isVisible()) {
				bar.MappedPointsY = new float[_maxVisibleIndex - _minVisibleIndex + 1];
				for (int i = 0, j = _minVisibleIndex; i < bar.MappedPointsY.length; i++, j++) {
					float percentage = (bar.Line.getPoints()[j] * bar.PosYCoefficient + previous[i]) / yMax;
					bar.MappedPointsY[i] = _endY - coefficient * (_endY - _startY) * percentage;
					previous[i] += bar.Line.getPoints()[j] * bar.PosYCoefficient;
				}
			}
		}
		preparePaths();
	}

	private Canvas drawBars(Canvas canvas) {
		Bar[] bars = getVisibleBars();

		if (bars == null || bars.length == 0)
			return canvas;

		for (int i = 0; i < _paths.length; i++) {
			_barPaint.setColor(bars[i].Line.getColor());
			canvas.drawPath(_paths[i], _barPaint);
		}

		return canvas;
	}

	private void preparePaths() {
		float halfBarWidth = (_mappedXPoints[_mappedXPoints.length - 1] - _mappedXPoints[0]) / (_mappedXPoints.length - 1) / 2;
		float startX = _mappedXPoints[0] - halfBarWidth;
		float startY = _endY;
		Bar[] bars = getVisibleBars();
		_paths = new Path[bars.length];

		float[] previous = new float[_mappedXPoints.length];
		Arrays.fill(previous, _endY);
		Path path = new Path();
		path.moveTo(startX, startY);
		for (int i = 0; i < bars.length; i++) {
			for (int j = 0; j < bars[i].MappedPointsY.length; j++) {
				path.lineTo(_mappedXPoints[j] - halfBarWidth, bars[i].MappedPointsY[j]);
				path.lineTo(_mappedXPoints[j] + halfBarWidth, bars[i].MappedPointsY[j]);
			}
			for (int n = previous.length - 1; n >= 0; n--) {
				path.lineTo(_mappedXPoints[n] + halfBarWidth, previous[n]);
				path.lineTo(_mappedXPoints[n] - halfBarWidth, previous[n]);
				previous[n] = bars[i].MappedPointsY[n];
			}
			_paths[i] = path;
			path = new Path();
			path.moveTo(_mappedXPoints[0] - halfBarWidth, bars[i].MappedPointsY[0]);
		}
	}

	private Bar[] getVisibleBars() {
		ArrayList<Bar> arrayList = new ArrayList<>();

		for (Bar bar : _bars)
			if (bar.isVisible())
				arrayList.add(bar);

		return arrayList.toArray(new Bar[arrayList.size()]);
	}

	private float getMaxPosYCoefficient() {
		float max = 0;
		Bar[] bars = getVisibleBars();
		for (Bar bar : bars) {
			if (bar.PosYCoefficient > max)
				max = bar.PosYCoefficient;
		}

		return max;
	}

	private void setupPaint() {
		_barPaint = new Paint();
		_barPaint.setStyle(Paint.Style.FILL);
		_barPaint.setStrokeCap(Paint.Cap.SQUARE);
		_barPaint.setAntiAlias(true);

		_highlightPaint = new Paint();
		_highlightPaint.setStyle(Paint.Style.FILL);
	}
}
