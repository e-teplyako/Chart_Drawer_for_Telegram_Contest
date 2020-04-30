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
    int getChosenPointPosition();
    void setChosenPointPosition(int pointPosition);
    void setSliderPositions(float pos1, float pos2);
    void setAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener);
    void setPlateFillColor(int color);
    void setPrimaryBgColor(int color);
    void setSliderBgColor(int color);
    void setSliderHandlerColor(int color);
    void setDividerColor(int color);
    void setMainTextColor(int color);
    void setLabelColor(int color);
    void setOpaquePlateColor(int color);
}
