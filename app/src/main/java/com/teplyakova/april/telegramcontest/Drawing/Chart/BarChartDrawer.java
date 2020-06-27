package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.teplyakova.april.telegramcontest.Animators.LocalYMinMaxAnimator;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class BarChartDrawer implements ChartDrawer, ValueAnimator.AnimatorUpdateListener {
	private ChartData _chartData;

	private int _minVisibleIndex;
	private int _maxVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;
	private float _endY;
	private float _startY;

	private Paint _barPaint;
	private Paint _highlightPaint;
	private RectF _highlightRect;

	private HashSet<Bar> _bars = new LinkedHashSet<>();

	private int _localYMax;

	public BarChartDrawer(ChartData chartData) {
		_chartData = chartData;

		_minVisibleIndex = 0;
		_maxVisibleIndex = chartData.getXPoints().length - 1;

		for (LineData lineData : chartData.getLines())
		{
			Bar bar = new Bar();
			bar.Line = lineData;
			bar.MappedPointsY = new float[lineData.getPoints().length];
			bar.PosYCoefficient = 1;
			_bars.add(bar);
		}
		_localYMax = MathUtils.getMaxYForStackedChart(_chartData.getActiveLines(), _minVisibleIndex, _maxVisibleIndex);
		setupPaint();
		_highlightRect = new RectF();
		setRangeAndAnimate(0f, 1f, null);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (animation.getAnimatedValue(LocalYMinMaxAnimator.MAX) != null)
			_localYMax = (int) animation.getAnimatedValue(LocalYMinMaxAnimator.MAX);
		if (_mappedXPoints != null)
			mapYPoints(getMaxPosYCoefficient(), _localYMax);
	}

	@Override
	public Canvas draw(Canvas canvas) {
		return drawBars(canvas);
	}

	@Override
	public void drawChosenPointHighlight(Canvas canvas, int index) {
		float halfBarWidth = (_mappedXPoints[_mappedXPoints.length - 1] - _mappedXPoints[0]) / (_mappedXPoints.length - 1) / 2;
		_highlightRect.set(0f, _startY, _mappedXPoints[index - _minVisibleIndex] - halfBarWidth, canvas.getWidth());
		canvas.drawRect(_highlightRect, _highlightPaint);
		_highlightRect.set(_mappedXPoints[index - _minVisibleIndex] + halfBarWidth, _startY, canvas.getWidth(), _endY);
		canvas.drawRect(_highlightRect, _highlightPaint);
	}

	@Override
	public void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener) {
		long width = _chartData.getXPoints()[_chartData.getXPoints().length - 1] - _chartData.getXPoints()[0];
		long startPos = (long) Math.floor(start * width) + _chartData.getXPoints()[0];
		long endPos = (long) Math.ceil(end * width) + _chartData.getXPoints()[0];

		long distanceToScreenBorder = (long) Math.ceil (((endPos - startPos) * _chartAreaWidthMarginPx) / _chartAreaWidthPx);

		_minVisibleIndex = MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(), startPos - distanceToScreenBorder);
		_maxVisibleIndex = MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(),  endPos + distanceToScreenBorder);

		_mappedXPoints  = mapXPoints(startPos, endPos);
		mapYPoints(getMaxPosYCoefficient(), _localYMax);
		setMaxYAndAnimate(listener);
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
		for (Bar bar : _bars) {
			float endCoeff = (_chartData.isLineActive(bar.Line) ? 1f : 0f);
			bar.Animator.start(bar, bar.PosYCoefficient, endCoeff, listener, this);
		}
		setMaxYAndAnimate(listener);
	}

	@Override
	public boolean isInSetLinesTransition() {
		boolean isinTransition = false;
		for (Bar bar : _bars) {
			if (bar.Animator.isRunning())
				isinTransition = true;
		}
		return isinTransition;
	}

	@Override
	public int getTouchedPointIndex(float x) {
		int position = MathUtils.getIndexOfNearestElement(_mappedXPoints, x);
		while (_chartData.getXPoints()[position + _minVisibleIndex] < _chartData.getXPoints()[0]) {
			position++;
		}
		while (_chartData.getXPoints()[position + _minVisibleIndex] > _chartData.getXPoints()[_chartData.getXPoints().length - 1]) {
			position--;
		}
		return position + _minVisibleIndex;
	}

	@Override
	public float getTouchedPointPosition(int index) {
		return _mappedXPoints[index - _minVisibleIndex];
	}

	@Override
	public void setAntiAlias(boolean antiAlias) {
		_barPaint.setAntiAlias(antiAlias);
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
		_highlightPaint.setColor(color);
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
			bar.MappedPointsY = new float[_maxVisibleIndex - _minVisibleIndex + 1];
			for (int i = 0, j = _minVisibleIndex; i < bar.MappedPointsY.length; i++, j++) {
				float percentage = (bar.Line.getPoints()[j] * bar.PosYCoefficient + previous[i]) / yMax;
				bar.MappedPointsY[i] = _endY - coefficient * (_endY - _startY) * percentage;
				previous[i] += bar.Line.getPoints()[j] * bar.PosYCoefficient;
			}
		}
		preparePaths();
	}

	private Canvas drawBars(Canvas canvas) {
		if (_bars == null || _bars.size() == 0)
			return canvas;

		for (Bar bar : _bars) {
			_barPaint.setColor(bar.Line.getColor());
			canvas.drawPath(bar.Path, _barPaint);
		}

		return canvas;
	}

	private void preparePaths() {
		float halfBarWidth = (_mappedXPoints[_mappedXPoints.length - 1] - _mappedXPoints[0]) / (_mappedXPoints.length - 1) / 2;
		float startX = _mappedXPoints[0] - halfBarWidth;
		float startY = _endY;

		float[] previous = new float[_mappedXPoints.length];
		Arrays.fill(previous, _endY);
		Path path = new Path();
		path.moveTo(startX, startY);
		for (Bar bar : _bars) {
			for (int j = 0; j < bar.MappedPointsY.length; j++) {
				path.lineTo(_mappedXPoints[j] - halfBarWidth, bar.MappedPointsY[j]);
				path.lineTo(_mappedXPoints[j] + halfBarWidth, bar.MappedPointsY[j]);
			}
			for (int n = previous.length - 1; n >= 0; n--) {
				path.lineTo(_mappedXPoints[n] + halfBarWidth, previous[n]);
				path.lineTo(_mappedXPoints[n] - halfBarWidth, previous[n]);
				previous[n] = bar.MappedPointsY[n];
			}
			bar.Path = path;
			path = new Path();
			path.moveTo(_mappedXPoints[0] - halfBarWidth, bar.MappedPointsY[0]);
		}
	}

	private float getMaxPosYCoefficient() {
		float max = 0;
		for (Bar bar : _bars) {
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

	private void setMaxYAndAnimate(ValueAnimator.AnimatorUpdateListener listener) {
		int max = MathUtils.getMaxYForStackedChart(_chartData.getActiveLines(), _minVisibleIndex, _maxVisibleIndex);
		LocalYMinMaxAnimator yMaxAnimator = new LocalYMinMaxAnimator();
		yMaxAnimator.start(0, 0, _localYMax, max, listener, this);
	}
}
