package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.TypedValue;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class StackedAreaChartDrawer extends BaseChartDrawer {

    public class ChartArea
    {
        public LineData Data;

        public int[]    Percentages;

        float[]         ChartMappedPointsYStart;
        float[]         ChartMappedPointsY;
        float[]         ChartMappedPointsYEnd;

        float[]         ScrollMappedPointsYStart;
        float[]         ScrollMappedPointsY;
        float[]         ScrollMappedPointsYEnd;

        boolean         isVisible;
        boolean         isActive;
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
            chartArea.isActive = true;
            chartArea.isVisible = true;
            mAreas.add(chartArea);
        }

        //NEED?
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
        mapYPointsForChartView();
        mapYPointsForScrollView();

        return result;
    }

    public void setLines (LineData[] lines) {
        for (ChartArea area : mAreas) {
            if(!Arrays.asList(lines).contains(area.Data)) {
                area.isActive = false;
            }
            else {
                area.isActive = true;
                area.isVisible = true;
            }
        }

        if (!mSetLinesFirstTime) {
            calculateEndYPoints();

            for (int i = 0; i < mAreas.size(); i++) {
                mAreas.get(i).ChartMappedPointsYStart = mAreas.get(i).ChartMappedPointsY;
                mAreas.get(i).ScrollMappedPointsYStart = mAreas.get(i).ScrollMappedPointsY;
                if (!mAreas.get(i).isActive && mAreas.get(i).isVisible) {
                    boolean coordinatesSet = false;
                    for (int j = i - 1; j >= 0 && !coordinatesSet; j--) {
                        if (mAreas.get(j).isActive) {
                            mAreas.get(i).ChartMappedPointsYEnd = mAreas.get(j).ChartMappedPointsYEnd;
                            mAreas.get(i).ScrollMappedPointsYEnd = mAreas.get(j).ScrollMappedPointsYEnd;
                            coordinatesSet = true;
                        }
                    }
                    if (!coordinatesSet) {
                        mAreas.get(i).ChartMappedPointsYEnd = new float[mAreas.get(i).ChartMappedPointsY.length];
                        mAreas.get(i).ScrollMappedPointsYEnd = new float[mAreas.get(i).ScrollMappedPointsY.length];
                        Arrays.fill(mAreas.get(i).ChartMappedPointsYEnd, mChartDrawingAreaEndY);
                        Arrays.fill(mAreas.get(i).ScrollMappedPointsYEnd, mScrollDrawingAreaEndY);
                    }
                }
            }

            startSetLinesAnimation();
        }
        else {
            mapXPointsForChartView();
            mapXPointsForScrollView();
            mapYPointsForChartView();
            mapYPointsForScrollView();
        }

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

                ChartArea[] areas = getActiveChartAreas();
                for (ChartArea area : areas) {
                    for (int i = 0; i < area.ChartMappedPointsY.length; i++) {
                        area.ChartMappedPointsY[i] = MathUtils.lerp(area.ChartMappedPointsYStart[i], area.ChartMappedPointsYEnd[i], t);
                    }
                    for (int i = 0; i < area.ScrollMappedPointsY.length; i++) {
                        area.ScrollMappedPointsY[i] = MathUtils.lerp(area.ScrollMappedPointsYStart[i], area.ScrollMappedPointsYEnd[i], t);
                    }
                }

                if (t == 1) {
                    for (ChartArea area : mAreas) {
                        //NEED?
                        area.ChartMappedPointsYStart = area.ChartMappedPointsY;
                        area.ScrollMappedPointsYStart = area.ScrollMappedPointsY;
                        if (!area.isActive && area.isVisible)
                            area.isVisible = false;
                    }
                }
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    private void calculatePercentages() {
        if (!mBordersSet || mAreas == null || mAreas.size() == 0 || !showChartAreas())
            return;

        ChartArea[] areas = getActiveChartAreas();
        if (areas == null || areas.length == 0)
            return;

        float[] sums = new float[areas[0].Data.posY.length];
        for (ChartArea area : areas) {
            area.Percentages = new int[sums.length];
            for (int i = 0; i < area.Percentages.length; i++) {
                sums[i] += area.Data.posY[i];
            }
        }

        for (int i = 0; i < sums.length; i++) {
            float[] assumedPercentages = new float[areas.length];
            for (int j = 0; j < areas.length; j++) {
                assumedPercentages[j] = areas[j].Data.posY[i] / sums[i] * 100;
            }
            int sum = 0;
            for (int n = 0; n < assumedPercentages.length; n++) {
                int floor = (int) Math.floor(assumedPercentages[n]);
                sum += floor;
                areas[n].Percentages[i] = floor;
                assumedPercentages[n] = assumedPercentages[n] - floor;
            }

            int remaining = 100 - sum;
            for (int j = 0; j < remaining; j++) {
                int index = MathUtils.getMaxIndex(assumedPercentages);
                assumedPercentages[index] = -1;
                areas[index].Percentages[i]++;
            }
        }
    }

    private void mapYPointsForChartView() {
        if (!mBordersSet || !showChartAreas())
            return;

        ChartArea[] areas = getActiveChartAreas();

        long calculatedArea = 100;

        int[] previous = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : areas) {
            area.ChartMappedPointsY = new float[mPointsMaxIndex - mPointsMinIndex + 1];
            for (int i = 0, j = mPointsMinIndex; i < area.ChartMappedPointsY.length; i++, j++) {
                float percentage = (float) (area.Percentages[j] + previous[i]) / (float) calculatedArea;
                area.ChartMappedPointsY[i] = mChartDrawingAreaHeight * percentage + mChartDrawingAreaStartY;
                area.ChartMappedPointsY[i] = mChartDrawingAreaEndY - area.ChartMappedPointsY[i] + mChartDrawingAreaStartY;
                previous[i] += area.Percentages[j];
            }
        }
    }

    private void mapYPointsForScrollView() {
        if (!showChartAreas())
            return;

        ChartArea[] areas = getActiveChartAreas();

        long calculatedArea = 100;

        int[] previous = new int[mScrollMappedPointsX.length];
        for (ChartArea area : areas) {
            area.ScrollMappedPointsY = new float[mScrollMappedPointsX.length];
            for (int i = 0; i < area.ScrollMappedPointsY.length; i++) {
                float percentage = (float) (area.Percentages[i] + previous[i]) / (float) calculatedArea;
                area.ScrollMappedPointsY[i] = mScrollDrawingAreaHeight * percentage + mScrollDrawingAreaStartY;
                area.ScrollMappedPointsY[i] = mScrollDrawingAreaEndY - area.ScrollMappedPointsY[i] + mScrollDrawingAreaStartY;
                previous[i] += area.Percentages[i];
            }
        }
    }

    private void calculateEndYPoints() {
        if (!mBordersSet || !showChartAreas())
            return;

        calculatePercentages();

        ChartArea[] areas = getActiveChartAreas();

        long calculatedArea = 100;

        int[] previous = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : areas) {
            area.ChartMappedPointsYEnd = new float[mPointsMaxIndex - mPointsMinIndex + 1];
            for (int i = 0, j = mPointsMinIndex; i < area.ChartMappedPointsYEnd.length; i++, j++) {
                float percentage = (float) (area.Percentages[j] + previous[i]) / (float) calculatedArea;
                area.ChartMappedPointsYEnd[i] = mChartDrawingAreaHeight * percentage + mChartDrawingAreaStartY;
                area.ChartMappedPointsYEnd[i] = mChartDrawingAreaEndY - area.ChartMappedPointsYEnd[i] + mChartDrawingAreaStartY;
                previous[i] += area.Percentages[j];
            }

            previous = new int[mScrollMappedPointsX.length];
            area.ScrollMappedPointsYEnd = new float[mScrollMappedPointsX.length];
            for (int i = 0; i < area.ScrollMappedPointsYEnd.length; i++) {
                float percentage = (float) (area.Percentages[i] + previous[i]) / (float) calculatedArea;
                area.ScrollMappedPointsYEnd[i] = mScrollDrawingAreaHeight * percentage + mScrollDrawingAreaStartY;
                area.ScrollMappedPointsYEnd[i] = mScrollDrawingAreaEndY - area.ScrollMappedPointsYEnd[i] + mScrollDrawingAreaStartY;
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
        ChartArea[] areas = getVisibleChartAreas();
        if (areas == null || areas.length == 0)
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
            previous = new float[areas[0].ChartMappedPointsY.length];
        }
        else {
            previous = new float[areas[0].ScrollMappedPointsY.length];
        }
        Arrays.fill(previous, drawingStartPoint);
        for (ChartArea area : areas) {
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

        ChartArea[] areas = getVisibleChartAreas();
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
            if (area.isVisible)
                return true;

        return false;
    }

    private ChartArea[] getVisibleChartAreas()
    {
        ArrayList<ChartArea> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.isVisible)
                arrayList.add(area);

        return arrayList.toArray(new ChartArea[arrayList.size()]);
    }

    private ChartArea[] getActiveChartAreas()
    {
        ArrayList<ChartArea> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.isActive)
                arrayList.add(area);

        return arrayList.toArray(new ChartArea[arrayList.size()]);
    }
}

