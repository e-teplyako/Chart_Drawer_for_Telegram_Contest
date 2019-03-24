package com.example.android.telegramcontest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.telegramcontest.Interfaces.SliderObservable;
import com.example.android.telegramcontest.Interfaces.SliderObserver;
import com.example.android.telegramcontest.Interfaces.WidthObserver;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class ScrollChartView2 extends View implements SliderObservable{

    public final static float MINIMAL_NORM_SLIDER_WIDTH = 0.2f;

    ArrayList<SliderObserver> mObservers;

    private Context mContext;
    private Resources.Theme mTheme;

    private long[] mPosX;
    private LineData[] mLines;
    private long mDefaultYMax;
    private long mDefaultYMin;

    private float mSliderPositionLeft;
    private float mSliderPositionRight;

    private RectF mBackGroundLeft;
    private RectF mBackgroundRight;
    private RectF mSliderLeft;
    private RectF mSliderRight;
    private RectF mChosenArea;
    private float mSliderWidth;

    private boolean mLeftSliderIsCaught = false;
    private boolean mRightSliderIsCaught = false;
    private boolean mChosenAreaIsCaught = false;
    private float mCurrChosenAreaPosition;
    private float mCurrChosenAreaWidth;
    private float mChosenAreaMinimalWidth;

    private float mViewWidth;
    private float mViewHeight;

    private Paint mChartPaint;
    private Paint mBackgroundPaint;
    private Paint mSliderPaint;
    private Paint mChosenPaint;

    public ScrollChartView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTheme = mContext.getTheme();

        mObservers = new ArrayList<>();

        setUpPaints();

        mBackGroundLeft = new RectF();
        mBackgroundRight = new RectF();
        mSliderLeft = new RectF();
        mSliderRight = new RectF();
        mChosenArea = new RectF();
    }

    public void init (ChartData chartData) {
        mPosX = chartData.posX;
    }

    public void setLines (LineData[] lines) {
        if (mLines == null || !Arrays.equals(mLines, lines))
            invalidate();

        mLines = lines;

        if (mLines != null && mLines.length != 0) {
            mDefaultYMax = MathUtils.getMax(lines);
            mDefaultYMin = MathUtils.getMin(lines);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getWidth();
        mViewHeight = getHeight();

        mSliderWidth = mViewWidth * 0.02f;
        mChosenAreaMinimalWidth = mViewWidth * MINIMAL_NORM_SLIDER_WIDTH;

        float defaultSliderPosLeft = mViewWidth * (1 - MINIMAL_NORM_SLIDER_WIDTH);
        float defaultSliderPosRight = mViewWidth;
        setSliderPositions(defaultSliderPosLeft, defaultSliderPosRight);

        calculateRects();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLines != null && mPosX != null && mLines.length != 0) {
            for (LineData line : mLines)
                drawLine(line, mDefaultYMin, mDefaultYMax, 255, canvas);
        }

        drawRects(canvas);
    }

    private void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(4);

        mBackgroundPaint = new Paint();
        TypedValue backgroundColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.chartScrollViewBackgroundColor, backgroundColor, true)) {
            mBackgroundPaint.setColor(backgroundColor.data);
        }

        mSliderPaint = new Paint();
        TypedValue sliderColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.sliderColor, sliderColor, true)) {
            mSliderPaint.setColor(sliderColor.data);
        }
        mSliderPaint.setStyle(Paint.Style.FILL);

        mChosenPaint = new Paint();
        mChosenPaint.setColor(sliderColor.data);
        mChosenPaint.setStyle(Paint.Style.STROKE);
        mChosenPaint.setStrokeWidth(10);
    }

    private void calculateRects() {
        float top = 0;
        float bottom = mViewHeight;
        float left;
        float right;

        left = 0;
        right = mSliderPositionLeft;
        mBackGroundLeft.set(left, top, right, bottom);

        left = mSliderPositionRight;
        right = mViewWidth;
        mBackgroundRight.set(left, top, right, bottom);

        left = mSliderPositionLeft;
        right = left + mSliderWidth;
        mSliderLeft.set(left, top, right, bottom);

        left = mSliderPositionRight - mSliderWidth;
        right = mSliderPositionRight;
        mSliderRight.set(left, top, right, bottom);

        left = mSliderPositionLeft + mSliderWidth;
        right = mSliderPositionRight - mSliderWidth;
        mChosenArea.set(left, top, right, bottom);
    }

    private void drawLine (LineData line, long yMin, long yMax, int alpha, Canvas canvas) {
        float[] mappedX = mapXPoints(mPosX);
        float[] mappedY = mapYPoints(line.posY, yMin, yMax);

        mChartPaint.setColor(line.color);
        mChartPaint.setAlpha(alpha);

        for (int i = 0; i < line.posY.length - 1; i++){
            canvas.drawLine(mappedX[i], mappedY[i], mappedX[i+1], mappedY[i+1], mChartPaint);
        }
    }

    private void drawRects(Canvas canvas) {
        canvas.drawRect(mBackGroundLeft, mBackgroundPaint);
        canvas.drawRect(mBackgroundRight, mBackgroundPaint);
        canvas.drawRect(mSliderLeft, mSliderPaint);
        canvas.drawRect(mSliderRight, mSliderPaint);
        canvas.drawRect(mChosenArea, mChosenPaint);
    }

    private float[] mapYPoints (long[] points, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[] mapped = new float[points.length];

        for (int i = 0; i < points.length; i++) {
            float percentage = (float) (points[i] - yMin) / (float) calculatedArea;
            mapped[i] = mViewHeight * percentage;
            mapped[i] = mViewHeight - mapped[i];
        }

        return mapped;
    }

    private float[] mapXPoints (long[] points) {
        long xMax = MathUtils.getMax(mPosX);
        long xMin = MathUtils.getMin(mPosX);
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[points.length];
        for (int i = 0; i < mapped.length; i++) {
            float percentage = (float) (points[i] - xMin) / (float) calculatedArea;
            mapped[i] = mViewWidth * percentage;
        }

        return mapped;
    }

    private void setSliderPositions (float pos1, float pos2) {
        if (mSliderPositionLeft != pos1 || mSliderPositionRight != pos2)
            invalidate();

        mSliderPositionLeft = pos1;
        mSliderPositionRight = pos2;
        mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;

        calculateRects();

        notifyObservers();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if ((x >= mSliderPositionLeft - 3f * mSliderWidth) && (x <= mSliderPositionLeft + mCurrChosenAreaWidth * 0.1f)) {
                    mLeftSliderIsCaught = true;
                }
                else if ((x >= mSliderPositionRight - mCurrChosenAreaWidth * 0.1f) &&(x <= mSliderPositionRight + 3f * mSliderWidth)) {
                    mRightSliderIsCaught = true;
                }
                else if (mChosenArea.contains(x, y)) {
                    mChosenAreaIsCaught = true;
                    mCurrChosenAreaPosition = x;
                    mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if (mLeftSliderIsCaught){
                    setSliderPositions(MathUtils.clamp(x, mSliderPositionRight - mChosenAreaMinimalWidth, 0f), mSliderPositionRight);
                }
                else if (mRightSliderIsCaught) {
                    setSliderPositions(mSliderPositionLeft, MathUtils.clamp(x, mViewWidth, mSliderPositionLeft + mChosenAreaMinimalWidth));
                }
                else if (mChosenAreaIsCaught) {
                    float deltaX = x - mCurrChosenAreaPosition;
                    setSliderPositions(MathUtils.clamp(mSliderPositionLeft + deltaX, mViewWidth - mCurrChosenAreaWidth, 0f), MathUtils.clamp(mSliderPositionRight + deltaX, mViewWidth, mCurrChosenAreaWidth));
                    mCurrChosenAreaPosition = x;
                }

                return true;

            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                mRightSliderIsCaught = false;
                mLeftSliderIsCaught = false;
                mChosenAreaIsCaught = false;
                return true;
        }
        return false;
    }

    @Override
    public void registerObserver(SliderObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(SliderObserver observer) {
        int i = mObservers.indexOf(observer);
        if (i >= 0) {
            mObservers.remove(i);
        }

    }

    @Override
    public void notifyObservers() {
        float normPos1 = mSliderPositionLeft / mViewWidth;
        float normPos2 = mSliderPositionRight / mViewWidth;

        for (int i = 0; i < mObservers.size(); i++) {
            SliderObserver observer = mObservers.get(i);
            observer.setBorders(normPos1, normPos2);
        }
    }
}
