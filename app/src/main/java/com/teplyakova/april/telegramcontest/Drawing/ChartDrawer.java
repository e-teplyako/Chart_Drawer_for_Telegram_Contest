package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;

import com.teplyakova.april.telegramcontest.UI.ThemedDrawer;

public interface ChartDrawer extends ThemedDrawer {
	Canvas draw(Canvas canvas);
	Canvas drawChartForGlobalRange(Canvas canvas);
	void drawChosenPointHighlight(Canvas canvas, int index);
	void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener);
	void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx);
	void setLinesAndAnimate(ValueAnimator.AnimatorUpdateListener listener);
	boolean isInSetLinesTransition();
	int getTouchedPointIndex(float x);
	float getTouchedPointPosition(int index);
}
