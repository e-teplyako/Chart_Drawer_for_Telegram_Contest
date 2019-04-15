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

public class StackedAreaChartDrawer implements ChartDrawer {

    public class ChartArea
    {
        public LineData Data;

        public int[] Percentages;

        public float PosYCoefficientStart;
        public float PosYCoefficient;
        public float PosYCoefficientEnd;

        public float[] ChartMappedPointsY;
        public float[] ScrollMappedPointsY;

        public boolean isVisible() {
            return (PosYCoefficient > 0 || PosYCoefficientEnd > 0);
        }
    }

    protected final int   Y_DIVIDERS_COUNT         = 5;
    protected final int   TEXT_SIZE_DP             = 12;
    protected final int   TEXT_LABEL_WIDTH_DP      = 36;
    protected final int   TEXT_LABEL_DISTANCE_DP   = 22;
    protected final int   PLATE_WIDTH_DP           = 120;
    protected final int   PLATE_HEIGHT_DP          = 180;
    protected final int   TEXT_SIZE_SMALL_DP       = 8;
    protected final int   TEXT_SIZE_MEDIUM_DP      = 12;
    protected final int   TEXT_SIZE_LARGE_DP       = 14;
    protected final int   SLIDER_WIDTH_DP                   = 6;
    protected final int   TOP_DATES_OFFSET_Y_DP             = 14;
    protected final float MINIMAL_NORM_SLIDER_WIDTH         = 0.2f;

    protected final float mTextSizePx;
    protected final float mDateWidthPx;
    protected final float mDateDistancePx;
    protected final float mPlateWidthPx;
    protected final float mPlateHeightPx;
    protected final float mTextSizeSmallPx;
    protected final float mTextSizeMediumPx;
    protected final float mTextSizeLargePx;
    protected final float mSliderWidthPx;
    protected final float mTopDatesOffsetYPx;

    protected Resources.Theme mTheme;
    protected Context mContext;

    protected Paint mChartPaint;
    protected Paint     mDividerPaint;
    protected TextPaint mBaseLabelPaint;
    protected Paint     mPlatePaint;
    protected TextPaint mPlateXValuePaint;
    protected TextPaint mPlateYValuePaint;
    protected TextPaint mPlateNamePaint;
    protected Paint mBackgroundPaint;
    protected Paint mSliderPaint;
    protected Paint mChosenAreaPaint;

    protected ArrayList<ChartArea> mAreas = new ArrayList<>();
    protected long[] mPosX;
    protected long  mPos1 = -1;
    protected long  mPos2 = -1;
    protected boolean mBordersSet;
    protected float[] mChartMappedPointsX;
    protected float[] mScrollMappedPointsX;
    protected float mNormWidth;
    protected int mPointsMinIndex;
    protected int mPointsMaxIndex;

    protected ValueAnimator        mSetLinesAnimator;
    protected ValueAnimator.AnimatorUpdateListener mViewAnimatorListener;

    protected float mViewWidth;
    protected float mViewHeight;
    protected float mChartDrawingAreaStartX;
    protected float mChartDrawingAreaEndX;
    protected float mChartDrawingAreaStartY;
    protected float mChartDrawingAreaEndY;
    protected float mChartDrawingAreaWidth;
    protected float mChartDrawingAreaHeight;
    protected float mScrollDrawingAreaStartX;
    protected float mScrollDrawingAreaEndX;
    protected float mScrollDrawingAreaStartY;
    protected float mScrollDrawingAreaEndY;
    protected float mScrollDrawingAreaWidth;
    protected float mScrollDrawingAreaHeight;
    protected float mDrawingAreaOffsetXPx;
    protected float mDrawingAreaOffsetYPx;
    protected float mXLabelsYCoordinate;

    protected boolean mPointIsChosen = false;
    protected float mXCoordinateOfTouch;
    protected int mPositionOfChosenPoint;

    protected boolean mSetLinesFirstTime = true;

    protected HashMap<Integer, Float> mXLabelsPeriodToMinChartWidthPx = new HashMap<>();
    protected int                     mXLabelsPeriodCurrent;

    protected Path mDrawPath;

    protected float mSliderPositionLeft;
    protected float mSliderPositionRight;
    protected float mNormSliderPosLeft = 0.8f;
    protected float mNormSliderPosRight = 1;

    protected RectF mBackGroundLeft;
    protected RectF mBackgroundRight;
    protected RectF mSliderLeft;
    protected RectF mSliderRight;
    protected RectF mChosenArea;

    protected boolean mLeftSliderIsCaught = false;
    protected boolean mRightSliderIsCaught = false;
    protected boolean mChosenAreaIsCaught = false;
    protected float mCurrChosenAreaPosition;
    protected float mCurrChosenAreaWidth;
    protected float mChosenAreaMinimalWidth;

    protected boolean mSliderPositionsSet = false;

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
        mTopDatesOffsetYPx = MathUtils.dpToPixels(TOP_DATES_OFFSET_Y_DP, context);
        mSliderWidthPx = MathUtils.dpToPixels(SLIDER_WIDTH_DP, context);

        for (LineData lineData : chartData.lines)
        {
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

        setUpPaints();

        mPosX = chartData.posX;

        mDrawPath = new Path();
        mBackGroundLeft = new RectF();
        mBackgroundRight = new RectF();
        mSliderLeft = new RectF();
        mSliderRight = new RectF();
        mChosenArea = new RectF();
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
        mViewWidth = width;
        mViewHeight = height;

        mChartDrawingAreaStartX = drawingAreaOffsetXPx;
        mChartDrawingAreaEndX = width - drawingAreaOffsetXPx;
        mChartDrawingAreaStartY = drawingAreaOffsetYPx;
        mChartDrawingAreaEndY = height - scrollDrawingAreaHeightPx - drawingAreaOffsetYPx;
        mChartDrawingAreaWidth = mChartDrawingAreaEndX - mChartDrawingAreaStartX;
        mChartDrawingAreaHeight = mChartDrawingAreaEndY - mChartDrawingAreaStartY;

        mScrollDrawingAreaStartX = drawingAreaOffsetXPx;
        mScrollDrawingAreaEndX = width - drawingAreaOffsetXPx;
        mScrollDrawingAreaStartY = mChartDrawingAreaEndY + drawingAreaOffsetYPx;
        mScrollDrawingAreaEndY = mScrollDrawingAreaStartY + scrollDrawingAreaHeightPx;
        mScrollDrawingAreaWidth = mScrollDrawingAreaEndX - mScrollDrawingAreaStartX;
        mScrollDrawingAreaHeight = mScrollDrawingAreaEndY - mScrollDrawingAreaStartY;

        mDrawingAreaOffsetXPx = drawingAreaOffsetXPx;
        mDrawingAreaOffsetYPx = drawingAreaOffsetYPx;

        mXLabelsYCoordinate = mChartDrawingAreaEndY + MathUtils.dpToPixels(13, mContext);

        mChosenAreaMinimalWidth = mScrollDrawingAreaWidth * MINIMAL_NORM_SLIDER_WIDTH;

        float minChartWidth = mChartDrawingAreaWidth;
        float maxChartWidth = minChartWidth / MINIMAL_NORM_SLIDER_WIDTH;
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

        mapXPointsForChartView();
        mapXPointsForScrollView();
        //mapYPointsForScrollView(1);
    }

    public void setAnimatorUpdateListener (ValueAnimator.AnimatorUpdateListener listener) {
        mViewAnimatorListener = listener;
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event, float x, float y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (y >= mChartDrawingAreaEndY || x <= mChartDrawingAreaStartX || x >= mChartDrawingAreaEndX) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(x);
                }
                if (x >= mScrollDrawingAreaStartX && x <= mScrollDrawingAreaEndX && y >= mScrollDrawingAreaStartY && y <= mScrollDrawingAreaEndY) {
                    if (mLeftSliderIsCaught){
                        setSliderPositions(normalizeSliderPosition(MathUtils.clamp(x, mSliderPositionRight - mChosenAreaMinimalWidth, mScrollDrawingAreaStartX)), normalizeSliderPosition(mSliderPositionRight));
                    }
                    else if (mRightSliderIsCaught) {
                        setSliderPositions(normalizeSliderPosition(mSliderPositionLeft), normalizeSliderPosition(MathUtils.clamp(x, mScrollDrawingAreaEndX, mSliderPositionLeft + mChosenAreaMinimalWidth)));
                    }
                    else if (mChosenAreaIsCaught) {
                        float deltaX = x - mCurrChosenAreaPosition;
                        setSliderPositions(normalizeSliderPosition(MathUtils.clamp(mSliderPositionLeft + deltaX, mScrollDrawingAreaEndX - mCurrChosenAreaWidth, mScrollDrawingAreaStartX)), normalizeSliderPosition(MathUtils.clamp(mSliderPositionRight + deltaX, mScrollDrawingAreaEndX, mScrollDrawingAreaStartX + mCurrChosenAreaWidth)));
                        mCurrChosenAreaPosition = x;
                    }

                    return true;
                }
            case MotionEvent.ACTION_DOWN:
                if (y >= mChartDrawingAreaEndY || x <= mChartDrawingAreaStartX || x >= mChartDrawingAreaEndX) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(x);
                }
                if (x >= mScrollDrawingAreaStartX && x <= mScrollDrawingAreaEndX && y >= mScrollDrawingAreaStartY && y <= mScrollDrawingAreaEndY) {
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
                }
            case MotionEvent.ACTION_UP:
                if (x >= mScrollDrawingAreaStartX && x <= mScrollDrawingAreaEndX && y >= mScrollDrawingAreaStartY && y <= mScrollDrawingAreaEndY) {
                    mRightSliderIsCaught = false;
                    mLeftSliderIsCaught = false;
                    mChosenAreaIsCaught = false;
                    return true;
                }
        }
        return true;
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

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * mDrawingAreaOffsetXPx) / mChartDrawingAreaWidth);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        calculatePercentages();
        mapXPointsForChartView();
        mapYPointsForChartView(getMaxPosYCoefficient());
        mapYPointsForScrollView(getMaxPosYCoefficient());

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
                mapYPointsForChartView(getMaxPosYCoefficient());
                mapYPointsForScrollView(getMaxPosYCoefficient());
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
        mPlateXValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateYValuePaint = new TextPaint();
        mPlateYValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateNamePaint = new TextPaint();
        mPlateNamePaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));

        mBackgroundPaint = new Paint();
        TypedValue backgroundColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.chartScrollViewBackgroundColor, backgroundColor, true)) {
            mBackgroundPaint.setColor(backgroundColor.data);
        }
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mSliderPaint = new Paint();
        TypedValue sliderColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.sliderColor, sliderColor, true)) {
            mSliderPaint.setColor(sliderColor.data);
        }
        mSliderPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mChosenAreaPaint = new Paint();
        mChosenAreaPaint.setColor(sliderColor.data);
        mChosenAreaPaint.setStyle(Paint.Style.STROKE);
        mChosenAreaPaint.setStrokeWidth(10);
    }

    private void calculateRects() {
        float top = mScrollDrawingAreaStartY;
        float bottom = mScrollDrawingAreaEndY;
        float left;
        float right;

        left = mScrollDrawingAreaStartX;
        right = mSliderPositionLeft;
        mBackGroundLeft.set(left, top, right, bottom);

        left = mSliderPositionRight;
        right = mScrollDrawingAreaEndX;
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

    protected void calculatePercentages() {
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

    public void setSliderPositions (float normPos1, float normPos2) {
        mSliderPositionLeft = normPos1 * mScrollDrawingAreaWidth + mScrollDrawingAreaStartX;
        mSliderPositionRight = normPos2 * mScrollDrawingAreaWidth + mScrollDrawingAreaStartX;
        mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;

        calculateRects();

        mNormSliderPosLeft = normPos1;
        mNormSliderPosRight = normPos2;
        setBorders(mNormSliderPosLeft, mNormSliderPosRight);

        mSliderPositionsSet = true;
    }

    public float[] getSliderPositions() {
        float[] positions = new float[2];
        positions[0] = mNormSliderPosLeft;
        positions[1] = mNormSliderPosRight;
        return positions;
    }

    protected float normalizeSliderPosition(float position) {
        return (position - mScrollDrawingAreaStartX) / mScrollDrawingAreaWidth;
    }

    protected void mapXPointsForChartView()
    {
        if (!mBordersSet)
            return;

        mChartMappedPointsX = mapXPointsForChartView(mPosX, mPos1, mPos2);
    }

    protected void mapXPointsForScrollView()
    {
        mScrollMappedPointsX = mapXPointsForScrollView(mPosX);
    }

    protected float[] mapXPointsForChartView(long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1];
        for (int i = 0, j = mPointsMinIndex; i < mapped.length; i++, j++) {
            float percentage = (float) (points[j] - xMin) / (float) calculatedArea;
            mapped[i] = mChartDrawingAreaStartX + mChartDrawingAreaWidth * percentage;
        }

        return mapped;
    }

    protected float[] mapXPointsForScrollView(long[] points) {
        long min = MathUtils.getMin(points);
        long calculatedArea = MathUtils.getMax(points) - min;
        float[] mapped = new float[points.length];
        for (int i = 0; i < mapped.length; i++) {
            float percentage = (float) (points[i] - min) / (float) calculatedArea;
            mapped[i] = mScrollDrawingAreaStartX + mScrollDrawingAreaWidth * percentage;
        }

        return mapped;
    }

    protected void mapYPointsForChartView(float coefficient) {
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

    protected void mapYPointsForScrollView(float coefficient) {
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

    protected void drawAreas(Canvas canvas, boolean drawInChartView) {
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
    }

    protected void drawScaleX (float[] mappedX, Canvas canvas) {

        float chartWidthPx = mChartDrawingAreaWidth / mNormWidth;
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

    protected void drawTopDatesText (Canvas canvas) {
        String dateText = DateTimeUtils.formatDatedMMMMMyyyy(mPos1) + " - " + DateTimeUtils.formatDatedMMMMMyyyy(mPos2);
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateXValuePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateText, mChartDrawingAreaEndX, mTopDatesOffsetYPx, mPlateXValuePaint);
    }

    protected void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], mChartDrawingAreaEndY, mDividerPaint);
    }

    protected void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
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

    protected void drawRects(Canvas canvas) {
        canvas.drawRect(mBackGroundLeft, mBackgroundPaint);
        canvas.drawRect(mBackgroundRight, mBackgroundPaint);
        canvas.drawRect(mSliderLeft, mSliderPaint);
        canvas.drawRect(mSliderRight, mSliderPaint);
        canvas.drawRect(mChosenArea, mChosenAreaPaint);
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

