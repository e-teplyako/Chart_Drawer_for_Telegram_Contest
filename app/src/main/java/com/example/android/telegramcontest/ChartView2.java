package com.example.android.telegramcontest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.telegramcontest.Interfaces.SliderObservable;
import com.example.android.telegramcontest.Interfaces.SliderObserver;
import com.example.android.telegramcontest.Utils.DateTimeUtils;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.Arrays;
import java.util.HashMap;

public class ChartView2 extends View implements SliderObserver{

    private Context mContext;
    private Resources.Theme mTheme;

    final float DRAWING_AREA_OFFSET = 0.05f;
    final int DIVIDERS_COUNT = 6;
    final int TEXT_SIZE_DP = 12;
    final int TEXT_LABEL_WIDTH_DP = 36;
    final int TEXT_LABEL_DISTANCE_DP = 22;

    private long[] mPosX;
    private long  mPos1 = -1;
    private long  mPos2 = -1;
    float mNormWidth;
    private int mPointsMinIndex;
    private int mPointsMaxIndex;
    private LineData[] mLines;
    private long mYMin;
    private long mYMax;
    private long mMappedX;
    private long[][] mMappedY;

    private float mViewWidth;
    private float mViewHeight;
    private float mDrawingAreaWidthStart;
    private float mDrawingAreaWidthEnd;
    private float mDrawingAreaHeightStart;
    private float mDrawingAreaHeightEnd;
    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;
    private float mXLabelsYCoordinate;

    final private float mTextSizePx;
    private float mDateWidthPx;
    private float mDateDistancePx;

    private boolean mSizeChanged = false;
    private boolean mPointIsChosen = false;

    private HashMap<Integer, Float> mXLabelPeriodicityToMinimalChartWidthPx = new HashMap<>();
    private int mXLabelPeriodicity;

    private Paint mChartPaint;
    private Paint mDividerPaint;
    private TextPaint mBaseLabelPaint;

    public ChartView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTheme = context.getTheme();

        mTextSizePx = MathUtils.dpToPixels(TEXT_SIZE_DP, mContext);

        mDateWidthPx = MathUtils.dpToPixels(TEXT_LABEL_WIDTH_DP, mContext);
        mDateDistancePx = MathUtils.dpToPixels(TEXT_LABEL_DISTANCE_DP, mContext);

        setUpPaints();
    }

    public void init (ChartData chartData, SliderObservable observable) {
        mPosX = chartData.posX;
        observable.registerObserver(this);
    }

    public void setBorders (float normPos1, float normPos2) {
        mNormWidth = normPos2 - normPos1;
        long pos1 = 0;
        long pos2 = 0;
        long xMin = MathUtils.getMin(mPosX);
        long xMax = MathUtils.getMax(mPosX);
        long width = xMax - xMin;
        pos1 = (long) Math.floor(normPos1 * width) + xMin;
        pos2 = (long) Math.ceil(normPos2 * width) + xMin;

        if (mPos1 != pos1 || mPos2 != pos2)
            invalidate();

        mPos1 = pos1;
        mPos2 = pos2;

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * DRAWING_AREA_OFFSET) / (1 - 2 * DRAWING_AREA_OFFSET));

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);
    }

    public void setLines (LineData[] lines) {
        if (mLines == null || !Arrays.equals(mLines, lines)) 
            invalidate();
        
        mLines = lines;

        if (mLines != null && mLines.length != 0) {
            mYMin = MathUtils.getMin(mLines);
            mYMax = MathUtils.getMax(mLines);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSizeChanged = true;

        mViewWidth = getWidth();
        mViewHeight = getHeight();
        mDrawingAreaWidthStart = mViewWidth * DRAWING_AREA_OFFSET;
        mDrawingAreaWidthEnd = mViewWidth * 0.95f;
        mDrawingAreaWidth = mDrawingAreaWidthEnd - mDrawingAreaWidthStart;
        mDrawingAreaHeightStart = mViewHeight * DRAWING_AREA_OFFSET;
        mDrawingAreaHeightEnd = mViewHeight * 0.85f;
        mDrawingAreaHeight = mDrawingAreaHeightEnd - mDrawingAreaHeightStart;

        mXLabelsYCoordinate = mViewHeight * 0.9f;

        float minChartWidth = mDrawingAreaWidth;
        float maxChartWidth = minChartWidth / ScrollChartView2.MINIMAL_NORM_SLIDER_WIDTH;

        int sizeOfArray = mPosX.length;
        for (int i = 1; true; i = i * 2) {
            // we take size - 1 cause first point should be labeled
            int textElemsCount = sizeOfArray / i;
            float chartWidth = mDateWidthPx * textElemsCount + mDateDistancePx * (textElemsCount - 1);
            if (chartWidth >= minChartWidth && chartWidth <= maxChartWidth)
                mXLabelPeriodicityToMinimalChartWidthPx.put(i, chartWidth);
            else if (chartWidth < minChartWidth) {
                mXLabelPeriodicityToMinimalChartWidthPx.put(i, chartWidth);
                break;
            }
        }
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

    private void drawLine (LineData line, long yMin, long yMax, int alpha, float[] mappedX, Canvas canvas) {
        float[] mappedY = mapYPoints(line.posY, yMin, yMax);

        mChartPaint.setColor(line.color);
        mChartPaint.setAlpha(alpha);

        for (int i = mPointsMinIndex; i < mPointsMaxIndex; i++){
            canvas.drawLine(mappedX[i], mappedY[i], mappedX[i+1], mappedY[i+1], mChartPaint);
        }
    }

    private float[] mapYPoints (long[] points, long yMin, long yMax) {
        long calculatedArea = MathUtils.getNearestSixDivider(yMax - yMin);
        float[] mapped = new float[points.length];

            for (int i = 0; i < points.length; i++) {
                float percentage = (float) (points[i] - yMin) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage + mDrawingAreaHeightStart;
                mapped[i] = mDrawingAreaHeightEnd - mapped[i] + mDrawingAreaHeightStart;
            }

        return mapped;
    }

    private float[] mapXPoints (long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[points.length];
            for (int i = 0; i < mapped.length; i++) {
                float percentage = (float) (points[i] - xMin) / (float) calculatedArea;
                mapped[i] = mDrawingAreaWidthStart + mDrawingAreaWidth * percentage;
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
            if (!mXLabelPeriodicityToMinimalChartWidthPx.containsKey(xLabelPeriodicity))
                continue;

            if (mXLabelPeriodicityToMinimalChartWidthPx.get(xLabelPeriodicity) <= chartWidthPx)
                break;
        }
        mXLabelPeriodicity = xLabelPeriodicity;

        mBaseLabelPaint.setAlpha(255);
        mBaseLabelPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = mPointsMinIndex; i <= mPointsMaxIndex; i++) {
            if ((mPosX.length - 1 - i) % mXLabelPeriodicity == 0) {
                canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[i], mXLabelsYCoordinate, mBaseLabelPaint);
            }
        }

        //if there are no inbetween labels
        if (mXLabelPeriodicityToMinimalChartWidthPx.get(mXLabelPeriodicity) == chartWidthPx)
            return;

        if (mXLabelPeriodicityToMinimalChartWidthPx.containsKey(mXLabelPeriodicity / 2)) {
            float alphaMultiplier = MathUtils.inverseLerp(mXLabelPeriodicityToMinimalChartWidthPx.get(mXLabelPeriodicity),
                                                          mXLabelPeriodicityToMinimalChartWidthPx.get(mXLabelPeriodicity / 2),
                                                          chartWidthPx);
            alphaMultiplier = (alphaMultiplier - 0.334f) * 3;
            alphaMultiplier = MathUtils.clamp(alphaMultiplier, 1, 0);
            mBaseLabelPaint.setAlpha((int) Math.floor(255 * alphaMultiplier));

            for (int i = mPointsMinIndex; i <= mPointsMaxIndex; i++) {
                if ((mPosX.length - 1 - i) % (mXLabelPeriodicity / 2) == 0 && (mPosX.length - 1 - i) % mXLabelPeriodicity != 0) {

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

    /*Right now only draws fixed dividers*/
    private void drawScaleY (long yMin, long yMax, int alpha, Canvas canvas) {
        long diff = MathUtils.getNearestSixDivider(yMax - yMin);
        long step = diff / DIVIDERS_COUNT;

        float spaceBetweenDividers = mDrawingAreaHeight / DIVIDERS_COUNT;

        float startX = mDrawingAreaWidthStart;
        float stopX = mDrawingAreaWidthEnd;
        float startY = mDrawingAreaHeightStart + spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < DIVIDERS_COUNT; i++) {
            canvas.drawLine(startX, startY, stopX, stopY, mDividerPaint);
            startY += spaceBetweenDividers;
            stopY = startY;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mPosX == null || mLines == null || mLines.length == 0 || mPos1 < 0 || mPos2 < 0)
            return;


        float[] mappedX = mapXPoints(mPosX, mPos1, mPos2);

        drawScaleX(mappedX, canvas);
        drawScaleY(mYMin, mYMax, 255, canvas);

        for (LineData line : mLines)
            drawLine(line, mYMin, mYMax, 255, mappedX, canvas);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
//        float x = event.getX();
//        float y = event.getY();
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE:
//            case MotionEvent.ACTION_DOWN:
//                this.getParent().requestDisallowInterceptTouchEvent(true);
//                if (y >= mDrawingAreaHeight) {
//                    hideVerticalDivider();
//                }
//                else {
//                    showPointDetails(x);
//                }
//                return true;
//            case MotionEvent.ACTION_UP:
//                this.getParent().requestDisallowInterceptTouchEvent(true);
//                return true;
//        }
//        return true;
//    }

//    private void showPointDetails(float xCoord) {
//        if (mLines == null || mLines.length == 0)
//            return;
//        int pointPosition = mapCoordinateToPoint(xCoord);
//        float pointCoordinate = mapXPoint(mXPointsPart[pointPosition]);
//        showVerticalDivider(pointCoordinate, pointPosition);
//    }
//
//
//    private void showVerticalDivider(float xCoord, int position) {
//        mPointIsChosen = true;
//        mXCoordinateOfChosenPoint = xCoord;
//        mPositionOfChosenPoint = position;
//        invalidate();
//    }
//
//    private void hideVerticalDivider() {
//        mPointIsChosen = false;
//        invalidate();
//    }
}
