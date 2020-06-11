package com.teplyakova.april.telegramcontest.Data;

import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class LocalChartData {
	private ChartData _chartData;

	public LocalChartData(ChartData chartData) {
		_chartData = chartData;
	}

	public int getFirstVisibleIndex(float startRange, float endRange, float chartAreaWidthMarginPx, float chartAreaWidthPx) {
		long maxValue = _chartData.getXPoints()[_chartData.getXPoints().length - 1];
		long minValue = _chartData.getXPoints()[0];
		long width = maxValue - minValue;
		long startPos = (long) Math.floor(startRange * width) + minValue;
		long endPos = (long) Math.ceil(endRange * width) + minValue;

		long distanceToScreenBorder = (long) Math.ceil (((endPos - startPos) * chartAreaWidthMarginPx) / chartAreaWidthPx);

		return MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(), startPos - distanceToScreenBorder);
	}

	public int getLastVisibleIndex(float startRange, float endRange, float chartAreaWidthMarginPx, float chartAreaWidthPx) {
		long maxValue = _chartData.getXPoints()[_chartData.getXPoints().length - 1];
		long minValue = _chartData.getXPoints()[0];
		long width = maxValue - minValue;
		long startPos = (long) Math.floor(startRange * width) + minValue;
		long endPos = (long) Math.ceil(endRange * width) + minValue;

		long distanceToScreenBorder = (long) Math.ceil (((endPos - startPos) * chartAreaWidthMarginPx) / chartAreaWidthPx);

		return  MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(),  endPos + distanceToScreenBorder);
	}

	public int getFirstInRangeIndex(float startRange) {
		long maxValue = _chartData.getXPoints()[_chartData.getXPoints().length - 1];
		long minValue = _chartData.getXPoints()[0];
		long width = maxValue - minValue;
		long startPos = (long) Math.floor(startRange * width) + minValue;
		return MathUtils.getIndexOfNearestRightElement(_chartData.getXPoints(), startPos);
	}

	public int getLastInRangeIndex(float endRange) {
		long maxValue = _chartData.getXPoints()[_chartData.getXPoints().length - 1];
		long minValue = _chartData.getXPoints()[0];
		long width = maxValue - minValue;
		long endPos = (long) Math.ceil(endRange * width) + minValue;
		return  MathUtils.getIndexOfNearestLeftElement(_chartData.getXPoints(),  endPos);
	}
}
