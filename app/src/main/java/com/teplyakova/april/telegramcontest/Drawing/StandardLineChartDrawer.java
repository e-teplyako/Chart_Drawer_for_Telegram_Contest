package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.Interfaces.SliderObservable;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.ScrollChartView;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StandardLineChartDrawer implements ChartDrawer {

    class YScale
    {
        long Height;
        long MaxY;
        int  Alpha;

        long HeightStart;
        long HeightEnd;

        long MaxYStart;
        long MaxYEnd;

        int  AlphaStart;
        int  AlphaEnd;
    }

    class ChartLine
    {
        LineData Data;

        int      Alpha;
        int      AlphaStart;
        int      AlphaEnd;

        float[] mMappedPointsY;

        //ArrayList<Float> optimizedPointsX = new ArrayList<Float>();
        //ArrayList<Float> optimizedPointsY = new ArrayList<Float>();

        private boolean IsVisible()
        {
            return (Alpha > 0 || AlphaEnd > 0);
        }
    }

    final int   DRAWING_AREA_OFFSET_X_DP = 8;
    final int   DRAWING_AREA_OFFSET_Y_DP = 16;
    final int   Y_DIVIDERS_COUNT         = 6;
    final int   TEXT_SIZE_DP             = 12;
    final int   TEXT_LABEL_WIDTH_DP      = 36;
    final int   TEXT_LABEL_DISTANCE_DP   = 22;
    final int   PLATE_WIDTH_DP           = 110;
    final int   PLATE_HEIGHT_DP          = 56;
    final int   TEXT_SIZE_SMALL_DP       = 8;
    final int   TEXT_SIZE_MEDIUM_DP      = 12;
    final int   TEXT_SIZE_LARGE_DP       = 14;

    private Resources.Theme mTheme;
    private Context         mContext;

    private Paint     mChartPaint;
    private Paint     mDividerPaint;
    private TextPaint mBaseLabelPaint;
    private Paint     mCirclePaint;
    private Paint     mPlatePaint;
    private TextPaint mPlateXValuePaint;
    private TextPaint mPlateYValuePaint;
    private TextPaint mPlateNamePaint;

    private final float mTextSizePx;
    private final float mDateWidthPx;
    private final float mDateDistancePx;
    private final float mPlateWidthPx;
    private final float mPlateHeightPx;
    private final float mTextSizeSmallPx;
    private final float mTextSizeMediumPx;
    private final float mTextSizeLargePx;
    private final float mDrawingAreaOffsetXPx;
    private final float mDrawingAreaOffsetYPx;

    private long[] mPosX;
    private long  mPos1 = -1;
    private long  mPos2 = -1;
    private boolean mBordersSet;
    private float[] mMappedPointsX;
    private float mNormWidth;
    private int mPointsMinIndex;
    private int mPointsMaxIndex;

    private ArrayList<ChartLine> mLines = new ArrayList<>();
    private ValueAnimator        mSetLinesAnimator;
    private ValueAnimator.AnimatorUpdateListener mViewAnimatorListener;

    private long          mMaxY       = -1;
    private long          mTargetMaxY = -1;
    private ValueAnimator mMaxYAnimator;

    private ArrayList<YScale> mYScales = new ArrayList<YScale>();

    private float mViewWidth;
    private float mViewHeight;
    private float mDrawingAreaStartX;
    private float mDrawingAreaEndX;
    private float mDrawingAreaStartY;
    private float mDrawingAreaEndY;
    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;
    private float mXLabelsYCoordinate;

    private boolean mPointIsChosen = false;
    private float mXCoordinateOfTouch;
    private int mPositionOfChosenPoint;

    private boolean mSetLinesFirstTime = true;

    private HashMap<Integer, Float> mXLabelsPeriodToMinChartWidthPx = new HashMap<>();
    private int                     mXLabelsPeriodCurrent;

    private float mOptimTolerancePx;

    public StandardLineChartDrawer(Context context, ChartData chartData, SliderObservable observable) {
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

        setUpPaints();

        for (LineData lineData : chartData.lines)
        {
            ChartLine chartLine = new ChartLine();
            chartLine.Data      = lineData;
            chartLine.Alpha     = 255;
            chartLine.AlphaEnd  = 255;
            mLines.add(chartLine);
        }

        mPosX = chartData.posX;
        mOptimTolerancePx = mPosX.length >= 150 ? MathUtils.dpToPixels(4, mContext) : 0;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBordersSet)
            drawScaleX(mMappedPointsX, canvas);

        if (!showChartLines() || !mBordersSet)
        {
            drawScaleY(100, 100, 255, canvas);
            return;
        }

        for (YScale yScale : mYScales)
            drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mMappedPointsX, mXCoordinateOfTouch);
            drawVerticalDivider(mMappedPointsX, canvas);
        }

        for (ChartLine line : mLines) {
            if (line.IsVisible())
                drawChartLine(line, canvas);
        }

        for (YScale yScale : mYScales)
            drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);

        if (mPointIsChosen) {
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
        mapPoints();
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

        mapXPoints();

        updateMaxY();

        hidePointDetails();

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

        updateMaxY();

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

    private boolean showChartLines()
    {
        for (ChartLine line : mLines)
            if (line.IsVisible())
                return true;

        return false;
    }

    private LineData[] getActiveChartLines()
    {
        ArrayList<LineData> arrayList = new ArrayList<>();

        for (ChartLine line : mLines)
            if (line.AlphaEnd > 0)
                arrayList.add(line.Data);

        return arrayList.toArray(new LineData[arrayList.size()]);
    }

    private void setUpPaints() {
        mChartPaint = new Paint();
        mChartPaint.setStyle(Paint.Style.STROKE);
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
        mBaseLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));

        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(6);

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

    private void mapXPoints()
    {
        if (!mBordersSet)
            return;

        mMappedPointsX = mapXPoints(mPosX, mPos1, mPos2);
    }

    private void mapPoints()
    {
        if (!mBordersSet || !showChartLines())
            return;

        for (ChartLine line : mLines) {
            if (line.IsVisible()){
                line.mMappedPointsY = mapYPoints(line.Data.posY, 0, mMaxY);
            }
        }
    }

    private void updateMaxY() {
        LineData[] activeLines = getActiveChartLines();

        if (!mBordersSet || activeLines.length == 0)
            return;

        long newYMax = MathUtils.getMaxY(activeLines, mPointsMinIndex, mPointsMaxIndex);
        newYMax = newYMax / Y_DIVIDERS_COUNT * (Y_DIVIDERS_COUNT + 1);

        if (newYMax != mTargetMaxY)
        {
            boolean firstTime = mTargetMaxY < 0;
            mTargetMaxY = newYMax;

            if (firstTime)
            {
                mMaxY = mTargetMaxY;

                YScale yScale = new YScale();
                yScale.Height = mMaxY;
                yScale.MaxY   = mMaxY;
                yScale.Alpha  = 255;
                mYScales.add(yScale);
            }
            else {
                startAnimationYMax();
            }
        }

        mapPoints();
    }

    private void startAnimationYMax() {
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
        newScale.Alpha       = 0;
        newScale.AlphaStart  = 0;
        newScale.AlphaEnd    = 255;
        mYScales.add(newScale);

        final long startY = mMaxY;
        final long endY   = mTargetMaxY;

        final String KEY_PHASE = "phase";

        mMaxYAnimator = new ValueAnimator();
        mMaxYAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
        mMaxYAnimator.setDuration(400);

        mMaxYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float t = (float) animator.getAnimatedValue(KEY_PHASE);

                mMaxY = (long)MathUtils.lerp(startY, endY, t);
                mapPoints();

                for (YScale yScale : mYScales)
                {
                    yScale.MaxY   = (long)MathUtils.lerp(yScale.MaxYStart,   yScale.MaxYEnd,   t);

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

    private void drawChartLine (ChartLine line, Canvas canvas){
        mChartPaint.setColor(line.Data.color);
        mChartPaint.setAlpha(line.Alpha);

        /*int arraySize = line.optimizedPointsX.size();

        for (int i = 0; i < arraySize - 1; i++) {
            canvas.drawLine(
                line.optimizedPointsX.get(i),
                line.optimizedPointsY.get(i),
                line.optimizedPointsX.get(i + 1),
                line.optimizedPointsY.get(i + 1),
                mChartPaint
            );
        }*/

        float[] drawingPoints = MathUtils.concatArraysForDrawing(mMappedPointsX, line.mMappedPointsY);
        if (drawingPoints != null) {
            canvas.drawLines(drawingPoints, mChartPaint);
        }

        if (mPointIsChosen)
            drawChosenPointCircle(mMappedPointsX, line.mMappedPointsY, line.Data.color, canvas);
    }

    private float[] mapYPoints (long[] points, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1];

        for (int i = 0, j = mPointsMinIndex; i < mapped.length; i++, j++) {
            float percentage = (float) (points[j] - yMin) / (float) calculatedArea;
            mapped[i] = mDrawingAreaHeight * percentage + mDrawingAreaStartY;
            mapped[i] = mDrawingAreaEndY - mapped[i] + mDrawingAreaStartY;
        }

        return mapped;
    }

    private float[] mapXPoints (long[] points, long xMin, long xMax) {
        long calculatedArea = xMax - xMin;
        float[] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1];
        for (int i = 0, j = mPointsMinIndex; i < mapped.length; i++, j++) {
            float percentage = (float) (points[j] - xMin) / (float) calculatedArea;
            mapped[i] = mDrawingAreaStartX + mDrawingAreaWidth * percentage;
        }

        return mapped;
    }

    private int mapCoordinateToPoint(float[] mappedX, float xCoord) {
        int position = MathUtils.getIndexOfNearestElement(mappedX, xCoord);
        while (mPosX[position + mPointsMinIndex] < mPos1) {
            position++;
        }
        while (mPosX[position + mPointsMinIndex] > mPos2) {
            position--;
        }
        return position + mPointsMinIndex;
    }

    private void drawScaleX (float[] mappedX, Canvas canvas) {

        float chartWidthPx = mDrawingAreaWidth / mNormWidth;
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

    private void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
    {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mDrawingAreaStartX, mDrawingAreaEndY, mDrawingAreaEndX, mDrawingAreaEndY, mDividerPaint);

        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / Y_DIVIDERS_COUNT;

        float startY = mDrawingAreaEndY - spaceBetweenDividers;
        float stopY = startY;

        mDividerPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT - 1; i++) {
            canvas.drawLine(mDrawingAreaStartX, startY, mDrawingAreaEndX, stopY, mDividerPaint);
            startY -= spaceBetweenDividers;
            stopY = startY;
        }
    }

    private void drawYLabels (long height, long yMax, int alpha, Canvas canvas) {
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / Y_DIVIDERS_COUNT;

        long step = 0;
        float yLabelCoord = mDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setAlpha(alpha);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), mDrawingAreaStartX, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }

    }

    private void drawChosenPointCircle(float[] mappedX, float[] mappedY, int color, Canvas canvas) {
        mCirclePaint.setColor(color);
        canvas.drawCircle(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mappedY[mPositionOfChosenPoint - mPointsMinIndex], 16f, mCirclePaint);
        TypedValue background = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.primaryBackgroundColor, background, true)) {
            mCirclePaint.setColor(background.data);
        }
        canvas.drawCircle(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mappedY[mPositionOfChosenPoint - mPointsMinIndex], 8f, mCirclePaint);
    }

    private void drawVerticalDivider(float[] mappedX, Canvas canvas) {
        mDividerPaint.setAlpha(255);
        canvas.drawLine(mappedX[mPositionOfChosenPoint - mPointsMinIndex], mDrawingAreaStartY, mappedX[mPositionOfChosenPoint - mPointsMinIndex], mDrawingAreaEndY, mDividerPaint);
    }

    private void drawChosenPointPlate(float[] mappedX, Canvas canvas) {
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

        //text
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        canvas.drawText(DateTimeUtils.formatDateEEEMMMd(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.22f, mPlateXValuePaint);

        LineData[] lines = getActiveChartLines();
        switch (lines.length) {
            case 1:
                mPlateYValuePaint.setTextSize(mTextSizeLargePx);
                mPlateYValuePaint.setColor(lines[0].color);
                mPlateYValuePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(lines[0].posY[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(lines[0].color);
                mPlateNamePaint.setTextSize(mTextSizeMediumPx);
                mPlateNamePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(lines[0].name, left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.8f, mPlateNamePaint);
                break;
            case 2:
                mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
                mPlateYValuePaint.setColor(lines[0].color);
                mPlateYValuePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(String.valueOf(lines[0].posY[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(lines[0].color);
                mPlateNamePaint.setTextSize(mTextSizeSmallPx);
                mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(lines[0].name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * 0.8f, mPlateNamePaint);
                mPlateYValuePaint.setColor(lines[1].color);
                mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(lines[1].posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(lines[1].color);
                mPlateNamePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(lines[1].name, right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * 0.8f, mPlateNamePaint);
                break;
            default:
                mPlateYValuePaint.setTextSize(mTextSizeSmallPx);
                mPlateNamePaint.setTextSize(mTextSizeSmallPx);
                mPlateYValuePaint.setTextAlign(Paint.Align.LEFT);
                mPlateNamePaint.setTextAlign(Paint.Align.RIGHT);
                float heightOffset = 0.45f;
                for (LineData line : lines){
                    mPlateYValuePaint.setColor(line.color);
                    mPlateNamePaint.setColor(line.color);
                    canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
                    canvas.drawText(line.name, right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
                    heightOffset += 0.16f;
                }
                break;
        }
    }

    private void showPointDetails(float xCoord) {
        if (!showChartLines())
            return;
        mXCoordinateOfTouch = xCoord;
        mPointIsChosen = true;
    }

    private void hidePointDetails() {
        mPointIsChosen = false;
    }

}
