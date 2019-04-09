package com.teplyakova.april.telegramcontest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.Interfaces.SliderObservable;
import com.teplyakova.april.telegramcontest.Interfaces.SliderObserver;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class ChartView extends View implements SliderObserver, ValueAnimator.AnimatorUpdateListener {

    private final int   DRAWING_AREA_OFFSET_X_DP = 8;
    private final int   DRAWING_AREA_OFFSET_Y_DP = 16;
    private final float mDrawingAreaOffsetXPx;
    private final float mDrawingAreaOffsetYPx;

    private ChartDrawer mDrawer;

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mDrawingAreaOffsetXPx = MathUtils.dpToPixels(DRAWING_AREA_OFFSET_X_DP, context);
        mDrawingAreaOffsetYPx = MathUtils.dpToPixels(DRAWING_AREA_OFFSET_Y_DP, context);
    }

    public void init(ChartDrawer drawer, SliderObservable observable) {
        mDrawer = drawer;
        observable.registerObserver(this);
        mDrawer.setAnimatorUpdateListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int viewWidth  = getWidth();
        int viewHeight = getHeight();
        float drawingAreaStartX = mDrawingAreaOffsetXPx;
        float drawingAreaEndX   = viewWidth - mDrawingAreaOffsetXPx;
        float drawingAreaStartY = mDrawingAreaOffsetYPx;
        float drawingAreaEndY   = viewHeight - mDrawingAreaOffsetYPx;

        mDrawer.setViewDimens(viewWidth, viewHeight, drawingAreaStartX, drawingAreaEndX, drawingAreaStartY, drawingAreaEndY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawer.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                mDrawer.handleTouchEvent(event, x, y);
                invalidate();
                return true;
        }
        return true;
    }

    @Override
    public void setBorders(float normPos1, float normPos2) {
        if (mDrawer.setBorders(normPos1, normPos2))
            invalidate();
    }

    public void setLines(LineData[] lines) {
        mDrawer.setLines(lines);
        invalidate();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }
}
