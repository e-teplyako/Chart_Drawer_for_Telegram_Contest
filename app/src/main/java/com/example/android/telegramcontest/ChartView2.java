package com.example.android.telegramcontest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.telegramcontest.Interfaces.SliderObservable;
import com.example.android.telegramcontest.Interfaces.SliderObserver;
import com.example.android.telegramcontest.Utils.DateTimeUtils;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ChartView2 extends View implements SliderObserver{

    class YScale
    {
        long Height;
        long MaxY;
        int  Alpha;

        long HeightStart;
        long HeightEnd;

        long MaxYStart;
        long MaxYEnd;

        int  AlphaStart;
        int  AlphaEnd;
    }

    final float DRAWING_AREA_OFFSET    = 0.05f;
    final int   Y_DIVIDERS_COUNT       = 6;
    final int   TEXT_SIZE_DP           = 12;
    final int   TEXT_LABEL_WIDTH_DP    = 36;
    final int   TEXT_LABEL_DISTANCE_DP = 22;

    private Resources.Theme mTheme;

    private Paint     mChartPaint;
    private Paint     mDividerPaint;
    private TextPaint mBaseLabelPaint;

    final float mTextSizePx;
    final float mDateWidthPx;
    final float mDateDistancePx;

    private long[] mPosX;
    private long  mPos1 = -1;
    private long  mPos2 = -1;
    float mNormWidth;
    private int mPointsMinIndex;
    private int mPointsMaxIndex;
    private LineData[] mLines;

    private long[] mMappedX;
    private long[][] mMappedY;

    private long          mMaxY       = -1;
    private long          mTargetMaxY = -1;
    private ValueAnimator mMaxYAnimator;

    private ArrayList<YScale> mYScales = new ArrayList<YScale>();

    private float mDrawingAreaStartX;
    private float mDrawingAreaEndX;
    private float mDrawingAreaStartY;
    private float mDrawingAreaEndY;
    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;
    private float mXLabelsYCoordinate;

    private boolean mPointIsChosen = false;
    private float mXCoordinateOfChosenPoint;
    private int mPositionOfChosenPoint;

    private HashMap<Integer, Float> mXLabelsPeriodToMinChartWidthPx = new HashMap<>();
    private int                     mXLabelsPeriodCurrent;

    public ChartView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTheme   = context.getTheme();

        mTextSizePx     = MathUtils.dpToPixels(TEXT_SIZE_DP,           context);
        mDateWidthPx    = MathUtils.dpToPixels(TEXT_LABEL_WIDTH_DP,    context);
        mDateDistancePx = MathUtils.dpToPixels(TEXT_LABEL_DISTANCE_DP, context);

        setUpPaints();
    }

    public void init (ChartData chartData, SliderObservable observable) {
        mPosX = chartData.posX;
        observable.registerObserver(this);
    }

    public void setBorders (float normPosX1, float normPosX2) {
        mNormWidth = normPosX2 - normPosX1;
        long pos1 = 0;
        long pos2 = 0;
        long xMin = MathUtils.getMin(mPosX);
        long xMax = MathUtils.getMax(mPosX);
        long width = xMax - xMin;
        pos1 = (long) Math.floor(normPosX1 * width) + xMin;
        pos2 = (long) Math.ceil(normPosX2 * width) + xMin;

        if (mPos1 != pos1 || mPos2 != pos2)
            invalidate();

        mPos1 = pos1;
        mPos2 = pos2;

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * DRAWING_AREA_OFFSET) / (1 - 2 * DRAWING_AREA_OFFSET));

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        UpdateMaxY();
    }

    public void setLines (LineData[] lines) {
        mLines = lines;
        UpdateMaxY();

        invalidate();
    }

    private void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(6);

        mDividerPaint = new Paint();
        TypedValue dividerColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.dividerColor, dividerColor, true)) {
            mDividerPaint.setColor(dividerColor.data);
        }
        mDividerPaint.setStrokeWidth(2);

        mBaseLabelPaint = new TextPaint();
        mBaseLabelPaint.setTextSize(mTextSizePx);
        TypedValue baseLabelColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.baseLabelColor, baseLabelColor, true)) {
            mBaseLabelPaint.setColor(baseLabelColor.data);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int viewWidth  = getWidth();
        int viewHeight = getHeight();

        mDrawingAreaStartX = viewWidth * DRAWING_AREA_OFFSET;
        mDrawingAreaEndX   = viewWidth * 0.95f;
        mDrawingAreaWidth  = mDrawingAreaEndX - mDrawingAreaStartX;

        mDrawingAreaStartY = viewHeight * DRAWING_AREA_OFFSET;
        mDrawingAreaEndY   = viewHeight * 0.85f;
        mDrawingAreaHeight = mDrawingAreaEndY - mDrawingAreaStartY;

        mXLabelsYCoordinate = viewHeight * 0.9f;

        float minChartWidth = mDrawingAreaWidth;
        float maxChartWidth = minChartWidth / ScrollChartView2.MINIMAL_NORM_SLIDER_WIDTH;
        int sizeOfArray = mPosX.length;
        for (int i = 1; true; i = i * 2) {
            // we take size - 1 cause first point should be labeled
            int textElemsCount = sizeOfArray / i;
            float chartWidth = mDateWidthPx * textElemsCount + mDateDistancePx * (textElemsCount - 1);
            if (chartWidth >= minChartWidth && chartWidth <= maxChartWidth)
                mXLabelsPeriodToMinChartWidthPx.put(i, chartWidth);
            else if (chartWidth < minChartWidth) {
                mXLabelsPeriodToMinChartWidthPx.put(i, minChartWidth);
                break;
            }
        }
    }

    private void UpdateMaxY() {
        if (mLines != null && mLines.length != 0 && mPos1 >= 0 && mPos2 >= 0)
        {
            long newYMax = MathUtils.getMaxY(mLines, mPointsMinIndex, mPointsMaxIndex);
            newYMax = newYMax / Y_DIVIDERS_COUNT * (Y_DIVIDERS_COUNT + 1);

            if (newYMax != mTargetMaxY)
            {
                boolean firstTime = mTargetMaxY < 0;
                mTargetMaxY = newYMax;

                if (firstTime)
                {
                    mMaxY = mTargetMaxY;

                    YScale yScale = new YScale();
                    yScale.Height = mMaxY;
                    yScale.MaxY   = mMaxY;
                    yScale.Alpha  = 255;
                    mYScales.add(yScale);
                }
                else {
                    StartAnimationYMax();
                }
            }
        }
    }

    private void StartAnimationYMax() {
        if (mMaxYAnimator != null)
        {
            mMaxYAnimator.cancel();
            mMaxYAnimator = null;
        }

        for (YScale yScale : mYScales)
        {
            yScale.HeightStart = yScale.Height;
            yScale.HeightEnd   = mTargetMaxY;
            yScale.MaxYStart   = yScale.MaxY;
            yScale.MaxYEnd     = yScale.MaxY;
            yScale.AlphaStart  = yScale.Alpha;
            yScale.AlphaEnd    = 0;
        }

        final YScale newScale = new YScale();
        newScale.Height      = mMaxY;
        newScale.HeightStart = mMaxY;
        newScale.HeightEnd   = mTargetMaxY;
        newScale.MaxY        = mTargetMaxY;
        newScale.MaxYStart   = mTargetMaxY;
        newScale.MaxYEnd     = mTargetMaxY;
        newScale.Alpha       = 0;
        newScale.AlphaStart  = 0;
        newScale.AlphaEnd    = 255;
        mYScales.add(newScale);

        final long startY = mMaxY;
        final long endY   = mTargetMaxY;

        final String KEY_PHASE = "phase";

        mMaxYAnimator = new ValueAnimator();
        mMaxYAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
        mMaxYAnimator.setDuration(400);

        mMaxYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float t = (float) animator.getAnimatedValue(KEY_PHASE);

                mMaxY = (long)MathUtils.lerp(startY, endY, t);

                for (YScale yScale : mYScales)
                {
                    yScale.Height = (long)MathUtils.lerp(yScale.HeightStart, yScale.HeightEnd, t);
                    yScale.MaxY   = (long)MathUtils.lerp(yScale.MaxYStart,   yScale.MaxYEnd,   t);

                    if (yScale.AlphaEnd == 255)
                        yScale.Alpha  = (int) MathUtils.lerp(yScale.AlphaStart,  yScale.AlphaEnd,  MathUtils.clamp(t / 0.45f - 1, 1, 0)); //t * 4 - 3
                    else
                        yScale.Alpha  = (int) MathUtils.lerp(yScale.AlphaStart,  yScale.AlphaEnd,  MathUtils.clamp(t / 0.55f, 1, 0)); // t / 0.75
                }

                invalidate();
            }
        });

        mMaxYAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator)
            {
                ArrayList<YScale> nonZeroScales = new ArrayList<>();

                for (YScale yScale : mYScales)
                    if (yScale.Alpha > 0)
                        nonZeroScales.add(yScale);

                mYScales = nonZeroScales;
            }
        });

        mMaxYAnimator.start();
    }

    private void drawChartLine (LineData line, long yMin, long yMax, int alpha, float[] mappedX, Canvas canvas) {
        float[] mappedY = mapYPoints(line.posY, yMin, yMax);

        mChartPaint.setColor(line.color);
        mChartPaint.setAlpha(alpha);

        for (int i = mPointsMinIndex; i < mPointsMaxIndex; i++){
            canvas.drawLine(mappedX[i], mappedY[i], mappedX[i+1], mappedY[i+1], mChartPaint);
        }
    }

    private float[] mapYPoints (long[] points, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[] mapped = new float[points.length];

            for (int i = 0; i < points.length; i++) {
                float percentage = (float) (points[i] - yMin) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage + mDrawingAreaStartY;
                mapped[i] = mDrawingAreaEndY - mapped[i] + mDrawingAreaStartY;
            }

        return mapped;
    }

    private float[] mapXPoints (long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[points.length];
            for (int i = 0; i < mapped.length; i++) {
                float percentage = (float) (points[i] - xMin) / (float) calculatedArea;
                mapped[i] = mDrawingAreaStartX + mDrawingAreaWidth * percentage;
            }

        return mapped;
    }

    private void cashMappedXPoints () {

    }

    private void cashMappedYPoints () {

    }

    private void drawScaleX (float[] mappedX, Canvas canvas) {

        float chartWidthPx = mDrawingAreaWidth / mNormWidth;
        int xLabelPeriodicity = 0;

        for (xLabelPeriodicity = 1; true; xLabelPeriodicity = xLabelPeriodicity * 2) {
            if (!mXLabelsPeriodToMinChartWidthPx.containsKey(xLabelPeriodicity))
                continue;

            if (mXLabelsPeriodToMinChartWidthPx.get(xLabelPeriodicity) <= chartWidthPx)
                break;
        }
        mXLabelsPeriodCurrent = xLabelPeriodicity;

        mBaseLabelPaint.setAlpha(255);
        mBaseLabelPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = mPointsMinIndex; i <= mPointsMaxIndex; i++) {
            if ((mPosX.length - 1 - i) % mXLabelsPeriodCurrent == 0) {
                canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[i], mXLabelsYCoordinate, mBaseLabelPaint);
            }
        }

        //if there are no inbetween labels
        if (mXLabelsPeriodToMinChartWidthPx.get(mXLabelsPeriodCurrent) == chartWidthPx)
            return;

        if (mXLabelsPeriodToMinChartWidthPx.containsKey(mXLabelsPeriodCurrent / 2)) {
            float alphaMultiplier = MathUtils.inverseLerp(mXLabelsPeriodToMinChartWidthPx.get(mXLabelsPeriodCurrent),
                                                          mXLabelsPeriodToMinChartWidthPx.get(mXLabelsPeriodCurrent / 2),
                                                          chartWidthPx);
            alphaMultiplier = (alphaMultiplier - 0.334f) * 3;
            alphaMultiplier = MathUtils.clamp(alphaMultiplier, 1, 0);
            mBaseLabelPaint.setAlpha((int) Math.floor(255 * alphaMultiplier));

            for (int i = mPointsMinIndex; i <= mPointsMaxIndex; i++) {
                if ((mPosX.length - 1 - i) % (mXLabelsPeriodCurrent / 2) == 0 && (mPosX.length - 1 - i) % mXLabelsPeriodCurrent != 0) {

                    if (i == mPosX.length - 1)
                        mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
                    else if (i == 0)
                        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
                    else
                        mBaseLabelPaint.setTextAlign(Paint.Align.CENTER);

                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[i], mXLabelsYCoordinate, mBaseLabelPaint);
                }
            }
        }
    }

    private void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
    {
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / Y_DIVIDERS_COUNT;

        float startY = mDrawingAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 1; i++) {
            canvas.drawLine(mDrawingAreaStartX, startY, mDrawingAreaEndX, stopY, mDividerPaint);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    private void drawYLabels (long height, long yMax, int alpha, Canvas canvas) {
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / Y_DIVIDERS_COUNT;

        long step = 0;
        float yLabelCoord = mDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setAlpha(alpha);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), mDrawingAreaStartX, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }

    }

    private void drawChosenPointValues(Canvas canvas) {
        //vertical divider
//        canvas.drawLine(mXCoordinateOfChosenPoint, mDrawingAreaStartY, mXCoordinateOfChosenPoint, mDrawingAreaEndY, mDividerPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDividerPaint.setAlpha(255);
        canvas.drawLine(mDrawingAreaStartX, mDrawingAreaEndY, mDrawingAreaEndX, mDrawingAreaEndY, mDividerPaint);

        float[] mappedX = null;

        if (mPosX != null && mPos1 >= 0 && mPos2 >= 0) {
            mappedX = mapXPoints(mPosX, mPos1, mPos2);
            drawScaleX(mappedX, canvas);
        }

        if (mPosX == null || mLines == null || mLines.length == 0 || mPos1 < 0 || mPos2 < 0)
        {
            drawScaleY(100, 100, 255, canvas);
            return;
        }

        for (YScale yScale : mYScales)
            drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);


        for (LineData line : mLines)
            drawChartLine(line, 0, mMaxY, 255, mappedX, canvas);

        for (YScale yScale : mYScales)
            drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);

        if (mPointIsChosen) {
            drawChosenPointValues(canvas);
            mPointIsChosen = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if (y >= mDrawingAreaEndY) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(x);
                }
                return true;
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
        }
        return true;
    }

    private void showPointDetails(float xCoord) {
        if (mLines == null || mLines.length == 0)
            return;
        mPointIsChosen = true;
//        mPositionOfChosenPoint = mapCoordinateToPoint(xCoord);
//        mXCoordinateOfChosenPoint = mapXPoint(mPosX[mPositionOfChosenPoint]);
        invalidate();
    }

    private void hidePointDetails() {
        mPointIsChosen = false;
        invalidate();
    }
}
