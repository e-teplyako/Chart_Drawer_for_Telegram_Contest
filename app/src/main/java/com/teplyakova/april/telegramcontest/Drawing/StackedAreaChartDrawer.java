package com.teplyakova.april.telegramcontest.Drawing;

import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
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
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StackedAreaChartDrawer extends BaseChartDrawer {

    public class ChartArea
    {
        public LineData Data;

        public int[]    Percentages;

        public float    PosYCoefficientStart;
        public float    PosYCoefficient;
        public float    PosYCoefficientEnd;

        float[]         ChartMappedPointsY;
        float[]         ScrollMappedPointsY;

        boolean isVisible() {
            return (PosYCoefficient > 0 || PosYCoefficientEnd > 0);
        }
    }

    private final int            Y_DIVIDERS_COUNT       = 5;
    private final int            PLATE_WIDTH_DP         = 140;
    private final int            PLATE_HEIGHT_DP        = 130;

    private final float          mPlateHeightPx;
    private final float          mPlateWidthPx;

    private Paint                mChartPaint;

    private ArrayList<ChartArea> mAreas                 = new ArrayList<>();

    private ValueAnimator        mSetLinesAnimator;

    private int                  mPositionOfChosenPoint;

    private boolean              mSetLinesFirstTime     = true;

    private Path                 mDrawPath;

    public StackedAreaChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mPlateHeightPx = MathUtils.dpToPixels(PLATE_HEIGHT_DP, context);
        mPlateWidthPx = MathUtils.dpToPixels(PLATE_WIDTH_DP, context);

        for (LineData lineData : chartData.lines) {
            ChartArea chartArea = new ChartArea();
            chartArea.Data = lineData;
            chartArea.Percentages = new int[lineData.posY.length];
            chartArea.ChartMappedPointsY = new float[lineData.posY.length];
            chartArea.ScrollMappedPointsY = new float[lineData.posY.length];
            chartArea.PosYCoefficient = 1;
            chartArea.PosYCoefficientStart = 1;
            chartArea.PosYCoefficientEnd = 1;
            mAreas.add(chartArea);
        }

        calculatePercentages();

        mDrawPath = new Path();
    }

    public void draw(Canvas canvas) {
        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!showChartAreas()) {
            drawScaleY(125, 125, 255, canvas);
            drawYLabels(125, 125, 255, true, canvas);
            drawRects(canvas);
            return;
        }

        drawAreas(canvas, true);
        drawAreas(canvas, false);

        drawScaleY(125, 125, 255, canvas);
        drawYLabels(125, 125, 255, true, canvas);

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mChartMappedPointsX, mXCoordinateOfTouch);
            drawVerticalDivider(mChartMappedPointsX, canvas);
            drawChosenPointPlate(mChartMappedPointsX, canvas);
        }

        drawRects(canvas);
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        super.setViewDimens(width, height, drawingAreaOffsetXPx, drawingAreaOffsetYPx, scrollDrawingAreaHeightPx);

        mapXPointsForChartView();
        mapXPointsForScrollView();
    }

    public boolean setBorders (float normPosX1, float normPosX2) {
        boolean result = super.setBorders(normPosX1, normPosX2);

        calculatePercentages();
        mapXPointsForChartView();
        mapXPointsForScrollView();
        mapYPointsForChartView(getMaxPosYCoefficient());
        mapYPointsForScrollView(getMaxPosYCoefficient());

        return result;
    }

    public void setLines (LineData[] lines) {
        for (ChartArea area : mAreas) {
            if(!Arrays.asList(lines).contains(area.Data)) {
                if (!mSetLinesFirstTime) {
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = 0;
                }
                else {
                    area.PosYCoefficient = 0;
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = area.PosYCoefficient;
                }
            }
            else {
                if (!mSetLinesFirstTime) {
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = 1;
                }
                else {
                    area.PosYCoefficient = 1;
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = area.PosYCoefficient;
                }
            }
        }

        if (!mSetLinesFirstTime)
            startSetLinesAnimation();

        hidePointDetails();

        mSetLinesFirstTime = false;
    }

    private void startSetLinesAnimation()
    {
        if (mSetLinesAnimator != null)
        {
            mSetLinesAnimator.cancel();
            mSetLinesAnimator = null;
        }

        final String KEY_PHASE = "phase";

        mSetLinesAnimator = new ValueAnimator();
        mSetLinesAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
        mSetLinesAnimator.setDuration(400);

        mSetLinesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float t = (float) animator.getAnimatedValue(KEY_PHASE);

                for (ChartArea area : mAreas) {
                    for (int i = 0; i < area.Percentages.length; i++) {
                        if (area.PosYCoefficient != area.PosYCoefficientEnd) {
                            area.PosYCoefficient = MathUtils.lerp(area.PosYCoefficientStart, area.PosYCoefficientEnd, t);
                        }
                    }
                }
                calculatePercentages();
                mapYPointsForChartView(getMaxPosYCoefficient());
                mapYPointsForScrollView(getMaxPosYCoefficient());
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    private void calculatePercentages() {
        if (!mBordersSet || mAreas == null || mAreas.size() == 0 || !showChartAreas())
            return;

        float[] sums = new float[mAreas.get(0).Data.posY.length];
        for (ChartArea area : mAreas) {
            area.Percentages = new int[sums.length];
            for (int i = 0; i < area.Percentages.length; i++) {
                sums[i] += area.PosYCoefficient * area.Data.posY[i];
            }
        }

        for (int i = 0; i < sums.length; i++) {
            float[] assumedPercentages = new float[mAreas.size()];
            for (int j = 0; j < mAreas.size(); j++) {
                assumedPercentages[j] = mAreas.get(j).PosYCoefficient * mAreas.get(j).Data.posY[i] / sums[i] * 100;
            }
            int sum = 0;
            for (int n = 0; n < assumedPercentages.length; n++) {
                int floor = (int) Math.floor(assumedPercentages[n]);
                sum += floor;
                mAreas.get(n).Percentages[i] = floor;
                assumedPercentages[n] = assumedPercentages[n] - floor;
            }

            int remaining = 100 - sum;
            for (int j = 0; j < remaining; j++) {
                int index = MathUtils.getMaxIndex(assumedPercentages);
                assumedPercentages[index] = -1;
                mAreas.get(index).Percentages[i]++;
            }
        }
    }

    private void mapYPointsForChartView(float coefficient) {
        if (!mBordersSet || !showChartAreas())
            return;

        long calculatedArea = 100;

        int[] previous = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : mAreas) {
            area.ChartMappedPointsY = new float[mPointsMaxIndex - mPointsMinIndex + 1];
            for (int i = 0, j = mPointsMinIndex; i < area.ChartMappedPointsY.length; i++, j++) {
                float percentage = (float) (area.Percentages[j] + previous[i]) / (float) calculatedArea;
                area.ChartMappedPointsY[i] = coefficient * mChartDrawingAreaHeight * percentage + mChartDrawingAreaStartY;
                area.ChartMappedPointsY[i] = mChartDrawingAreaEndY - area.ChartMappedPointsY[i] + mChartDrawingAreaStartY;
                previous[i] += area.Percentages[j];
            }
        }
    }

    private void mapYPointsForScrollView(float coefficient) {
        if (!showChartAreas())
            return;

        long calculatedArea = 100;

        int[] previous = new int[mScrollMappedPointsX.length];
        for (ChartArea area : mAreas) {
            area.ScrollMappedPointsY = new float[mScrollMappedPointsX.length];
            for (int i = 0; i < area.ScrollMappedPointsY.length; i++) {
                float percentage = (float) (area.Percentages[i] + previous[i]) / (float) calculatedArea;
                area.ScrollMappedPointsY[i] = coefficient * mScrollDrawingAreaHeight * percentage + mScrollDrawingAreaStartY;
                area.ScrollMappedPointsY[i] = mScrollDrawingAreaEndY - area.ScrollMappedPointsY[i] + mScrollDrawingAreaStartY;
                previous[i] += area.Percentages[i];
            }
        }
    }

    @Override
    protected void setUpPaints() {
        super.setUpPaints();

        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    private void drawAreas(Canvas canvas, boolean drawInChartView) {
        if (mAreas == null || mAreas.size() == 0)
            return;

        if (!mDrawPath.isEmpty())
            mDrawPath.reset();

        float drawingStartPoint;
        float[] mappedX;
        if (drawInChartView) {
            drawingStartPoint = mChartDrawingAreaEndY;
            mappedX = mChartMappedPointsX;
        }
        else {
            drawingStartPoint = mScrollDrawingAreaEndY;
            mappedX = mScrollMappedPointsX;
        }

        if(!drawInChartView) {
            canvas.save();
            Path clipPath = new Path();
            RectF clipRect = new RectF(mScrollDrawingAreaStartX, mScrollDrawingAreaStartY, mScrollDrawingAreaEndX, mScrollDrawingAreaEndY);
            clipPath.addRoundRect(clipRect, 20, 20, Path.Direction.CW);
            canvas.clipPath(clipPath);
        }

        mDrawPath.moveTo(mappedX[mappedX.length - 1], drawingStartPoint);
        float[] previous;
        if (drawInChartView) {
            previous = new float[mAreas.get(0).ChartMappedPointsY.length];
        }
        else {
            previous = new float[mAreas.get(0).ScrollMappedPointsY.length];
        }
        Arrays.fill(previous, drawingStartPoint);
        for (ChartArea area : mAreas) {
            for (int i = previous.length - 1; i >= 0; i--) {
                mDrawPath.lineTo(mappedX[i], previous[i]);
            }
            float[] mappedY;
            if (drawInChartView) {
                mappedY = area.ChartMappedPointsY;
            }
            else {
                mappedY = area.ScrollMappedPointsY;
            }
            for (int i = 0; i < mappedY.length; i++) {
                mDrawPath.lineTo(mappedX[i], mappedY[i]);
            }
            mChartPaint.setColor(area.Data.color);
            canvas.drawPath(mDrawPath, mChartPaint);
            mDrawPath.reset();
            mDrawPath.moveTo(mappedX[mappedX.length - 1], mappedY[mappedY.length - 1]);
            previous = mappedY;
        }
        if (!drawInChartView)
            canvas.restore();
    }

    private void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaEndY, mDividerPaint);
    }

    private void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
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

        ChartArea[] areas = getActiveChartAreas();
        mPlateXValuePaint.setTextSize(mTextSizeLargePx);
        mPlateXValuePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.1f, mPlateXValuePaint);

        mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateNamePaint.setTextSize(mTextSizeMediumPx);
        mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
        mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
        float heightOffset = 0.24f;
        for (ChartArea area : areas){
            mPlateYValuePaint.setColor(area.Data.color);
            canvas.drawText(String.valueOf(area.Percentages[mPositionOfChosenPoint - mPointsMinIndex]) + "% " + area.Data.name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
            canvas.drawText(String.valueOf(area.Data.posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
            heightOffset += 0.14f;
        }
    }

    private void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
    {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mChartDrawingAreaStartX, mChartDrawingAreaEndY, mChartDrawingAreaEndX, mChartDrawingAreaEndY, mDividerPaint);

        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / (Y_DIVIDERS_COUNT - 1);

        float startY = mChartDrawingAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 2; i++) {
            canvas.drawLine(mChartDrawingAreaStartX, startY, mChartDrawingAreaEndX, stopY, mDividerPaint);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    private void drawYLabels (long height, long yMax, int alpha, boolean left, Canvas canvas) {
        float xCoord;
        if (left) {
            mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
            xCoord = mChartDrawingAreaStartX;
        }
        else {
            mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
            xCoord = mChartDrawingAreaEndX;
        }
        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / (Y_DIVIDERS_COUNT - 1);

        long step = 0;
        float yLabelCoord = mChartDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setAlpha(alpha);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }

    }

    @Override
    protected void showPointDetails(float xCoord) {
        if (!showChartAreas())
            return;

        super.showPointDetails(xCoord);
    }

    private boolean showChartAreas()
    {
        for (ChartArea area : mAreas)
            if (area.isVisible())
                return true;

        return false;
    }

    private ChartArea[] getActiveChartAreas()
    {
        ArrayList<ChartArea> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.isVisible())
                arrayList.add(area);

        return arrayList.toArray(new ChartArea[arrayList.size()]);
    }

    private float getMaxPosYCoefficient() {
        float max = 0;
        for (ChartArea area : mAreas) {
            if (area.PosYCoefficient > max)
                max = area.PosYCoefficient;
        }

        return max;
    }
}

