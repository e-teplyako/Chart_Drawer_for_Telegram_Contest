package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.android.telegramcontest.Interfaces.SliderObservable;
import com.example.android.telegramcontest.Interfaces.SliderObserver;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.Arrays;

public class ChartView2 extends View implements SliderObserver{

    private Context mContext;

    final float DRAWING_AREA_OFFSET = 0.05f;

    private long[] mPosX;
    private long  mPos1 = -1;
    private long  mPos2 = -1;
    private int mPointsMinIndex;
    private int mPointsMaxIndex;
    private LineData[] mLines;
    private long mYMin;
    private long mYMax;
    private long mMappedX;
    private long[][] mMappedY;

    private float mViewWidth;
    private float mViewHeight;
    private float mDrawingAreaStart;
    private float mDrawingAreaEnd;
    private float mDrawingAreaWidth;

    private boolean mSizeChanged = false;

    private Paint mChartPaint;

    public ChartView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setUpPaints();
    }

    public void init (ChartData chartData, SliderObservable observable) {
        mPosX = chartData.posX;
        observable.registerObserver(this);
    }

    public void setBorders (float normPos1, float normPos2) {
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSizeChanged = true;

        mViewWidth = getWidth();
        mViewHeight = getHeight();
        mDrawingAreaStart = mViewWidth * DRAWING_AREA_OFFSET;
        mDrawingAreaEnd = mViewWidth * 0.95f;
        mDrawingAreaWidth = mDrawingAreaEnd - mDrawingAreaStart;
    }


    private void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(6);
    }

    private void drawLine (LineData line, long yMin, long yMax, int alpha, Canvas canvas) {
        float[] mappedX = mapXPoints(mPosX, mPos1, mPos2);
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
                mapped[i] = mViewHeight * percentage;
                mapped[i] = mViewHeight - mapped[i];
            }

        return mapped;
    }

    private float[] mapXPoints (long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[points.length];
            for (int i = 0; i < mapped.length; i++) {
                float percentage = (float) (points[i] - xMin) / (float) calculatedArea;
                mapped[i] = mDrawingAreaStart + mDrawingAreaWidth * percentage;
            }

        return mapped;
    }

    private void cashMappedXPoints () {

    }

    private void cashMappedYPoints () {

    }

    private void drawScaleX () {

    }

    private void drawScaleY (long yMin, long yMax, int alpha) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mPosX == null || mLines == null || mLines.length == 0 || mPos1 < 0 || mPos2 < 0)
            return;

        drawScaleX();
        drawScaleY(mYMin, mYMax, 255);

        for (LineData line : mLines)
            drawLine(line, 0, 10000, 255, canvas);
    }


}
