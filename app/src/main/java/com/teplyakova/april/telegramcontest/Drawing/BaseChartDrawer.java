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
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.UI.ThemedDrawer;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.HashMap;

public abstract class BaseChartDrawer implements ChartDrawer, ThemedDrawer {
    private final float                  MINIMAL_SLIDER_WIDTH_NORMALIZED = 0.2f;
    private final float                  DATE_WIDTH_PX;
    private final float                  DATE_DISTANCE_PX;
    private final float                  TEXT_SIZE_SMALL_PX;
            final float                  TEXT_SIZE_MEDIUM_PX;
            final float                  TEXT_SIZE_LARGE_PX;
            final float                  VERTICAL_TEXT_OFFSET_PX;
    final float                          HORIZONTAL_TEXT_OFFSET_PX;
    private final float                  SLIDER_WIDTH_PX;
    private final float                  TOP_DATES_Y_OFFSET_PX;

    Resources.Theme                      theme;
    Context                              context;

    float                                viewWidthPx;
    float                                viewHeightPx;
    float                                chartAreaStartX;
    float                                chartAreaEndX;
    float                                chartAreaStartY;
    float                                chartAreaEndY;
    float                                chartAreaWidthPx;
    float                                chartAreaHeightPx;
    float                                scrollAreaStartX;
    float                                scrollAreaEndX;
    float                                scrollAreaStartY;
    float                                scrollAreaEndY;
    float                                scrollAreaWidthPx;
    float                                scrollAreaHeightPx;
    float                                drawingAreaOffsetXPx;
    float                                drawingAreaOffsetYPx;

    Paint                                background;
    Paint                                slider;
    Paint                                divider;
    TextPaint                            label;
    Paint                                plateBorder;
    Paint                                plateFill;
    TextPaint                            plateXValue;
    TextPaint                            plateYValue;
    TextPaint                            plateName;

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

    private Path                         mScrollBackground;
    private Path                         mSlider;

    ValueAnimator.AnimatorUpdateListener mViewAnimatorListener;

    boolean                              mPointIsChosen                  = false;
    int                                  mPositionOfChosenPoint;

    public BaseChartDrawer(Context context, ChartData chartData) {
        this.context = context;
        theme = context.getTheme();

        mPosX = chartData.posX;
        mMinX = MathUtils.getMin(mPosX);
        mMaxX = MathUtils.getMax(mPosX);

        DATE_WIDTH_PX = MathUtils.dpToPixels(36,    context);
        DATE_DISTANCE_PX = MathUtils.dpToPixels(22, context);
        TEXT_SIZE_SMALL_PX = MathUtils.dpToPixels(8, context);
        TEXT_SIZE_MEDIUM_PX = MathUtils.dpToPixels(12, context);
        TEXT_SIZE_LARGE_PX = MathUtils.dpToPixels(14, context);
        VERTICAL_TEXT_OFFSET_PX = MathUtils.dpToPixels(4, context);
        HORIZONTAL_TEXT_OFFSET_PX = MathUtils.dpToPixels(7, context);
        SLIDER_WIDTH_PX = MathUtils.dpToPixels(10, context);
        TOP_DATES_Y_OFFSET_PX = MathUtils.dpToPixels(14, context);

        setUpPaints();

        mScrollBackground = new Path();
        mSlider = new Path();
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        viewWidthPx = width;
        viewHeightPx = height;

        chartAreaStartX = drawingAreaOffsetXPx;
        chartAreaEndX = width - drawingAreaOffsetXPx;
        chartAreaStartY = drawingAreaOffsetYPx;
        chartAreaEndY = height - scrollDrawingAreaHeightPx - 2 * drawingAreaOffsetYPx;
        chartAreaWidthPx = chartAreaEndX - chartAreaStartX;
        chartAreaHeightPx = chartAreaEndY - chartAreaStartY;

        scrollAreaStartX = drawingAreaOffsetXPx;
        scrollAreaEndX = width - drawingAreaOffsetXPx;
        scrollAreaStartY = chartAreaEndY + 2 * drawingAreaOffsetYPx;
        scrollAreaEndY = scrollAreaStartY + scrollDrawingAreaHeightPx;
        scrollAreaWidthPx = scrollAreaEndX - scrollAreaStartX;
        scrollAreaHeightPx = scrollAreaEndY - scrollAreaStartY;

        this.drawingAreaOffsetXPx = drawingAreaOffsetXPx;
        this.drawingAreaOffsetYPx = drawingAreaOffsetYPx;

        mChosenAreaMinimalWidth = scrollAreaWidthPx * MINIMAL_SLIDER_WIDTH_NORMALIZED;

        mXLabelsYCoordinate = chartAreaEndY + MathUtils.dpToPixels(13, context);

        float minChartWidth = chartAreaWidthPx;
        float maxChartWidth = minChartWidth / MINIMAL_SLIDER_WIDTH_NORMALIZED;
        int sizeOfArray = mPosX.length;
        for (int i = 1; true; i = i * 2) {
            int textElemsCount = sizeOfArray / i;
            float chartWidth = DATE_WIDTH_PX * textElemsCount + DATE_DISTANCE_PX * (textElemsCount - 1);
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
                if (y >= chartAreaEndY || x <= chartAreaStartX || x >= chartAreaEndX) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(mapCoordinateToPoint(mChartMappedPointsX, x));
                }
                if (x >= scrollAreaStartX && x <= scrollAreaEndX && y >= scrollAreaStartY && y <= scrollAreaEndY) {
                    if (mLeftSliderIsCaught){
                        setSliderPositions(normalizeSliderPosition(MathUtils.clamp(x, mSliderPositionRight - mChosenAreaMinimalWidth, scrollAreaStartX)), normalizeSliderPosition(mSliderPositionRight));
                    }
                    else if (mRightSliderIsCaught) {
                        setSliderPositions(normalizeSliderPosition(mSliderPositionLeft), normalizeSliderPosition(MathUtils.clamp(x, scrollAreaEndX, mSliderPositionLeft + mChosenAreaMinimalWidth)));
                    }
                    else if (mChosenAreaIsCaught) {
                        float deltaX = x - mCurrChosenAreaPosition;
                        mCurrChosenAreaPosition = x;
                        setSliderPositions(normalizeSliderPosition(MathUtils.clamp(mSliderPositionLeft + deltaX, scrollAreaEndX - mCurrChosenAreaWidth, scrollAreaStartX)), normalizeSliderPosition(MathUtils.clamp(mSliderPositionRight + deltaX, scrollAreaEndX, scrollAreaStartX + mCurrChosenAreaWidth)));
                    }
                    return true;
                }
            case MotionEvent.ACTION_DOWN:
                if (y >= chartAreaEndY || x <= chartAreaStartX || x >= chartAreaEndX) {
                    hidePointDetails();
                }
                else {
                    showPointDetails(mapCoordinateToPoint(mChartMappedPointsX, x));
                }
                if (x >= scrollAreaStartX && x <= scrollAreaEndX && y >= scrollAreaStartY && y <= scrollAreaEndY) {
                    if ((x >= mSliderPositionLeft - 3f * SLIDER_WIDTH_PX) && (x <= mSliderPositionLeft + SLIDER_WIDTH_PX)) {
                        mLeftSliderIsCaught = true;
                    }
                    else if ((x >= mSliderPositionRight - SLIDER_WIDTH_PX) &&(x <= mSliderPositionRight + 3f * SLIDER_WIDTH_PX)) {
                        mRightSliderIsCaught = true;
                    }
                    else if ((x >= mSliderPositionLeft + SLIDER_WIDTH_PX) && (x <= mSliderPositionRight - SLIDER_WIDTH_PX)) {
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

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * drawingAreaOffsetXPx) / chartAreaWidthPx);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        hidePointDetails();

        return result;
    }

    public void setAnimatorUpdateListener (ValueAnimator.AnimatorUpdateListener listener) {
        mViewAnimatorListener = listener;
    }

    public void setSliderPositions (float normPos1, float normPos2) {
        mSliderPositionLeft = normPos1 * scrollAreaWidthPx + scrollAreaStartX;
        mSliderPositionRight = normPos2 * scrollAreaWidthPx + scrollAreaStartX;
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

    public int getChosenPointPosition() {
        if (mPointIsChosen)
            return mPositionOfChosenPoint;
        else
            return -1;
    }

    public void setChosenPointPosition(int pointPosition) {
        if (pointPosition != -1) {
            showPointDetails(pointPosition);
        }
    }

    private void calculateRects() {
        float top = scrollAreaStartY;
        float bottom = scrollAreaEndY;
        float left;
        float right;

        mScrollBackground.reset();
        mSlider.reset();

        left = scrollAreaStartX;
        right = mSliderPositionLeft + SLIDER_WIDTH_PX;
        RectF rect = new RectF(left, top, right, bottom);
        mScrollBackground.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set((right + left) / 2, top, right, bottom);
        mScrollBackground.addRect(rect, Path.Direction.CW);
        left = mSliderPositionRight - SLIDER_WIDTH_PX;
        right = scrollAreaEndX;
        rect.set(left, top, right, bottom);
        mScrollBackground.addRoundRect(rect, 20 ,20, Path.Direction.CW);
        rect.set(left, top, (right + left) / 2, bottom);
        mScrollBackground.addRect(rect, Path.Direction.CW);

        left = mSliderPositionLeft;
        right = left + SLIDER_WIDTH_PX;
        rect.set(left, top, right, bottom);
        mSlider.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set((right + left) / 2, top, right, bottom);
        mSlider.addRect(rect, Path.Direction.CW);
        left = mSliderPositionRight - SLIDER_WIDTH_PX;
        right = mSliderPositionRight;
        rect.set(left, top, right, bottom);
        mSlider.addRoundRect(rect, 20, 20, Path.Direction.CW);
        rect.set(left, top, (right + left) / 2, bottom);
        mSlider.addRect(rect, Path.Direction.CW);
        mSlider.moveTo(mSliderPositionLeft + SLIDER_WIDTH_PX, scrollAreaStartY);
        mSlider.lineTo(mSliderPositionRight - SLIDER_WIDTH_PX, scrollAreaStartY);
        mSlider.moveTo(mSliderPositionLeft + SLIDER_WIDTH_PX, scrollAreaEndY);
        mSlider.lineTo(mSliderPositionRight - SLIDER_WIDTH_PX, scrollAreaEndY);
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
            mapped[i] = chartAreaStartX + chartAreaWidthPx * percentage;
        }

        return mapped;
    }

    protected float[] mapXPointsForScrollView(long[] points) {
        long min = MathUtils.getMin(points);
        long calculatedArea = MathUtils.getMax(points) - min;
        float[] mapped = new float[points.length];
        for (int i = 0; i < mapped.length; i++) {
            float percentage = (float) (points[i] - min) / (float) calculatedArea;
            mapped[i] = scrollAreaStartX + scrollAreaWidthPx * percentage;
        }

        return mapped;
    }

    protected float normalizeSliderPosition(float position) {
        return (position - scrollAreaStartX) / scrollAreaWidthPx;
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
        divider = new Paint();
        divider.setStrokeWidth(2);

        label = new TextPaint();
        label.setTextSize(TEXT_SIZE_MEDIUM_PX);
        label.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        label.setAntiAlias(true);

        plateBorder = new Paint();
        plateBorder.setStyle(Paint.Style.STROKE);
        plateBorder.setStrokeWidth(2);

        plateFill = new Paint();
        plateFill.setStyle(Paint.Style.FILL);

        plateXValue = new TextPaint();
        plateXValue.setTextSize(TEXT_SIZE_MEDIUM_PX);
        plateXValue.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
        plateXValue.setAntiAlias(true);

        plateYValue = new TextPaint();
        plateYValue.setTextSize(TEXT_SIZE_MEDIUM_PX);
        plateYValue.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
        plateYValue.setTextAlign(Paint.Align.RIGHT);
        plateYValue.setAntiAlias(true);

        plateName = new TextPaint();
        plateName.setTextSize(TEXT_SIZE_MEDIUM_PX);
        plateName.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        plateName.setTextAlign(Paint.Align.LEFT);
        plateName.setAntiAlias(true);

        background = new Paint();
        TypedValue backgroundColor = new TypedValue();
        if (theme.resolveAttribute(R.attr.chartScrollViewBackgroundColor, backgroundColor, true)) {
            background.setColor(backgroundColor.data);
        }
        background.setStyle(Paint.Style.FILL);

        slider = new Paint();
        TypedValue sliderColor = new TypedValue();
        if (theme.resolveAttribute(R.attr.sliderColor, sliderColor, true)) {
            slider.setColor(sliderColor.data);
        }
        slider.setStyle(Paint.Style.FILL_AND_STROKE);
        slider.setStrokeWidth(2);
    }

    protected void drawRects(Canvas canvas) {
        canvas.drawPath(mScrollBackground, background);
        canvas.drawPath(mSlider, slider);
    }

    protected void drawTopDatesText (Canvas canvas) {
        int firstVisiblePosition = MathUtils.getIndexOfNearestRightElement(mPosX, mPos1);
        String dateText = DateTimeUtils.formatDatedMMMMMyyyy(mPosX[firstVisiblePosition]) + " - " + DateTimeUtils.formatDatedMMMMMyyyy(mPos2);
        plateXValue.setTextSize(TEXT_SIZE_MEDIUM_PX);
        plateXValue.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateText, chartAreaEndX, TOP_DATES_Y_OFFSET_PX, plateXValue);
    }

    protected void drawScaleX (float[] mappedX, Canvas canvas) {

        float chartWidthPx = chartAreaWidthPx / mNormWidth;
        int xLabelPeriodicity;

        for (xLabelPeriodicity = 1; true; xLabelPeriodicity = xLabelPeriodicity * 2) {
            if (mXLabelsPeriodToMinChartWidthPx.containsKey(xLabelPeriodicity)) {
                if (mXLabelsPeriodToMinChartWidthPx.get(xLabelPeriodicity) <= chartWidthPx)
                    break;
            }
        }
        int labelsPeriodCurrent = xLabelPeriodicity;

        label.setAlpha(255);
        label.setTextAlign(Paint.Align.CENTER);

        for (int i = mPointsMinIndex, j = 0; i <= mPointsMaxIndex; i++, j++) {
            if ((mPosX.length - 1 - i) % labelsPeriodCurrent == 0) {
                if (i == mPosX.length - 1) {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j]- MathUtils.dpToPixels(9, context), mXLabelsYCoordinate, label);
                }
                else {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j], mXLabelsYCoordinate, label);
                }
            }
        }

        //if there are no inbetween labels
        if (mXLabelsPeriodToMinChartWidthPx.get(labelsPeriodCurrent) == chartWidthPx)
            return;

        if (mXLabelsPeriodToMinChartWidthPx.containsKey(labelsPeriodCurrent / 2)) {
            float alphaMultiplier = MathUtils.inverseLerp(mXLabelsPeriodToMinChartWidthPx.get(labelsPeriodCurrent),
                    mXLabelsPeriodToMinChartWidthPx.get(labelsPeriodCurrent / 2),
                    chartWidthPx);
            alphaMultiplier = (alphaMultiplier - 0.334f) * 3;
            alphaMultiplier = MathUtils.clamp(alphaMultiplier, 1, 0);
            label.setAlpha((int) Math.floor(255 * alphaMultiplier));

            for (int i = mPointsMinIndex, j = 0; i <= mPointsMaxIndex; i++, j++) {
                if ((mPosX.length - 1 - i) % (labelsPeriodCurrent / 2) == 0 && (mPosX.length - 1 - i) % labelsPeriodCurrent != 0) {
                    canvas.drawText(DateTimeUtils.formatDateMMMd(mPosX[i]), mappedX[j], mXLabelsYCoordinate, label);
                }
            }
        }
    }

    protected void showPointDetails(int positionOfChosenPoint) {
        mPositionOfChosenPoint = positionOfChosenPoint;
        mPointIsChosen = true;
    }

    protected void hidePointDetails() {
        mPointIsChosen = false;
    }

    @Override
    public void setPlateFillColor(int color) {
        plateFill.setColor(color);
    }

    @Override
    public void setSliderBgColor(int color) {
        background.setColor(color);
    }

    @Override
    public void setSliderHandlerColor(int color) {
        slider.setColor(color);
    }

    @Override
    public void setPrimaryBgColor(int color) {

    }

    @Override
    public void setDividerColor(int color) {
        divider.setColor(color);
        plateBorder.setColor(color);
    }

    @Override
    public void setMainTextColor(int color) {
        plateName.setColor(color);
        plateXValue.setColor(color);
    }

    @Override
    public void setLabelColor(int color) {
        label.setColor(color);
    }

    @Override
    public void setOpaquePlateColor(int color) {

    }
}
