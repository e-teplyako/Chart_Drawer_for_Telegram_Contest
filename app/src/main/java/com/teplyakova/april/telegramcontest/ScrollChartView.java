package com.teplyakova.april.telegramcontest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.teplyakova.april.telegramcontest.Interfaces.SliderObservable;
import com.teplyakova.april.telegramcontest.Interfaces.SliderObserver;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class ScrollChartView extends View implements SliderObservable{

    public final static float MINIMAL_NORM_SLIDER_WIDTH = 0.2f;

    private final static int SLIDER_WIDTH_DP = 6;

    private final float mOptimizeTolerancePx;

    private ArrayList<SliderObserver> mObservers;

    private Context mContext;
    private Resources.Theme mTheme;

    private long[] mPosX;
    private LineData[] mLines;
    private long mDefaultYMax;
    private long mDefaultYMin;

    private float mSliderPositionLeft;
    private float mSliderPositionRight;
    private float mNormSliderPosLeft = 0.8f;
    private float mNormSliderPosRight = 1;

    private RectF mBackGroundLeft;
    private RectF mBackgroundRight;
    private RectF mSliderLeft;
    private RectF mSliderRight;
    private RectF mChosenArea;
    private final float mSliderWidthPx;

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

    private boolean mSizesChanged;

    private HashMap<LineData, ArrayList<Float>> mLineDataToPointsX = new HashMap<>();
    private HashMap<LineData, ArrayList<Float>> mLineDataToPointsY = new HashMap<>();

    public ScrollChartView(Context context, @Nullable AttributeSet attrs) {
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

        mSliderWidthPx = MathUtils.dpToPixels(SLIDER_WIDTH_DP, context);

        mOptimizeTolerancePx = MathUtils.dpToPixels(4, context);
    }

    public void init (ChartData chartData) {
        mPosX = chartData.posX;
    }

    public void setLines (LineData[] lines) {
        mLines = lines;
        invalidate();

        if (mLines != null && mLines.length != 0) {
            mDefaultYMax = MathUtils.getMaxY(lines);
            mDefaultYMin = MathUtils.getMinY(lines);
        }

        calculatePoints();
    }

    private void calculatePoints() {
        if (mLines == null || mLines.length == 0 || !mSizesChanged)
            return;

        float[] mappedX = mapXPoints(mPosX);

        for (LineData line : mLines)
        {
            float[] mappedY = mapYPoints(line.posY, mDefaultYMin, mDefaultYMax);
            ArrayList<Float> optimizedX = new ArrayList<Float>();
            ArrayList<Float> optimizedY = new ArrayList<Float>();

            MathUtils.optimizePoints(mappedX, mappedY, 0, optimizedX, optimizedY);

            mLineDataToPointsX.put(line, optimizedX);
            mLineDataToPointsY.put(line, optimizedY);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSizesChanged = true;

        mViewWidth = getWidth();
        mViewHeight = getHeight();

        mChosenAreaMinimalWidth = mViewWidth * MINIMAL_NORM_SLIDER_WIDTH;
        
        if (mNormSliderPosLeft <=0 && mNormSliderPosRight <= 0) {
            mNormSliderPosLeft = 0.8f;
            mNormSliderPosRight = 1;
        }
        float pos1 = mNormSliderPosLeft * mViewWidth;
        float pos2 = mNormSliderPosRight * mViewWidth;
        setSliderPositions(pos1, pos2);

        calculatePoints();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLines != null && mPosX != null && mLines.length != 0) {
            for (LineData line : mLines)
                if (mLineDataToPointsX.containsKey(line))
                    drawLine(mLineDataToPointsX.get(line), mLineDataToPointsY.get(line), line.color, 255, canvas);
        }

        drawRects(canvas);
    }

    private void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.STROKE);
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
        right = left + mSliderWidthPx;
        mSliderLeft.set(left, top, right, bottom);

        left = mSliderPositionRight - mSliderWidthPx;
        right = mSliderPositionRight;
        mSliderRight.set(left, top, right, bottom);

        left = mSliderPositionLeft + mSliderWidthPx;
        right = mSliderPositionRight - mSliderWidthPx;
        mChosenArea.set(left, top, right, bottom);
    }

    private void drawLine (ArrayList<Float> pointsX, ArrayList<Float> pointsY, int color, int alpha, Canvas canvas) {
        mChartPaint.setColor(color);
        mChartPaint.setAlpha(alpha);

        float[] drawingPoints = MathUtils.concatArraysForDrawing(pointsX.toArray(new Float[pointsX.size()]), pointsY.toArray(new Float[pointsY.size()]));
        if (drawingPoints != null) {
            canvas.drawLines(drawingPoints, mChartPaint);
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
        if (mSliderPositionLeft != pos1 || mSliderPositionRight != pos2) {
            invalidate();
        }

        mSliderPositionLeft = pos1;
        mSliderPositionRight = pos2;
        mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;

        calculateRects();

        if (mSizesChanged) {
            mNormSliderPosLeft = mSliderPositionLeft / mViewWidth;
            mNormSliderPosRight = mSliderPositionRight / mViewWidth;
            notifyObservers();
        }

        Log.e("Slider positions: ", "left - " + String.valueOf(mSliderPositionLeft) + " right: " +String.valueOf(mSliderPositionRight));
        Log.e("Norm positions: ", "left - " + String.valueOf(mNormSliderPosLeft) + " right - " + String.valueOf(mNormSliderPosRight));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if ((x >= mSliderPositionLeft - 3f * mSliderWidthPx) && (x <= mSliderPositionLeft + mCurrChosenAreaWidth * 0.1f)) {
                    mLeftSliderIsCaught = true;
                }
                else if ((x >= mSliderPositionRight - mCurrChosenAreaWidth * 0.1f) &&(x <= mSliderPositionRight + 3f * mSliderWidthPx)) {
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

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable outState = super.onSaveInstanceState();
        SavedState ss = new SavedState(outState);
        if (mSizesChanged) {
            ss.normPos1 = mNormSliderPosLeft;
            ss.normPos2 = mNormSliderPosRight;
        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mNormSliderPosLeft = ss.normPos1;
        mNormSliderPosRight = ss.normPos2;
    }

    private static class SavedState extends BaseSavedState {
        float normPos1;
        float normPos2;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            normPos1 = in.readFloat();
            normPos2 = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(normPos1);
            out.writeFloat(normPos2);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
