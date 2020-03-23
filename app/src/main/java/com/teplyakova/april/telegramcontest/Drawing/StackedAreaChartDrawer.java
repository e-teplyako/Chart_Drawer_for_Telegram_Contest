package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Trace;
import android.util.Log;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
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

    private final float          PLATE_WIDTH_PX;

    private Paint                mChartPaint;

    private ArrayList<ChartArea> mAreas                 = new ArrayList<>();

    private ValueAnimator        mSetLinesAnimator;

    private boolean              mSetLinesFirstTime     = true;

    private Path[]               mChartPaths;
    private Path[]               mScrollPaths;

    public StackedAreaChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        PLATE_WIDTH_PX = MathUtils.dpToPixels(140, context);

        for (LineData lineData : chartData.getLines()) {
            ChartArea chartArea = new ChartArea();
            chartArea.Data = lineData;
            chartArea.Percentages = new int[lineData.posY.length];
            chartArea.ChartMappedPointsY = new float[lineData.posY.length];
            chartArea.ScrollMappedPointsY = new float[lineData.posY.length];
            chartArea.isActive = true;
            chartArea.isVisible = true;
            mAreas.add(chartArea);
        }

        calculatePercentages();
    }

    public void draw(Canvas canvas) {
        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!showVisibleChartAreas()) {
            drawScaleY(125, 125, 255, canvas);
            drawYLabels(125, 125, 255, canvas);
            drawRects(canvas);
            return;
        }

        drawAreas(canvas);
        drawAreas(canvas);

        drawScaleY(125, 125, 255, canvas);
        drawYLabels(125, 125, 255, canvas);

        if (mPointIsChosen) {
            drawVerticalDivider(mChartMappedPointsX, canvas);
            drawChosenPointPlate(mChartMappedPointsX, canvas);
        }

        drawRects(canvas);
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        super.setViewDimens(width, height, drawingAreaOffsetXPx, drawingAreaOffsetYPx, scrollDrawingAreaHeightPx);
        Log.e("DIMENS", "SET");
        mapXPointsForScrollView();
        mapYPointsForScrollView();
    }

    public boolean setBorders (float normPosX1, float normPosX2) {
        if (mSetLinesAnimator != null) {
            mSetLinesAnimator.cancel();
            mSetLinesAnimator = null;
        }
        boolean result = super.setBorders(normPosX1, normPosX2);

        mapXPointsForChartView();
        mapYPointsForChartView();

        return result;
    }

    public void setLines (LineData[] lines) {
        if (lines == null || lines.length == 0)
            hidePointDetails();

        for (int i = 0; i < mAreas.size(); i++) {
            if (!Arrays.asList(lines).contains(mAreas.get(i).Data)) {
                mAreas.get(i).isActive = false;
            } else {
                mAreas.get(i).isActive = true;
            }
        }

        if (!mSetLinesFirstTime) {
            calculatePercentages();
            calculateEndYPoints();

            for (int i = 0; i < mAreas.size(); i++) {
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
                        Arrays.fill(mAreas.get(i).ChartMappedPointsYEnd, chartAreaEndY);
                        Arrays.fill(mAreas.get(i).ScrollMappedPointsYEnd, scrollAreaEndY);
                    }
                    mAreas.get(i).ChartMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ChartMappedPointsY, mAreas.get(i).ChartMappedPointsY.length);
                    mAreas.get(i).ScrollMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ScrollMappedPointsY, mAreas.get(i).ScrollMappedPointsY.length);
                }
                else if (mAreas.get(i).isActive && !mAreas.get(i).isVisible) {
                    boolean coordinatesSet = false;
                    for (int j = i - 1; j >= 0 && !coordinatesSet; j--) {
                        if (mAreas.get(j).isActive) {
                            mAreas.get(i).ChartMappedPointsY = Arrays.copyOf(mAreas.get(j).ChartMappedPointsY, mAreas.get(j).ChartMappedPointsY.length);
                            mAreas.get(i).ScrollMappedPointsY = Arrays.copyOf(mAreas.get(j).ScrollMappedPointsY, mAreas.get(j).ScrollMappedPointsY.length);
                            coordinatesSet = true;
                        }
                    }
                    if (!coordinatesSet) {
                        mAreas.get(i).ChartMappedPointsY = new float[mAreas.get(i).ChartMappedPointsYEnd.length];
                        mAreas.get(i).ScrollMappedPointsY = new float[mAreas.get(i).ScrollMappedPointsYEnd.length];
                        Arrays.fill(mAreas.get(i).ChartMappedPointsY, chartAreaEndY);
                        Arrays.fill(mAreas.get(i).ScrollMappedPointsY, scrollAreaEndY);
                    }
                    mAreas.get(i).isVisible = true;
                    mAreas.get(i).ChartMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ChartMappedPointsY, mAreas.get(i).ChartMappedPointsY.length);
                    mAreas.get(i).ScrollMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ScrollMappedPointsY, mAreas.get(i).ScrollMappedPointsY.length);
                }
                else if (mAreas.get(i).isActive && mAreas.get(i).isVisible) {
                    mAreas.get(i).ChartMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ChartMappedPointsY, mAreas.get(i).ChartMappedPointsY.length);
                    mAreas.get(i).ScrollMappedPointsYStart = Arrays.copyOf(mAreas.get(i).ScrollMappedPointsY, mAreas.get(i).ScrollMappedPointsY.length);
                }
            }
            startSetLinesAnimation();
        }
        else {
            for (ChartArea area : mAreas) {
                if (area.isActive)
                    area.isVisible = true;
                else
                    area.isVisible = false;
            }
            calculatePercentages();
            mapXPointsForChartView();
            mapYPointsForChartView();
            mapXPointsForScrollView();
            mapYPointsForScrollView();
        }

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
                    if (area.isVisible) {
                        for (int i = 0; i < area.ChartMappedPointsY.length; i++) {
                            area.ChartMappedPointsY[i] = MathUtils.lerp(area.ChartMappedPointsYStart[i], area.ChartMappedPointsYEnd[i], t);
                        }
                        for (int i = 0; i < area.ScrollMappedPointsY.length; i++) {
                            area.ScrollMappedPointsY[i] = MathUtils.lerp(area.ScrollMappedPointsYStart[i], area.ScrollMappedPointsYEnd[i], t);
                        }
                    }
                    if (t == 1 &&!area.isActive && area.isVisible) {
                        area.isVisible = false;
                    }
                    prepareChartPaths();
                    prepareScrollPaths();
                }
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    private void calculatePercentages() {
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
        if (!mBordersSet)
            return;

        long calculatedArea = 100;

        int[] previous = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : mAreas) {
            if (area.isActive) {
                area.ChartMappedPointsY = new float[mPointsMaxIndex - mPointsMinIndex + 1];
                for (int i = 0, j = mPointsMinIndex; i < area.ChartMappedPointsY.length; i++, j++) {
                    float percentage = (float) (area.Percentages[j] + previous[i]) / (float) calculatedArea;
                    area.ChartMappedPointsY[i] = chartAreaHeightPx * percentage + chartAreaStartY;
                    area.ChartMappedPointsY[i] = chartAreaEndY - area.ChartMappedPointsY[i] + chartAreaStartY;
                    previous[i] += area.Percentages[j];
                }
            }
        }
        prepareChartPaths();
    }

    private void mapYPointsForScrollView() {
        long calculatedArea = 100;

        int[] previous = new int[mAreas.get(0).Data.posY.length];
        for (ChartArea area : mAreas) {
            if (area.isActive) {
                area.ScrollMappedPointsY = new float[previous.length];
                for (int i = 0; i < area.ScrollMappedPointsY.length; i++) {
                    float percentage = (float) (area.Percentages[i] + previous[i]) / (float) calculatedArea;
                    area.ScrollMappedPointsY[i] = scrollAreaHeightPx * percentage + scrollAreaStartY;
                    area.ScrollMappedPointsY[i] = scrollAreaEndY - area.ScrollMappedPointsY[i] + scrollAreaStartY;
                    previous[i] += area.Percentages[i];
                }
            }
        }
        prepareScrollPaths();
    }

    private void calculateEndYPoints() {
        if (Build.VERSION.SDK_INT > 18)
            Trace.beginSection("calculateEndYPoints");
        if (!mBordersSet)
            return;

        long calculatedArea = 100;

        int[] previousChart = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        int[] previousScroll = new int[mScrollMappedPointsX.length];
        for (ChartArea area : mAreas) {
            if (area.isActive) {
                area.ChartMappedPointsYEnd = new float[mPointsMaxIndex - mPointsMinIndex + 1];
                for (int i = 0, j = mPointsMinIndex; i < area.ChartMappedPointsYEnd.length; i++, j++) {
                    float percentage = (float) (area.Percentages[j] + previousChart[i]) / (float) calculatedArea;
                    area.ChartMappedPointsYEnd[i] = chartAreaHeightPx * percentage + chartAreaStartY;
                    area.ChartMappedPointsYEnd[i] = chartAreaEndY - area.ChartMappedPointsYEnd[i] + chartAreaStartY;
                    previousChart[i] += area.Percentages[j];
                }

                area.ScrollMappedPointsYEnd = new float[mScrollMappedPointsX.length];
                for (int i = 0; i < area.ScrollMappedPointsYEnd.length; i++) {
                    float percentage = (float) (area.Percentages[i] + previousScroll[i]) / (float) calculatedArea;
                    area.ScrollMappedPointsYEnd[i] = scrollAreaHeightPx * percentage + scrollAreaStartY;
                    area.ScrollMappedPointsYEnd[i] = scrollAreaEndY - area.ScrollMappedPointsYEnd[i] + scrollAreaStartY;
                    previousScroll[i] += area.Percentages[i];
                }
            }
        }
        if (Build.VERSION.SDK_INT > 18)
            Trace.endSection();
    }

    private void prepareChartPaths() {
        if (!mBordersSet)
            return;

        ChartArea[] areas = getVisibleChartAreas();
        mChartPaths = new Path[areas.length];

        float[] previous = new float[mChartMappedPointsX.length];
        Arrays.fill(previous, chartAreaEndY);
        Path path = new Path();
        path.moveTo(mChartMappedPointsX[mChartMappedPointsX.length - 1], chartAreaEndY);
        for (int j = 0; j < areas.length; j++) {
            for (int i = previous.length - 1; i >= 0; i--) {
                path.lineTo(mChartMappedPointsX[i], previous[i]);
            }
            for (int i = 0; i < areas[j].ChartMappedPointsY.length; i++) {
                path.lineTo(mChartMappedPointsX[i], areas[j].ChartMappedPointsY[i]);
            }
            mChartPaths[j] = path;
            path = new Path();
            path.moveTo(mChartMappedPointsX[mChartMappedPointsX.length - 1], areas[j].ChartMappedPointsY[areas[j].ChartMappedPointsY.length - 1]);
            previous = areas[j].ChartMappedPointsY;
        }
    }

    private void prepareScrollPaths() {
        ChartArea[] areas = getVisibleChartAreas();
        mScrollPaths = new Path[areas.length];

        float[] previous = new float[mScrollMappedPointsX.length];
        Arrays.fill(previous, scrollAreaEndY);
        Path path = new Path();
        path.moveTo(mScrollMappedPointsX[mScrollMappedPointsX.length - 1], scrollAreaEndY);
        for (int j = 0; j < areas.length; j++) {
            for (int i = previous.length - 1; i >= 0; i--) {
                path.lineTo(mScrollMappedPointsX[i], previous[i]);
            }
            for (int i = 0; i < areas[j].ScrollMappedPointsY.length; i++) {
                path.lineTo(mScrollMappedPointsX[i], areas[j].ScrollMappedPointsY[i]);
            }
            mScrollPaths[j] = path;
            path = new Path();
            path.moveTo(mScrollMappedPointsX[mScrollMappedPointsX.length - 1], areas[j].ScrollMappedPointsY[areas[j].ScrollMappedPointsY.length - 1]);
            previous = areas[j].ScrollMappedPointsY;
        }
    }

    @Override
    protected void setUpPaints() {
        super.setUpPaints();

        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.FILL);
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    private void drawAreas(Canvas canvas) {
        ChartArea[] areas = getVisibleChartAreas();

        if (areas == null || areas.length == 0)
            return;

        for (int i = 0; i < mChartPaths.length; i++) {
            mChartPaint.setColor(areas[i].Data.color);
            canvas.drawPath(mChartPaths[i], mChartPaint);

            canvas.save();
            Path clipPath = new Path();
            RectF clipRect = new RectF(scrollAreaStartX, scrollAreaStartY, scrollAreaEndX, scrollAreaEndY);
            clipPath.addRoundRect(clipRect, 20, 20, Path.Direction.CW);
            canvas.clipPath(clipPath);
            canvas.drawPath(mScrollPaths[i], mChartPaint);
            canvas.restore();
        }
    }

    private void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        divider.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], chartAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], chartAreaEndY, divider);
    }

    private void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
        ChartArea[] areas = getActiveChartAreas();
        //plate
        float plateHeightPx = (3 + areas.length) * VERTICAL_TEXT_OFFSET_PX + (1 + areas.length) * TEXT_SIZE_MEDIUM_PX;
        float top = chartAreaHeightPx * 0.05f + chartAreaWidthPx * 0.05f;
        float bottom = top + plateHeightPx;
        float left;
        float right;
        float offset = chartAreaWidthPx * 0.05f;
        if ((mappedX[mPositionOfChosenPoint - mPointsMinIndex] + offset + PLATE_WIDTH_PX) >= chartAreaEndX) {
            right = mappedX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - PLATE_WIDTH_PX;
        } else {
            left = mappedX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
            right = left + PLATE_WIDTH_PX;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        int cornerRadius = 25;

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateBorder);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateFill);

        //text
        float textPosY = top + TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        plateXValue.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + PLATE_WIDTH_PX * 0.5f, textPosY, plateXValue);

        textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        for (ChartArea area : areas){
            plateYValue.setColor(area.Data.color);
            canvas.drawText(String.valueOf(area.Percentages[mPositionOfChosenPoint - mPointsMinIndex]) + "% " + area.Data.name, left + HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateName);
            canvas.drawText(String.valueOf(area.Data.posY[mPositionOfChosenPoint]), right - HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateYValue);
            textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        }
    }

    private void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
    {
        divider.setAlpha(255);
        canvas.drawLine(chartAreaStartX, chartAreaEndY, chartAreaEndX, chartAreaEndY, divider);

        float spaceBetweenDividers = (float)yMax / height * chartAreaHeightPx / (Y_DIVIDERS_COUNT - 1);

        float startY = chartAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        divider.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 2; i++) {
            canvas.drawLine(chartAreaStartX, startY, chartAreaEndX, stopY, divider);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    private void drawYLabels (long height, long yMax, int alpha, Canvas canvas) {
        float xCoord = chartAreaStartX;
        label.setTextAlign(Paint.Align.LEFT);

        float spaceBetweenDividers = (float)yMax / height * chartAreaHeightPx / (Y_DIVIDERS_COUNT - 1);

        long step = 0;
        float yLabelCoord = chartAreaEndY * 0.99f;

        label.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, label);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }
    }

    @Override
    protected void showPointDetails(int pointPosition) {
        if (!showVisibleChartAreas())
            return;

        super.showPointDetails(pointPosition);
    }

    private boolean showVisibleChartAreas()
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

