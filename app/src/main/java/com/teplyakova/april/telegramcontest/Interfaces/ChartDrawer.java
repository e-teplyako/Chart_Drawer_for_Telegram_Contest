package com.teplyakova.april.telegramcontest.Interfaces;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.LineData;

public interface ChartDrawer{
    void draw(Canvas canvas);
    void setViewDimens(float width, float height, float drawingAreaStartX, float drawingAreaEndX, float drawingAreaStartY, float drawingAreaEndY);
    void handleTouchEvent(MotionEvent event, float x, float y);
    void setLines(LineData[] lines);
    boolean setBorders(float normPos1, float normPos2);
    void setAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener);
}
