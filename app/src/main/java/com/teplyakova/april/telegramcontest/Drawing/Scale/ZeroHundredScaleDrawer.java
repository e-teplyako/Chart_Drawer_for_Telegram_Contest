package com.teplyakova.april.telegramcontest.Drawing.Scale;

import android.animation.ValueAnimator;

import com.teplyakova.april.telegramcontest.Data.ChartData;

public class ZeroHundredScaleDrawer extends StandardScaleDrawer {

	public ZeroHundredScaleDrawer(ChartData chartData) {
		_chartData = chartData;
		setMinValue(0);
		setMaxValue(100);
		setSegmentsNumber(4);
		setUpPaint();
	}

	@Override
	public void setLinesAndAnimate(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {

	}

	@Override
	public void chosenAreaChanged(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {

	}
}
