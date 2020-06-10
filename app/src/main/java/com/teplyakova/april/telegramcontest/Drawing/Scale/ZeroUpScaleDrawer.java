package com.teplyakova.april.telegramcontest.Drawing.Scale;

import android.animation.ValueAnimator;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class ZeroUpScaleDrawer extends StandardScaleDrawer {

	public ZeroUpScaleDrawer(ChartData chartData) {
		_chartData = chartData;
		setMinValue(0);
		setMaxValue(MathUtils.getLocalMax(_chartData.getActiveLines(), 0, _chartData.getXPoints().length - 1));
		setUpPaint();
	}

	@Override
	public void setLinesAndAnimate(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		setMinMaxValueAndAnimate(0,
				MathUtils.getMaxYForStackedChart(_chartData.getActiveLines(), firstVisibleIndex, lastVisibleIndex), listener);
	}

	@Override
	public void chosenAreaChanged(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		setMinMaxValueAndAnimate(0,
				MathUtils.getMaxYForStackedChart(_chartData.getActiveLines(), firstVisibleIndex, lastVisibleIndex), listener);
	}
}
