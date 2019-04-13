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
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.ScrollChartView;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StackedAreaChartDrawer implements ChartDrawer {

    public class ChartArea
    {
        public LineData Data;

        public int[] Percentages;

        public float PosYCoefficientStart;
        public float PosYCoefficient;
        public float PosYCoefficientEnd;

        public float[] MappedPointsY;

        public boolean isVisible() {
            return (PosYCoefficient > 0 || PosYCoefficientEnd > 0);
        }
    }

    protected final int   DRAWING_AREA_OFFSET_X_DP = 8;
    protected final int   DRAWING_AREA_OFFSET_Y_DP = 16;
    protected final int   Y_DIVIDERS_COUNT         = 5;
    protected final int   TEXT_SIZE_DP             = 12;
    protected final int   TEXT_LABEL_WIDTH_DP      = 36;
    protected final int   TEXT_LABEL_DISTANCE_DP   = 22;
    protected final int   PLATE_WIDTH_DP           = 120;
    protected final int   PLATE_HEIGHT_DP          = 180;
    protected final int   TEXT_SIZE_SMALL_DP       = 8;
    protected final int   TEXT_SIZE_MEDIUM_DP      = 12;
    protected final int   TEXT_SIZE_LARGE_DP       = 14;

    protected Resources.Theme mTheme;
    protected Context mContext;

    protected Paint mChartPaint;
    protected Paint     mDividerPaint;
    protected TextPaint mBaseLabelPaint;
    protected Paint     mPlatePaint;
    protected TextPaint mPlateXValuePaint;
    protected TextPaint mPlateYValuePaint;
    protected TextPaint mPlateNamePaint;

    protected final float mTextSizePx;
    protected final float mDateWidthPx;
    protected final float mDateDistancePx;
    protected final float mPlateWidthPx;
    protected final float mPlateHeightPx;
    protected final float mTextSizeSmallPx;
    protected final float mTextSizeMediumPx;
    protected final float mTextSizeLargePx;
    protected final float mDrawingAreaOffsetXPx;
    protected final float mDrawingAreaOffsetYPx;

    protected ArrayList<ChartArea> mAreas = new ArrayList<>();
    protected long[] mPosX;
    protected long  mPos1 = -1;
    protected long  mPos2 = -1;
    protected boolean mBordersSet;
    protected float[] mMappedPointsX;
    protected float mNormWidth;
    protected int mPointsMinIndex;
    protected int mPointsMaxIndex;

    protected ValueAnimator        mSetLinesAnimator;
    protected ValueAnimator.AnimatorUpdateListener mViewAnimatorListener;

    protected float mViewWidth;
    protected float mViewHeight;
    protected float mDrawingAreaStartX;
    protected float mDrawingAreaEndX;
    protected float mDrawingAreaStartY;
    protected float mDrawingAreaEndY;
    protected float mDrawingAreaWidth;
    protected float mDrawingAreaHeight;
    protected float mXLabelsYCoordinate;

    protected boolean mPointIsChosen = false;
    protected float mXCoordinateOfTouch;
    protected int mPositionOfChosenPoint;

    protected boolean mSetLinesFirstTime = true;

    protected HashMap<Integer, Float> mXLabelsPeriodToMinChartWidthPx = new HashMap<>();
    protected int                     mXLabelsPeriodCurrent;

    protected Path mDrawPath;

    public StackedAreaChartDrawer(Context context, ChartData chartData) {
        mContext = context;
        mTheme = context.getTheme();

        mTextSizePx     = MathUtils.dpToPixels(TEXT_SIZE_DP,           context);
        mDateWidthPx    = MathUtils.dpToPixels(TEXT_LABEL_WIDTH_DP,    context);
        mDateDistancePx = MathUtils.dpToPixels(TEXT_LABEL_DISTANCE_DP, context);
        mPlateWidthPx = MathUtils.dpToPixels(PLATE_WIDTH_DP, context);
        mPlateHeightPx = MathUtils.dpToPixels(PLATE_HEIGHT_DP, context);
        mTextSizeSmallPx = MathUtils.dpToPixels(TEXT_SIZE_SMALL_DP, context);
        mTextSizeMediumPx = MathUtils.dpToPixels(TEXT_SIZE_MEDIUM_DP, context);
        mTextSizeLargePx = MathUtils.dpToPixels(TEXT_SIZE_LARGE_DP, context);
        mDrawingAreaOffsetXPx = MathUtils.dpToPixels(DRAWING_AREA_OFFSET_X_DP, context);
        mDrawingAreaOffsetYPx = MathUtils.dpToPixels(DRAWING_AREA_OFFSET_Y_DP, context);

        for (LineData lineData : chartData.lines)
        {
            ChartArea chartArea = new ChartArea();
            chartArea.Data = lineData;
            chartArea.PosYCoefficient = 1;
            chartArea.PosYCoefficientStart = 1;
            chartArea.PosYCoefficientEnd = 1;
            mAreas.add(chartArea);
        }
        calculatePercentages();

        setUpPaints();

        mPosX = chartData.posX;

        mDrawPath = new Path();
    }

    public void draw(Canvas canvas) {
        if (mBordersSet)
            drawScaleX(mMappedPointsX, canvas);

        if (!showChartAreas()) {
            drawScaleY(125, 125, 255, canvas);
            drawYLabels(125, 125, 255, true, canvas);
            return;
        }

        drawAreas(canvas);

        drawScaleY(125, 125, 255, canvas);
        drawYLabels(125, 125, 255, true, canvas);

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mMappedPointsX, mXCoordinateOfTouch);
            drawVerticalDivider(mMappedPointsX, canvas);
            drawChosenPointPlate(mMappedPointsX, canvas);
        }
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaStartX, float drawingAreaEndX, float drawingAreaStartY, float drawingAreaEndY) {
        mViewWidth = width;
        mViewHeight = height;
        mDrawingAreaStartX = drawingAreaStartX;
        mDrawingAreaEndX = drawingAreaEndX;
        mDrawingAreaStartY = drawingAreaStartY;
        mDrawingAreaEndY = drawingAreaEndY;
        mDrawingAreaWidth = mDrawingAreaEndX - mDrawingAreaStartX;
        mDrawingAreaHeight = mDrawingAreaEndY - mDrawingAreaStartY;

        mXLabelsYCoordinate = mDrawingAreaEndY + MathUtils.dpToPixels(13, mContext);

        float minChartWidth = mDrawingAreaWidth;
        float maxChartWidth = minChartWidth / ScrollChartView.MINIMAL_NORM_SLIDER_WIDTH;
        int sizeOfArray = mPosX.length;
        for (int i = 1; true; i = i * 2) {
            int textElemsCount = sizeOfArray / i;
            float chartWidth = mDateWidthPx * textElemsCount + mDateDistancePx * (textElemsCount - 1);
            if (chartWidth >= minChartWidth && chartWidth <= maxChartWidth)
                mXLabelsPeriodToMinChartWidthPx.put(i, chartWidth);
            else if (chartWidth < minChartWidth) {
                mXLabelsPeriodToMinChartWidthPx.put(i, minChartWidth);
                break;
            }
        }

        mapXPoints();
    }

    public void setAnimatorUpdateListener (ValueAnimator.AnimatorUpdateListener listener) {
        mViewAnimatorListener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, float x, float y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                if (y >= mDrawingAreaEndY || x <= mDrawingAreaStartX || x >= mDrawingAreaEndX) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(x);
                }
        }
    }

    public boolean setBorders (float normPosX1, float normPosX2) {
        mBordersSet = true;

        mNormWidth = normPosX2 - normPosX1;
        long pos1;
        long pos2;
        long xMin = MathUtils.getMin(mPosX);
        long xMax = MathUtils.getMax(mPosX);
        long width = xMax - xMin;
        pos1 = (long) Math.floor(normPosX1 * width) + xMin;
        pos2 = (long) Math.ceil(normPosX2 * width) + xMin;

        boolean result = false;
        if (mPos1 != pos1 || mPos2 != pos2)
            result = true;

        mPos1 = pos1;
        mPos2 = pos2;

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * mDrawingAreaOffsetXPx) / mDrawingAreaWidth);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        calculatePercentages();
        mapXPoints();
        mapYPoints(getMaxPosYCoefficient());

        hidePointDetails();

        return result;
    }

    public void setLines (LineData[] lines) {

        for (ChartArea area : mAreas) {
            if(!Arrays.asList(lines).contains(area.Data)) {
                if (!mSetLinesFirstTime) {
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = 0;
                }
            }
            else {
                if (!mSetLinesFirstTime) {
                    area.PosYCoefficientStart = area.PosYCoefficient;
                    area.PosYCoefficientEnd = 1;
                }
            }
        }

        if (!mSetLinesFirstTime)
            startSetLinesAnimation();

        hidePointDetails();

        mSetLinesFirstTime = false;
    }

    protected void startSetLinesAnimation()
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
                mapYPoints(getMaxPosYCoefficient());
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    protected boolean showChartAreas()
    {
        for (ChartArea area : mAreas)
            if (area.isVisible())
                return true;

        return false;
    }

    protected ChartArea[] getActiveChartAreas()
    {
        ArrayList<ChartArea> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.isVisible())
                arrayList.add(area);

        return arrayList.toArray(new ChartArea[arrayList.size()]);
    }

    protected float getMaxPosYCoefficient() {
        float max = 0;
        for (ChartArea area : mAreas) {
            if (area.PosYCoefficient > max)
                max = area.PosYCoefficient;
        }

        return max;
    }

    protected void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.FILL);

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
        mBaseLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));

        mPlatePaint = new Paint();

        mPlateXValuePaint = new TextPaint();
        TypedValue textColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.labelTextColor, textColor, true)) {
            mPlateXValuePaint.setColor(textColor.data);
        }
        mPlateXValuePaint.setTextAlign(Paint.Align.CENTER);
        mPlateXValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateYValuePaint = new TextPaint();
        mPlateYValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateNamePaint = new TextPaint();
        mPlateNamePaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
    }

    protected void calculatePercentages() {
        if (!mBordersSet || mAreas == null || mAreas.size() == 0 || !showChartAreas())
            return;

        float[] sums = new float[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : mAreas) {
            area.Percentages = new int[mPointsMaxIndex - mPointsMinIndex + 1];
            for (int i = 0, j = mPointsMinIndex; j <= mPointsMaxIndex; i++, j++) {
                sums[i] += area.PosYCoefficient * area.Data.posY[j];
            }
        }

        for (int i = 0; i < sums.length; i++) {
            float[] assumedPercentages = new float[mAreas.size()];
            for (int j = 0; j < mAreas.size(); j++) {
                assumedPercentages[j] = mAreas.get(j).PosYCoefficient * mAreas.get(j).Data.posY[i + mPointsMinIndex] / sums[i] * 100;
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

    protected void mapXPoints()
    {
        if (!mBordersSet)
            return;

        mMappedPointsX = mapXPoints(mPosX, mPos1, mPos2);
    }

    protected void mapYPoints (float coefficient) {
        if (!mBordersSet || !showChartAreas())
            return;

        long calculatedArea = 100;

        int[] previous = new int[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : mAreas) {
            area.MappedPointsY = new float[mPointsMaxIndex - mPointsMinIndex + 1];
            for (int i = 0, j = mPointsMinIndex; i < area.MappedPointsY.length; i++, j++) {
                float percentage = (float) (area.Percentages[i] + previous[i]) / (float) calculatedArea;
                area.MappedPointsY[i] = coefficient * mDrawingAreaHeight * percentage + mDrawingAreaStartY;
                area.MappedPointsY[i] = mDrawingAreaEndY - area.MappedPointsY[i] + mDrawingAreaStartY;
                previous[i] += area.Percentages[i];
            }
        }
    }

    protected float[] mapXPoints (long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1];
        for (int i = 0, j = mPointsMinIndex; i < mapped.length; i++, j++) {
            float percentage = (float) (points[j] - xMin) / (float) calculatedArea;
            mapped[i] = mDrawingAreaStartX + mDrawingAreaWidth * percentage;
        }

        return mapped;
    }

    protected int mapCoordinateToPoint(float[] mappedX, float xCoord) {
        int position = MathUtils.getIndexOfNearestElement(mappedX, xCoord);
        while (mPosX[position + mPointsMinIndex] < mPos1) {
            position++;
        }
        while (mPosX[position + mPointsMinIndex] > mPos2) {
            position--;
        }
        return position + mPointsMinIndex;
    }

    protected void drawAreas(Canvas canvas) {
        if (mAreas == null || mAreas.size() == 0)
            return;

        if (!mDrawPath.isEmpty())
            mDrawPath.reset();

        mDrawPath.moveTo(mMappedPointsX[mMappedPointsX.length - 1], mDrawingAreaEndY);
        //mDrawPath.lineTo(0, mDrawingAreaEndY);
        float[] previous = new float[mAreas.get(0).MappedPointsY.length];
        Arrays.fill(previous, mDrawingAreaEndY);
        for (ChartArea area : mAreas) {
            for (int i = previous.length - 1; i >= 0; i--) {
                mDrawPath.lineTo(mMappedPointsX[i], previous[i]);
            }
            for (int i = 0; i < area.MappedPointsY.length; i++) {
                mDrawPath.lineTo(mMappedPointsX[i], area.MappedPointsY[i]);
            }
            mChartPaint.setColor(area.Data.color);
            canvas.drawPath(mDrawPath, mChartPaint);
            mDrawPath.reset();
            mDrawPath.moveTo(mMappedPointsX[mMappedPointsX.length - 1], area.MappedPointsY[area.MappedPointsY.length - 1]);
            previous = area.MappedPointsY;
        }
    }

    protected void drawScaleX (float[] mappedX, Canvas canvas) {

        float chartWidthPx = mDrawingAreaWidth / mNormWidth;
        int xLabelPeriodicity;

        for (xLabelPeriodicity = 1; true; xLabelPeriodicity = xLabelPeriodicity * 2) {
            if (!mXLabelsPeriodToMinChartWidthPx.containsKey(xLabelPeriodicity))
                continue;

            if (mXLabelsPeriodToMinChartWidthPx.get(xLabelPeriodicity) <= chartWidthPx)
                break;
        }
        mXLabelsPeriodCurrent = xLabelPeriodicity;

        TypedValue baseLabelColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.baseLabelColor, baseLabelColor, true)) {
            mBaseLabelPaint.setColor(baseLabelColor.data);
        }
        mBaseLabelPaint.setAlpha(255);
        mBaseLabelPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = mPointsMinIndex, j = 0; i <= mPointsMaxIndex; i++, j++) {
            if ((mPosX.length - 1 - i) % mXLabelsPeriodCurrent == 0) {
                if (i == mPosX.length - 1) {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j]- MathUtils.dpToPixels(9, mContext), mXLabelsYCoordinate, mBaseLabelPaint);
                }
                else {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j], mXLabelsYCoordinate, mBaseLabelPaint);
                }
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

            for (int i = mPointsMinIndex, j = 0; i <= mPointsMaxIndex; i++, j++) {
                if ((mPosX.length - 1 - i) % (mXLabelsPeriodCurrent / 2) == 0 && (mPosX.length - 1 - i) % mXLabelsPeriodCurrent != 0) {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j], mXLabelsYCoordinate, mBaseLabelPaint);
                }
            }
        }
    }

    protected void drawScaleY (float height, long yMax, int alpha, Canvas canvas)
    {
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / (Y_DIVIDERS_COUNT - 1);

        float startY = mDrawingAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 2; i++) {
            canvas.drawLine(mDrawingAreaStartX, startY, mDrawingAreaEndX, stopY, mDividerPaint);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    protected void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mDrawingAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], mDrawingAreaEndY, mDividerPaint);
    }

    protected void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
        //plate
        float top = mDrawingAreaHeight * 0.05f + mDrawingAreaWidth * 0.05f;
        float bottom = top + mPlateHeightPx;
        float left;
        float right;
        float offset = mDrawingAreaWidth * 0.05f;
        if ((mappedX[mPositionOfChosenPoint - mPointsMinIndex] + offset + mPlateWidthPx) >= mDrawingAreaEndX) {
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
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.1f, mPlateXValuePaint);

        mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateNamePaint.setTextSize(mTextSizeMediumPx);
        mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
        mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
        float heightOffset = 0.2f;
        long sumOfChosenValues = 0;
        for (ChartArea area : areas){
            mPlateYValuePaint.setColor(area.Data.color);
            mPlateNamePaint.setColor(area.Data.color);
            canvas.drawText(String.valueOf(area.Percentages[mPositionOfChosenPoint - mPointsMinIndex]) + "% " + area.Data.name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
            canvas.drawText(String.valueOf(area.Data.posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
            heightOffset += 0.1f;
            sumOfChosenValues += area.Data.posY[mPositionOfChosenPoint];
        }
        mPlateNamePaint.setColor(Color.BLACK);
        mPlateYValuePaint.setColor(Color.BLACK);
        canvas.drawText("All", left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
        canvas.drawText(String.valueOf(sumOfChosenValues), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
    }

    private void drawYLabels (float height, long yMax, int alpha, boolean left, Canvas canvas) {
        float xCoord;
        if (left) {
            mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
            xCoord = mDrawingAreaStartX;
        }
        else {
            mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
            xCoord = mDrawingAreaEndX;
        }
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / (Y_DIVIDERS_COUNT - 1);

        long step = 0;
        float yLabelCoord = mDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setAlpha(alpha);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }

    }

    protected void showPointDetails(float xCoord) {
        if (!showChartAreas())
            return;
        mXCoordinateOfTouch = xCoord;
        mPointIsChosen = true;
    }

    protected void hidePointDetails() {
        mPointIsChosen = false;
    }


}

