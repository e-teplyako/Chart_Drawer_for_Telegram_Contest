package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;

public interface ChartDrawer {
	Canvas draw(Canvas canvas);
	void drawChosenPointHighlight(Canvas canvas, int index);
	void setRangeAndAnimate(float start, float end, ValueAnimator.AnimatorUpdateListener listener);
	void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx);
	void setLinesAndAnimate(ValueAnimator.AnimatorUpdateListener listener);
	int getTouchedPointIndex(float x);
	float getTouchedPointPosition(int index);
}
