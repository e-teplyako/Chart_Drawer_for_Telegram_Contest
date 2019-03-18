package com.example.android.telegramcontest;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ScrollView;

public class ScrollChartView extends View {

    private final String LOG_TAG = ScrollView.class.getSimpleName();

    private Paint mChartPaint;
    private final int CHART_STROKE_WIDTH = 4;

    private Paint mBackgroundPaint;
    private final int BACKGROUND_COLOR = Color.parseColor("#CCEEEEEE");
    private Paint mHighlightedPaint;
    private Paint mSliderPaint;
    private final int HIGHLIGHTED_BORDER_COLOR = Color.parseColor("#66BDBDBD");
    private final int HIGHLIGHTED_BORDER_STROKE_WIDTH = 10;

    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;

    private long[] mXPoints;
    private long[][] mYPoints;
    private String[] mColors;

    private float mHighlightedAreaLeftBorder;
    private float mHighlightedAreaRightBorder;
    private RectF mBackgroundRectLeft;
    private RectF mBackgroundRectRight;
    private RectF mHighlightedRect;
    private RectF mSliderLeft;
    private RectF mSliderRight;
    private float mSliderWidth;

    private boolean mLeftSliderIsCaught;
    private boolean mRightSliderIsCaught;
    private boolean mHighlightedAreaIsCaught;
    private float mHighlightedAreaMinimalWidth;
    private float mCurrentHighlightedAreaPosition;
    private float mCurrentHighlightedAreaWidth;



    public ScrollChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    private void init(){
        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(CHART_STROKE_WIDTH);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(BACKGROUND_COLOR);

        mHighlightedPaint = new Paint();
        mHighlightedPaint.setColor(HIGHLIGHTED_BORDER_COLOR);
        mHighlightedPaint.setStyle(Paint.Style.STROKE);
        mHighlightedPaint.setStrokeWidth(HIGHLIGHTED_BORDER_STROKE_WIDTH);

        mSliderPaint = new Paint();
        mSliderPaint.setColor(HIGHLIGHTED_BORDER_COLOR);
        mSliderPaint.setStyle(Paint.Style.FILL);

        mBackgroundRectLeft = new RectF();
        mBackgroundRectRight = new RectF();
        mHighlightedRect = new RectF();
        mSliderLeft = new RectF();
        mSliderRight = new RectF();

       mLeftSliderIsCaught = false;
       mRightSliderIsCaught = false;
       mHighlightedAreaIsCaught = false;
    }

    public void setChartParams(long[] xPts, long[][] yPts, String[] colors) {
        mXPoints = xPts;
        mYPoints = yPts;
        mColors = colors;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawingAreaWidth = getWidth();
        mDrawingAreaHeight = getHeight();
        mHighlightedAreaLeftBorder = mDrawingAreaWidth - (mDrawingAreaWidth * 0.3f);
        mHighlightedAreaRightBorder = mDrawingAreaWidth;
        mSliderWidth = mDrawingAreaWidth * 0.02f;
        mHighlightedAreaMinimalWidth = mDrawingAreaWidth * 0.2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mXPoints != null && mYPoints != null) {
            drawChart(canvas);
        }
        mBackgroundRectLeft.set(0f, 0f, mHighlightedAreaLeftBorder, mDrawingAreaHeight);
        mBackgroundRectRight.set(mHighlightedAreaRightBorder, 0f, mDrawingAreaWidth, mDrawingAreaHeight);
        mHighlightedRect.set(mHighlightedAreaLeftBorder + mSliderWidth, 0f, mHighlightedAreaRightBorder - mSliderWidth, mDrawingAreaHeight);
        mSliderLeft.set(mHighlightedAreaLeftBorder, 0f, mHighlightedAreaLeftBorder + mSliderWidth, mDrawingAreaHeight);
        mSliderRight.set(mHighlightedAreaRightBorder - mSliderWidth, 0f, mHighlightedAreaRightBorder, mDrawingAreaHeight);
        canvas.drawRect(mBackgroundRectLeft, mBackgroundPaint);
        canvas.drawRect(mBackgroundRectRight, mBackgroundPaint);
        canvas.drawRect(mHighlightedRect, mHighlightedPaint);
        canvas.drawRect(mSliderLeft, mSliderPaint);
        canvas.drawRect(mSliderRight, mSliderPaint);
    }

    private void drawChart (Canvas canvas) {
        if (mXPoints == null || mYPoints == null) return;

        mChartPaint.setColor(Color.RED);

        long maxX = MathUtils.getMax(mXPoints);
        long minX = MathUtils.getMin(mXPoints);
        long maxY = MathUtils.getMax(mYPoints);
        long minY = MathUtils.getMin(mYPoints);

        float[] mappedX = mapXPoints(mXPoints, minX, maxX);

        for (int i = 0; i < mYPoints.length; i++) {
            float[] mappedY = mapYPoints(mYPoints[i], minY, maxY);
            mChartPaint.setColor(Color.parseColor(mColors[i]));
            for (int j = 0; j < mappedY.length - 1; j++){
                canvas.drawLine(mappedX[j], mappedY[j], mappedX[j+1], mappedY[j+1], mChartPaint);
            }
        }
    }

    private float[] mapXPoints (long[] xPts, long min, long max) {
        long calculatedArea = max - min;
        float[] mapped = new float[xPts.length];
        for (int i = 0; i < xPts.length; i++) {
            float percentage = (float)(xPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaWidth * percentage;
        }
        return mapped;
    }

    private float[] mapYPoints (long[] yPts, long min, long max) {
        long calculatedArea = max - min;
        float[] mapped = new float[yPts.length];
        for (int i = 0; i < yPts.length; i++) {
            float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaHeight * percentage;
            mapped[i] = mDrawingAreaHeight - mapped[i];
        }
        return mapped;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(LOG_TAG, "X: " +  String.valueOf(x));
                if ((x >= mHighlightedAreaLeftBorder - 3f * mSliderWidth) && (x <= mHighlightedAreaLeftBorder + 3f * mSliderWidth)) {
                    mLeftSliderIsCaught = true;
                }
                else if ((x >= mHighlightedAreaRightBorder - 3f * mSliderWidth) &&(x <= mHighlightedAreaRightBorder + 3f * mSliderWidth)) {
                    mRightSliderIsCaught = true;
                }
                else if (mHighlightedRect.contains(x, y)) {
                    mHighlightedAreaIsCaught = true;
                    mCurrentHighlightedAreaPosition = x;
                    mCurrentHighlightedAreaWidth = mHighlightedAreaRightBorder - mHighlightedAreaLeftBorder;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mLeftSliderIsCaught){
                    mHighlightedAreaLeftBorder = MathUtils.clamp(x,mHighlightedAreaRightBorder - mHighlightedAreaMinimalWidth, 0f);
                    invalidate();
                }
                else if (mRightSliderIsCaught) {
                    mHighlightedAreaRightBorder = MathUtils.clamp(x, mDrawingAreaWidth, mHighlightedAreaLeftBorder + mHighlightedAreaMinimalWidth);
                    invalidate();
                }
                else if (mHighlightedAreaIsCaught) {
                    float deltaX = x - mCurrentHighlightedAreaPosition;
                    mHighlightedAreaRightBorder = MathUtils.clamp(mHighlightedAreaRightBorder + deltaX, mDrawingAreaWidth, mCurrentHighlightedAreaWidth);
                    mHighlightedAreaLeftBorder = MathUtils.clamp(mHighlightedAreaLeftBorder + deltaX, mDrawingAreaWidth - mCurrentHighlightedAreaWidth, 0f);
                    mCurrentHighlightedAreaPosition = x;
                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:
                mRightSliderIsCaught = false;
                mLeftSliderIsCaught = false;
                mHighlightedAreaIsCaught = false;
                return true;
        }
        return false;
    }
}
