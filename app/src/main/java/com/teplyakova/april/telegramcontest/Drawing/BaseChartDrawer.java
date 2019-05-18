package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.HashMap;

public abstract class BaseChartDrawer implements ChartDrawer {
    private final float                  MINIMAL_NORM_SLIDER_WIDTH      = 0.2f;
    private final int                    TEXT_LABEL_WIDTH_DP            = 36;
    private final int                    TEXT_LABEL_DISTANCE_DP         = 22;
    private final int                    TEXT_SIZE_SMALL_DP             = 8;
    private final int                    TEXT_SIZE_MEDIUM_DP            = 12;
    private final int                    TEXT_SIZE_LARGE_DP             = 14;
    private final int                    SLIDER_WIDTH_DP                = 10;
    private final int                    TOP_DATES_OFFSET_Y_DP          = 14;

    private final float                  mDateWidthPx;
    private final float                  mDateDistancePx;
    private final float                  mTextSizeSmallPx;
            final float                  mTextSizeMediumPx;
            final float                  mTextSizeLargePx;
    private final float                  mSliderWidthPx;
    private final float                  mTopDatesOffsetYPx;

    Resources.Theme                      mTheme;
    Context                              mContext;

    float                                mViewWidth;
    float                                mViewHeight;
    float                                mChartDrawingAreaStartX;
    float                                mChartDrawingAreaEndX;
    float                                mChartDrawingAreaStartY;
    float                                mChartDrawingAreaEndY;
    float                                mChartDrawingAreaWidth;
    float                                mChartDrawingAreaHeight;
    float                                mScrollDrawingAreaStartX;
    float                                mScrollDrawingAreaEndX;
    float                                mScrollDrawingAreaStartY;
    float                                mScrollDrawingAreaEndY;
    float                                mScrollDrawingAreaWidth;
    float                                mScrollDrawingAreaHeight;
    float                                mDrawingAreaOffsetXPx;
    float                                mDrawingAreaOffsetYPx;

    Paint                                mBackgroundPaint;
    Paint                                mSliderPaint;
    Paint                                mDividerPaint;
    TextPaint                            mBaseLabelPaint;
    Paint                                mPlateStrokePaint;
    Paint                                mPlateFillPaint;
    TextPaint                            mPlateXValuePaint;
    TextPaint                            mPlateYValuePaint;
    TextPaint                            mPlateNamePaint;

    private boolean                      mLeftSliderIsCaught             = false;
    private boolean                      mRightSliderIsCaught            = false;
    private boolean                      mChosenAreaIsCaught             = false;

    private float                        mCurrChosenAreaPosition;
    private float                        mCurrChosenAreaWidth;
    private float                        mChosenAreaMinimalWidth;

    private float                        mNormSliderPosLeft              = 0.8f;
    private float                        mNormSliderPosRight             = 1;
    private float                        mSliderPositionLeft;
    private float                        mSliderPositionRight;

    private long                         mPos1                           = -1;
    private long                         mPos2                           = -1;
    private float                        mNormWidth;
    long[]                               mPosX;
    long                                 mMinX;
    long                                 mMaxX;
    boolean                              mBordersSet;
    int                                  mPointsMinIndex;
    int                                  mPointsMaxIndex;

    float[]                              mChartMappedPointsX;
    float[]                              mScrollMappedPointsX;

    private float                        mXLabelsYCoordinate;
    private HashMap<Integer, Float>      mXLabelsPeriodToMinChartWidthPx = new HashMap<>();
    private int                          mXLabelsPeriodCurrent;

    private Path                         mScrollBackground;
    private Path                         mSlider;

    ValueAnimator.AnimatorUpdateListener mViewAnimatorListener;

    boolean                              mPointIsChosen                  = false;
    int                                  mPositionOfChosenPoint;

    public BaseChartDrawer(Context context, ChartData chartData) {
        mContext = context;
        mTheme = context.getTheme();

        mPosX = chartData.posX;
        mMinX = MathUtils.getMin(mPosX);
        mMaxX = MathUtils.getMax(mPosX);

        mDateWidthPx    = MathUtils.dpToPixels(TEXT_LABEL_WIDTH_DP,    context);
        mDateDistancePx = MathUtils.dpToPixels(TEXT_LABEL_DISTANCE_DP, context);
        mTextSizeSmallPx = MathUtils.dpToPixels(TEXT_SIZE_SMALL_DP, context);
        mTextSizeMediumPx = MathUtils.dpToPixels(TEXT_SIZE_MEDIUM_DP, context);
        mTextSizeLargePx = MathUtils.dpToPixels(TEXT_SIZE_LARGE_DP, context);
        mSliderWidthPx = MathUtils.dpToPixels(SLIDER_WIDTH_DP, context);
        mTopDatesOffsetYPx = MathUtils.dpToPixels(TOP_DATES_OFFSET_Y_DP, context);

        setUpPaints();

        mScrollBackground = new Path();
        mSlider = new Path();
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        mViewWidth = width;
        mViewHeight = height;

        mChartDrawingAreaStartX = drawingAreaOffsetXPx;
        mChartDrawingAreaEndX = width - drawingAreaOffsetXPx;
        mChartDrawingAreaStartY = drawingAreaOffsetYPx;
        mChartDrawingAreaEndY = height - scrollDrawingAreaHeightPx - 2 * drawingAreaOffsetYPx;
        mChartDrawingAreaWidth = mChartDrawingAreaEndX - mChartDrawingAreaStartX;
        mChartDrawingAreaHeight = mChartDrawingAreaEndY - mChartDrawingAreaStartY;

        mScrollDrawingAreaStartX = drawingAreaOffsetXPx;
        mScrollDrawingAreaEndX = width - drawingAreaOffsetXPx;
        mScrollDrawingAreaStartY = mChartDrawingAreaEndY + 2 * drawingAreaOffsetYPx;
        mScrollDrawingAreaEndY = mScrollDrawingAreaStartY + scrollDrawingAreaHeightPx;
        mScrollDrawingAreaWidth = mScrollDrawingAreaEndX - mScrollDrawingAreaStartX;
        mScrollDrawingAreaHeight = mScrollDrawingAreaEndY - mScrollDrawingAreaStartY;

        mDrawingAreaOffsetXPx = drawingAreaOffsetXPx;
        mDrawingAreaOffsetYPx = drawingAreaOffsetYPx;

        mChosenAreaMinimalWidth = mScrollDrawingAreaWidth * MINIMAL_NORM_SLIDER_WIDTH;

        mXLabelsYCoordinate = mChartDrawingAreaEndY + MathUtils.dpToPixels(13, mContext);

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
                        mCurrChosenAreaPosition = x;
                        setSliderPositions(normalizeSliderPosition(MathUtils.clamp(mSliderPositionLeft + deltaX, mScrollDrawingAreaEndX - mCurrChosenAreaWidth, mScrollDrawingAreaStartX)), normalizeSliderPosition(MathUtils.clamp(mSliderPositionRight + deltaX, mScrollDrawingAreaEndX, mScrollDrawingAreaStartX + mCurrChosenAreaWidth)));
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
                    if ((x >= mSliderPositionLeft - 3f * mSliderWidthPx) && (x <= mSliderPositionLeft + mSliderWidthPx)) {
                        mLeftSliderIsCaught = true;
                    }
                    else if ((x >= mSliderPositionRight - mSliderWidthPx) &&(x <= mSliderPositionRight + 3f * mSliderWidthPx)) {
                        mRightSliderIsCaught = true;
                    }
                    else if ((x >= mSliderPositionLeft + mSliderWidthPx) && (x <= mSliderPositionRight - mSliderWidthPx)) {
                        mChosenAreaIsCaught = true;
                        mCurrChosenAreaPosition = x;
                        mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                    mRightSliderIsCaught = false;
                    mLeftSliderIsCaught = false;
                    mChosenAreaIsCaught = false;
                    return true;
        }
        return false;
    }

    public boolean setBorders (float normPosX1, float normPosX2) {
        mBordersSet = true;

        mNormWidth = normPosX2 - normPosX1;
        long pos1;
        long pos2;
        long width = mMaxX - mMinX;
        pos1 = (long) Math.floor(normPosX1 * width) + mMinX;
        pos2 = (long) Math.ceil(normPosX2 * width) + mMinX;

        boolean result = false;
        if (mPos1 != pos1 || mPos2 != pos2)
            result = true;

        mPos1 = pos1;
        mPos2 = pos2;

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * mDrawingAreaOffsetXPx) / mChartDrawingAreaWidth);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        hidePointDetails();

        return result;
    }

    public void setAnimatorUpdateListener (ValueAnimator.AnimatorUpdateListener listener) {
        mViewAnimatorListener = listener;
    }

    public void setSliderPositions (float normPos1, float normPos2) {
        mSliderPositionLeft = normPos1 * mScrollDrawingAreaWidth + mScrollDrawingAreaStartX;
        mSliderPositionRight = normPos2 * mScrollDrawingAreaWidth + mScrollDrawingAreaStartX;
        mCurrChosenAreaWidth = mSliderPositionRight - mSliderPositionLeft;

        calculateRects();
        mNormSliderPosLeft = normPos1;
        mNormSliderPosRight = normPos2;
        setBorders(mNormSliderPosLeft, mNormSliderPosRight);
    }

    public float[] getSliderPositions() {
        float[] positions = new float[2];
        positions[0] = mNormSliderPosLeft;
        positions[1] = mNormSliderPosRight;
        return positions;
    }

    private void calculateRects() {
        float top = mScrollDrawingAreaStartY;
        float bottom = mScrollDrawingAreaEndY;
        float left;
        float right;

        mScrollBackground.reset();
        mSlider.reset();

        left = mScrollDrawingAreaStartX;
        right = mSliderPositionLeft + mSliderWidthPx;
        RectF rect = new RectF(left, top, right, bottom);
        mScrollBackground.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set((right + left) / 2, top, right, bottom);
        mScrollBackground.addRect(rect, Path.Direction.CW);
        left = mSliderPositionRight - mSliderWidthPx;
        right = mScrollDrawingAreaEndX;
        rect.set(left, top, right, bottom);
        mScrollBackground.addRoundRect(rect, 20 ,20, Path.Direction.CW);
        rect.set(left, top, (right + left) / 2, bottom);
        mScrollBackground.addRect(rect, Path.Direction.CW);

        left = mSliderPositionLeft;
        right = left + mSliderWidthPx;
        rect.set(left, top, right, bottom);
        mSlider.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set((right + left) / 2, top, right, bottom);
        mSlider.addRect(rect, Path.Direction.CW);
        left = mSliderPositionRight - mSliderWidthPx;
        right = mSliderPositionRight;
        rect.set(left, top, right, bottom);
        mSlider.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set(left, top, (right + left) / 2, bottom);
        mSlider.addRect(rect, Path.Direction.CW);
        mSlider.moveTo(mSliderPositionLeft + mSliderWidthPx, mScrollDrawingAreaStartY);
        mSlider.lineTo(mSliderPositionRight - mSliderWidthPx, mScrollDrawingAreaStartY);
        mSlider.moveTo(mSliderPositionLeft + mSliderWidthPx, mScrollDrawingAreaEndY);
        mSlider.lineTo(mSliderPositionRight - mSliderWidthPx, mScrollDrawingAreaEndY);
    }

    protected void mapXPointsForChartView() {
        if (!mBordersSet)
            return;

        mChartMappedPointsX = mapXPointsForChartView(mPosX, mPos1, mPos2);
    }

    protected void mapXPointsForScrollView() {
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

    protected float normalizeSliderPosition(float position) {
        return (position - mScrollDrawingAreaStartX) / mScrollDrawingAreaWidth;
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

    protected void setUpPaints() {
        mDividerPaint = new Paint();
        TypedValue dividerColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.dividerColor, dividerColor, true)) {
            mDividerPaint.setColor(dividerColor.data);
        }
        mDividerPaint.setStrokeWidth(2);

        mBaseLabelPaint = new TextPaint();
        mBaseLabelPaint.setTextSize(mTextSizeMediumPx);
        TypedValue baseLabelColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.baseLabelColor, baseLabelColor, true)) {
            mBaseLabelPaint.setColor(baseLabelColor.data);
        }
        mBaseLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        mBaseLabelPaint.setAntiAlias(true);

        mPlateStrokePaint = new Paint();
        mPlateStrokePaint.setColor(dividerColor.data);
        mPlateStrokePaint.setStyle(Paint.Style.STROKE);
        mPlateStrokePaint.setStrokeWidth(2);

        mPlateFillPaint = new Paint();
        mPlateFillPaint.setStyle(Paint.Style.FILL);
        TypedValue plateColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.plateBackgroundColor, plateColor, true)) {
            mPlateFillPaint.setColor(plateColor.data);
        }


        mPlateXValuePaint = new TextPaint();
        TypedValue textColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.labelTextColor, textColor, true)) {
            mPlateXValuePaint.setColor(textColor.data);
        }
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateXValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
        mPlateXValuePaint.setAntiAlias(true);

        mPlateYValuePaint = new TextPaint();
        mPlateYValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
        mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
        mPlateYValuePaint.setAntiAlias(true);

        mPlateNamePaint = new TextPaint();
        mPlateNamePaint.setColor(textColor.data);
        mPlateNamePaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
        mPlateNamePaint.setAntiAlias(true);

        mBackgroundPaint = new Paint();
        TypedValue backgroundColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.chartScrollViewBackgroundColor, backgroundColor, true)) {
            mBackgroundPaint.setColor(backgroundColor.data);
        }
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mSliderPaint = new Paint();
        TypedValue sliderColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.sliderColor, sliderColor, true)) {
            mSliderPaint.setColor(sliderColor.data);
        }
        mSliderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSliderPaint.setStrokeWidth(2);
    }

    protected void drawRects(Canvas canvas) {
        canvas.drawPath(mScrollBackground, mBackgroundPaint);
        canvas.drawPath(mSlider, mSliderPaint);
    }

    protected void drawTopDatesText (Canvas canvas) {
        int firstVisiblePosition = MathUtils.getIndexOfNearestRightElement(mPosX, mPos1);
        String dateText = DateTimeUtils.formatDatedMMMMMyyyy(mPosX[firstVisiblePosition]) + " - " + DateTimeUtils.formatDatedMMMMMyyyy(mPos2);
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateXValuePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateText, mChartDrawingAreaEndX, mTopDatesOffsetYPx, mPlateXValuePaint);
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

    protected void showPointDetails(float xCoord) {
        mPositionOfChosenPoint = mapCoordinateToPoint(mChartMappedPointsX, xCoord);
        mPointIsChosen = true;
    }

    protected void hidePointDetails() {
        mPointIsChosen = false;
    }
}
