package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.LineData;

public interface ChartDrawer{
    void draw(Canvas canvas);
    void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx);
    boolean handleTouchEvent(MotionEvent event, float x, float y);
    void setLines(LineData[] lines);
    float[] getSliderPositions();
    void setSliderPositions(float pos1, float pos2);
    void setAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener);
}
