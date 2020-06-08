package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class StackedAreaChartDrawer implements ChartDrawer {
	private ChartData _chartData;
	private long _firstVisibleValue;
	private long _lastVisibleValue;
	private int _firstVisibleIndex;
	private int _lastVisibleIndex;

	private float[] _mappedXPoints;
	private float _chartAreaWidthPx;
	private float _chartAreaWidthMarginPx;
	private float _endY;
	private float _startY;

	private Paint _areaPaint;
	private HashSet<Bar> _areas = new LinkedHashSet<>();

	public StackedAreaChartDrawer(ChartData chartData) {
		_chartData = chartData;

		_firstVisibleValue = chartData.getXPoints()[0];
		_lastVisibleValue = chartData.getXPoints()[chartData.getXPoints().length - 1];
		_firstVisibleIndex = 0;
		_lastVisibleIndex = chartData.getXPoints().length - 1;

		for (LineData lineData : chartData.getLines())
		{
			Bar area = new Bar();
			area.Line = lineData;
			area.MappedPointsY = new float[lineData.getPoints().length];
			area.PosYCoefficient = 1;
			_areas.add(area);
		}
		setupPaint();
	}

	@Override
	public Canvas draw(Canvas canvas) {
		return null;
	}

	@Override
	public void drawChosenPointHighlight(Canvas canvas, int index) {

	}

	@Override
	public void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener) {

	}

	@Override
	public void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx) {

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
		return 0;
	}

	@Override
	public float getTouchedPointPosition(int index) {
		return 0;
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
}
