package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class BaseLineChartDrawer extends BaseChartDrawer {

    class YScale {
        long Height;
        long MaxY;
        long MinY;
        int  Alpha;

        long HeightStart;
        long HeightEnd;

        long MaxYStart;
        long MaxYEnd;

        long MinYStart;
        long MinYEnd;

        int  AlphaStart;
        int  AlphaEnd;
    }

    class ChartLine
    {
        LineData Data;

        int      Alpha;
        int      AlphaStart;
        int      AlphaEnd;

        float[]  mChartMappedPointsY;
        float[]  mScrollMappedPointsY;
        float[]  mScrollOptimizedPointsY;
        float[]  mScrollOptimizedPointsX;

        boolean IsVisible()
        {
            return (Alpha > 0 || AlphaEnd > 0);
        }
    }

    abstract class YMaxAnimator {
        long mMaxY = -1;
        long mMinY;
        long mTargetMaxY = -1;
        long mTargetMinY = -1;
        ValueAnimator mMaxYAnimator;
        ArrayList<YScale> mYScales = new ArrayList<YScale>();

        YMaxAnimator() {
        }

        abstract void updateMinMaxY();

        void startAnimationYMax() {
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
                yScale.MinYStart   = yScale.MinY;
                yScale.MinYEnd     = yScale.MinY;
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
            newScale.MinY        = mTargetMinY;
            newScale.MinYStart   = mTargetMinY;
            newScale.MinYEnd     = mTargetMinY;
            newScale.Alpha       = 0;
            newScale.AlphaStart  = 0;
            newScale.AlphaEnd    = 255;
            mYScales.add(newScale);

            final long startMaxY = mMaxY;
            final long endMaxY   = mTargetMaxY;
            final long startMinY = mMinY;
            final long endMinY   = mTargetMinY;

            final String KEY_PHASE = "phase";

            mMaxYAnimator = new ValueAnimator();
            mMaxYAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
            mMaxYAnimator.setDuration(400);

            mMaxYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    float t = (float) animator.getAnimatedValue(KEY_PHASE);

                    mMaxY = (long)MathUtils.lerp(startMaxY, endMaxY, t);
                    mMinY = (long)MathUtils.lerp(startMinY, endMinY, t);
                    mapYPointsForChartView();

                    for (YScale yScale : mYScales)
                    {
                        yScale.MaxY   = (long)MathUtils.lerp(yScale.MaxYStart,   yScale.MaxYEnd,   t);
                        yScale.MinY   = (long)MathUtils.lerp(yScale.MinYStart,   yScale.MinYEnd,   t);

                        if (yScale.AlphaEnd == 255)
                        {
                            if (yScale.Height < yScale.MaxY)
                                yScale.Height = (long)MathUtils.lerp(yScale.HeightStart, yScale.HeightEnd, MathUtils.getEaseOut(t));
                            else
                                yScale.Height = (long)MathUtils.lerp(yScale.HeightStart, yScale.HeightEnd, MathUtils.getEaseIn(t));

                            yScale.Alpha  = (int) MathUtils.lerp(yScale.AlphaStart,  yScale.AlphaEnd,  MathUtils.clamp(t / 0.45f - 1, 1, 0));
                        }
                        else
                        {
                            yScale.Height = (long)MathUtils.lerp(yScale.HeightStart, yScale.HeightEnd, t);

                            yScale.Alpha  = (int) MathUtils.lerp(yScale.AlphaStart,  yScale.AlphaEnd,  MathUtils.clamp(t / 0.55f, 1, 0));
                        }
                    }
                }
            });

            mMaxYAnimator.addUpdateListener(mViewAnimatorListener);

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
    }

    final int             Y_DIVIDERS_COUNT         = 6;
    final int             PLATE_WIDTH_DP           = 110;
    final int             PLATE_HEIGHT_DP          = 56;

    private final float   mPlateWidthPx;
    private final float   mPlateHeightPx;
    private final float   mOptimTolerancePx;

    Paint                 mChartPaint;
    Paint                 mCirclePaint;

    ArrayList<ChartLine>  mLines                  = new ArrayList<>();

    private ValueAnimator mSetLinesAnimator;

    private boolean       mSetLinesFirstTime      = true;

    int                   mPositionOfChosenPoint;


    BaseLineChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mOptimTolerancePx = mPosX.length >= 150 ? MathUtils.dpToPixels(2, mContext) : 1;
        mPlateWidthPx = MathUtils.dpToPixels(PLATE_WIDTH_DP, context);
        mPlateHeightPx = MathUtils.dpToPixels(PLATE_HEIGHT_DP, context);
    }

    public abstract void draw(Canvas canvas);

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        super.setViewDimens(width, height, drawingAreaOffsetXPx, drawingAreaOffsetYPx, scrollDrawingAreaHeightPx);

        mapXPointsForChartView();
        mapXPointsForScrollView();
        mapYPointsForChartView();
        mapYPointsForScrollView();
    }

    @Override
    public boolean setBorders (float normPosX1, float normPosX2) {
        boolean result = super.setBorders(normPosX1, normPosX2);

        mapXPointsForChartView();

        return result;
    }

    public void setLines (LineData[] lines) {
        boolean animate = false;

        for (ChartLine line : mLines) {
            line.AlphaEnd = Arrays.asList(lines).contains(line.Data) ? 255 : 0;

            if (line.AlphaEnd != line.Alpha)
                animate = true;

            if (mSetLinesFirstTime) {
                line.Alpha = line.AlphaEnd;
                animate = false;
            }
        }

        if (animate)
            startSetLinesAnimation();

        hidePointDetails();

        mapYPointsForScrollView();

        mSetLinesFirstTime = false;
    }

    private void startSetLinesAnimation()
    {
        if (mSetLinesAnimator != null)
        {
            mSetLinesAnimator.cancel();
            mSetLinesAnimator = null;
        }

        for (ChartLine line : mLines)
            line.AlphaStart = line.Alpha;

        final String KEY_PHASE = "phase";

        mSetLinesAnimator = new ValueAnimator();
        mSetLinesAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
        mSetLinesAnimator.setDuration(400);

        mSetLinesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float t = (float) animator.getAnimatedValue(KEY_PHASE);

                for (ChartLine line : mLines) {
                    if (line.Alpha != line.AlphaEnd)
                        line.Alpha = (int)MathUtils.lerp(line.AlphaStart,   line.AlphaEnd,   t);
                }
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    protected abstract void mapYPointsForChartView();

    private void mapYPointsForScrollView()
    {
        if (!showChartLines())
            return;

        for (ChartLine line : mLines) {
            if (line.IsVisible()){
                line.mScrollMappedPointsY = mapYPointsForScrollView(line.Data.posY);
                optimizeScrollPoints(line);
            }
        }
    }

    float[] mapYPointsForChartView(long[] points, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1];

        for (int i = 0, j = mPointsMinIndex; i < mapped.length; i++, j++) {
            float percentage = (float) (points[j] - yMin) / (float) calculatedArea;
            mapped[i] = mChartDrawingAreaHeight * percentage + mChartDrawingAreaStartY;
            mapped[i] = mChartDrawingAreaEndY - mapped[i] + mChartDrawingAreaStartY;
        }

        return mapped;
    }

    private float[] mapYPointsForScrollView(long[] points) {
        long calculatedArea = MathUtils.getMax(points);
        float[] mapped = new float[points.length];

        for (int i = 0; i < mapped.length; i++) {
            float percentage = (float) points[i] / (float) calculatedArea;
            mapped[i] = mScrollDrawingAreaHeight * percentage + mScrollDrawingAreaStartY;
            mapped[i] = mScrollDrawingAreaEndY - mapped[i] + mScrollDrawingAreaStartY;
        }

        return mapped;
    }

    private void optimizeScrollPoints(ChartLine line) {
        if (mScrollMappedPointsX == null)
            return;
        ArrayList<Float> optimizedX = new ArrayList<>();
        ArrayList<Float> optimizedY = new ArrayList<>();
        MathUtils.optimizePoints(mScrollMappedPointsX, line.mScrollMappedPointsY, mOptimTolerancePx, optimizedX, optimizedY);
        line.mScrollOptimizedPointsX = new float[optimizedX.size()];
        for (int i = 0; i < line.mScrollOptimizedPointsX.length; i++) {
            line.mScrollOptimizedPointsX[i] = optimizedX.get(i);
        }
        line.mScrollOptimizedPointsY = new float[optimizedY.size()];
        for (int i = 0; i < line.mScrollOptimizedPointsY.length; i++) {
            line.mScrollOptimizedPointsY[i] = optimizedY.get(i);
        }
    }

    @Override
    protected void setUpPaints() {
        super.setUpPaints();

        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.STROKE);
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStrokeCap(Paint.Cap.ROUND);

        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(6);
        mCirclePaint.setAntiAlias(true);
    }

    void drawChartLineInChartView(ChartLine line, Canvas canvas, float[] mappedX, float[] mappedY){
        mChartPaint.setColor(line.Data.color);
        mChartPaint.setAlpha(line.Alpha);

        canvas.save();
        canvas.clipRect(0, mChartDrawingAreaStartY, mViewWidth, mChartDrawingAreaEndY);
        float[] drawingPoints = MathUtils.concatArraysForDrawing(mappedX, mappedY);
        if (drawingPoints != null) {
            canvas.drawLines(drawingPoints, mChartPaint);
        }
        canvas.restore();
    }

    void drawChartLineInScrollView(ChartLine line, Canvas canvas, float[] mappedX, float[] mappedY){
        mChartPaint.setColor(line.Data.color);
        mChartPaint.setAlpha(line.Alpha);

        canvas.save();
        Path clipPath = new Path();
        RectF clipRect = new RectF(mScrollDrawingAreaStartX, mScrollDrawingAreaStartY, mScrollDrawingAreaEndX, mScrollDrawingAreaEndY);
        clipPath.addRoundRect(clipRect, 20, 20, Path.Direction.CW);
        canvas.clipPath(clipPath);

        float[] drawingPoints = MathUtils.concatArraysForDrawing(mappedX, mappedY);
        if (drawingPoints != null) {
            canvas.drawLines(drawingPoints, mChartPaint);
        }

        canvas.restore();
    }

    void drawChosenPointCircle(float[] mappedX, float[] mappedY, int color, Canvas canvas) {
        mCirclePaint.setColor(color);
        canvas.drawCircle(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mappedY[mPositionOfChosenPoint - mPointsMinIndex], 16f, mCirclePaint);
        TypedValue background = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.primaryBackgroundColor, background, true)) {
            mCirclePaint.setColor(background.data);
        }
        canvas.drawCircle(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mappedY[mPositionOfChosenPoint - mPointsMinIndex], 8f, mCirclePaint);
    }

    void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaEndY, mDividerPaint);
    }

    void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
        //plate
        float top = mChartDrawingAreaHeight * 0.05f + mChartDrawingAreaWidth * 0.05f;
        float bottom = top + mPlateHeightPx;
        float left;
        float right;
        float offset = mChartDrawingAreaWidth * 0.05f;
        if ((mappedX[mPositionOfChosenPoint - mPointsMinIndex] + offset + mPlateWidthPx) >= mChartDrawingAreaEndX) {
            right = mappedX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - mPlateWidthPx;
        } else {
            left = mappedX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
            right = left + mPlateWidthPx;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        int cornerRadius = 25;

        TypedValue dividerColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.dividerColor, dividerColor, true)) {
            mPlatePaint.setColor(dividerColor.data);
        }
        mPlatePaint.setStrokeWidth(2);
        mPlatePaint.setStyle(Paint.Style.STROKE);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        mPlatePaint.setStyle(Paint.Style.FILL);
        TypedValue plateColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.plateBackgroundColor, plateColor, true)) {
            mPlatePaint.setColor(plateColor.data);
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        //text
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateXValuePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.22f, mPlateXValuePaint);

        LineData[] lines = getActiveChartLines();
        mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateNamePaint.setTextSize(mTextSizeMediumPx);
        float heightOffset = 0.55f;
        for (LineData line : lines){
            mPlateYValuePaint.setColor(line.color);
            canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
            canvas.drawText(line.name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
            heightOffset += 0.3f;
        }
    }


    void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
    {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mChartDrawingAreaStartX, mChartDrawingAreaEndY, mChartDrawingAreaEndX, mChartDrawingAreaEndY, mDividerPaint);

        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / Y_DIVIDERS_COUNT;

        float startY = mChartDrawingAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 1; i++) {
            canvas.drawLine(mChartDrawingAreaStartX, startY, mChartDrawingAreaEndX, stopY, mDividerPaint);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    @Override
    protected void showPointDetails(float xCoord) {
        if (!showChartLines())
            return;

        super.showPointDetails(xCoord);
    }

    boolean showChartLines() {
        for (ChartLine line : mLines)
            if (line.IsVisible())
                return true;

        return false;
    }

    LineData[] getActiveChartLines()
    {
        ArrayList<LineData> arrayList = new ArrayList<>();

        for (ChartLine line : mLines)
            if (line.AlphaEnd > 0)
                arrayList.add(line.Data);

        return arrayList.toArray(new LineData[arrayList.size()]);
    }
}
