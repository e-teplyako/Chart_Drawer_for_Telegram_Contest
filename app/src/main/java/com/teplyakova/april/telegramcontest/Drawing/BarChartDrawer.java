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
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.ScrollChartView;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class BarChartDrawer implements ChartDrawer {
    public class YScale
    {
        public long Height;
        public long MaxY;
        public int  Alpha;

        public long HeightStart;
        public long HeightEnd;

        public long MaxYStart;
        public long MaxYEnd;

        public int  AlphaStart;
        public int  AlphaEnd;
    }

    public class ChartBar
    {
        public LineData[] Data;

        public int      Alpha;
        public int      AlphaStart;
        public int      AlphaEnd;

        public float[][] mMappedPointsY;

        public YMaxAnimator mYMaxAnimator;

        //ArrayList<Float> optimizedPointsX = new ArrayList<Float>();
        //ArrayList<Float> optimizedPointsY = new ArrayList<Float>();

        public boolean IsVisible()
        {
            return (Alpha > 0 || AlphaEnd > 0);
        }
    }

    public class YMaxAnimator {
        public ChartBar mBar;
        public long mMaxY = -1;
        public long mTargetMaxY = -1;
        public ValueAnimator mMaxYAnimator;
        public ArrayList<YScale> mYScales = new ArrayList<YScale>();
        public boolean mLeft;

        public YMaxAnimator(ChartBar bar, boolean left) {
            mBar = bar;
            mLeft = left;
        }

        public void updateMaxY() {

            if (!mBordersSet || mBar.Data == null || mBar.Data.length == 0)
                return;

            long newYMax = MathUtils.getMaxYForStackedChart(mBar.Data, mPointsMinIndex, mPointsMaxIndex);
            newYMax = (newYMax / Y_DIVIDERS_COUNT + 1) * Y_DIVIDERS_COUNT;

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

            mapYPoints(mBar);
        }

    public void startAnimationYMax() {
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
                    mapYPoints(mBar);

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
    }

    protected final int   DRAWING_AREA_OFFSET_X_DP = 8;
    protected final int   DRAWING_AREA_OFFSET_Y_DP = 16;
    protected final int   Y_DIVIDERS_COUNT         = 6;
    protected final int   TEXT_SIZE_DP             = 12;
    protected final int   TEXT_LABEL_WIDTH_DP      = 36;
    protected final int   TEXT_LABEL_DISTANCE_DP   = 22;
    protected final int   PLATE_WIDTH_DP           = 120;
    protected final int   PLATE_HEIGHT_DP          = 180;
    protected final int   TEXT_SIZE_SMALL_DP       = 8;
    protected final int   TEXT_SIZE_MEDIUM_DP      = 12;
    protected final int   TEXT_SIZE_LARGE_DP       = 14;

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

    protected Resources.Theme mTheme;
    protected Context mContext;

    protected Paint mBarPaint;
    protected Paint     mDividerPaint;
    protected TextPaint mBaseLabelPaint;
    protected Paint     mPlatePaint;
    protected TextPaint mPlateXValuePaint;
    protected TextPaint mPlateYValuePaint;
    protected TextPaint mPlateNamePaint;
    protected Paint mOpaquePaint;

    protected long[] mPosX;
    protected long  mPos1 = -1;
    protected long  mPos2 = -1;
    protected boolean mBordersSet;
    protected float[] mMappedPointsX;
    protected float mNormWidth;
    protected int mPointsMinIndex;
    protected int mPointsMaxIndex;

    protected ChartBar mCurrentBar;
    protected ChartBar mOldBar;
    protected LineData[] mLines;
    protected RectF mBarRect;
    protected RectF mOpaqueRect;
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

    protected float mOptimTolerancePx;

    public BarChartDrawer(Context context, ChartData chartData) {
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

        mLines = chartData.lines;
        mPosX = chartData.posX;
        mOptimTolerancePx = mPosX.length >= 150 ? MathUtils.dpToPixels(4, mContext) : 0;

        mCurrentBar = new ChartBar();
        mCurrentBar.Data = chartData.lines;
        mCurrentBar.Alpha = 255;
        mCurrentBar.AlphaEnd = 255;
        mCurrentBar.mYMaxAnimator = new YMaxAnimator(mCurrentBar, true);

        mBarRect = new RectF();
        mOpaqueRect = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {

        if (mBordersSet)
            drawScaleX(mMappedPointsX, canvas);

        if (!mBordersSet || (!mCurrentBar.IsVisible() && !mOldBar.IsVisible()) || mLines == null || mLines.length == 0) {
            drawScaleY(100, 100, 255, canvas);
            return;
        }

        long ymax = MathUtils.getMaxYForStackedChart(mLines, mPointsMinIndex, mPointsMaxIndex);


        if (mCurrentBar != null && mCurrentBar.IsVisible()) {
            drawBars(mCurrentBar, canvas, 0, mCurrentBar.mYMaxAnimator.mMaxY);
        }
        if (mOldBar != null && mOldBar.IsVisible()) {
            drawBars(mOldBar, canvas, 0, mOldBar.mYMaxAnimator.mMaxY);
        }


        if (mCurrentBar != null) {
            for (YScale yScale : mCurrentBar.mYMaxAnimator.mYScales) {
                drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
                drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, true, canvas);
                /*drawScaleY(ymax, ymax, yScale.Alpha, canvas);
                drawYLabels(ymax, ymax, yScale.Alpha, true, canvas);*/
            }
        }
        if (mOldBar != null) {
            for (YScale yScale : mOldBar.mYMaxAnimator.mYScales) {
                //drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
                //drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, true, canvas);
            }
        }

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mMappedPointsX, mXCoordinateOfTouch);
            drawOpaqueRects(canvas);
            drawChosenPointPlate(canvas);
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
        mapYPoints(mCurrentBar);
        mapYPoints(mOldBar);
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
                    Log.e("MAX Y:", String.valueOf(MathUtils.getMaxYForStackedChart(mCurrentBar.Data, mPointsMinIndex, mPointsMaxIndex)));
                }
        }
    }

    @Override
    public void setLines(LineData[] lines) {
        boolean animate = false;

        if (mSetLinesFirstTime) {
            animate = false;
        }

        if (!MathUtils.equals(lines, mLines)) {
            if (!mSetLinesFirstTime) {
                animate = true;
                mOldBar = mCurrentBar;
                mOldBar.AlphaEnd = 0;
                mCurrentBar = new ChartBar();
                mCurrentBar.Data = lines;
                mCurrentBar.Alpha = 0;
                mCurrentBar.AlphaEnd = 255;
                mCurrentBar.mYMaxAnimator = new YMaxAnimator(mCurrentBar, true);
            }
            else {
                mCurrentBar.Data = lines;
            }
        }

        if (animate)
            startChartBarAnimation();

        if (mCurrentBar != null) {
            mCurrentBar.mYMaxAnimator.updateMaxY();
        }
        if (mOldBar != null) {
            mOldBar.mYMaxAnimator.updateMaxY();
        }


        mLines = lines;
        mapYPoints(mCurrentBar);
        mapYPoints(mOldBar);
        mapXPoints();

        hidePointDetails();

        mSetLinesFirstTime = false;
    }

    @Override
    public boolean setBorders(float normPos1, float normPos2) {
        mBordersSet = true;

        mNormWidth = normPos2 - normPos1;
        long pos1;
        long pos2;
        long xMin = MathUtils.getMin(mPosX);
        long xMax = MathUtils.getMax(mPosX);
        long width = xMax - xMin;
        pos1 = (long) Math.floor(normPos1 * width) + xMin;
        pos2 = (long) Math.ceil(normPos2 * width) + xMin;

        boolean result = false;
        if (mPos1 != pos1 || mPos2 != pos2) {
            result = true;
        }

        mPos1 = pos1;
        mPos2 = pos2;

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * mDrawingAreaOffsetXPx) / mDrawingAreaWidth);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        mapXPoints();
        mapYPoints(mCurrentBar);

        if (mCurrentBar != null) {
            mCurrentBar.mYMaxAnimator.updateMaxY();
        }
        if (mOldBar != null) {
            mOldBar.mYMaxAnimator.updateMaxY();
        }

        hidePointDetails();

        return result;
    }

    @Override
    public void setAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mViewAnimatorListener = listener;
    }

    protected void startChartBarAnimation()
    {
        if (mSetLinesAnimator != null)
        {
            mSetLinesAnimator.cancel();
            mSetLinesAnimator = null;
        }

        if (mCurrentBar != null) {
            mCurrentBar.AlphaStart = mCurrentBar.Alpha;
        }
        if (mOldBar != null) {
            mOldBar.AlphaStart = mOldBar.Alpha;
        }

        final String KEY_PHASE = "phase";

        mSetLinesAnimator = new ValueAnimator();
        mSetLinesAnimator.setValues(PropertyValuesHolder.ofFloat(KEY_PHASE, 0, 1));
        mSetLinesAnimator.setDuration(400);

        mSetLinesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float t = (float) animator.getAnimatedValue(KEY_PHASE);

                if (mCurrentBar != null) {
                    if (mCurrentBar.Alpha != mCurrentBar.AlphaEnd)
                        mCurrentBar.Alpha = (int)MathUtils.lerp(mCurrentBar.AlphaStart,   mCurrentBar.AlphaEnd,   t);
                }
                if (mOldBar != null) {
                    if (mOldBar.Alpha != mOldBar.AlphaEnd) {
                        mOldBar.Alpha = (int) MathUtils.lerp(mOldBar.AlphaStart, mOldBar.AlphaEnd, t);
                    }
                    else {
                        mOldBar = null;
                    }
                }
            }
        });
        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    protected void setUpPaints() {
        mBarPaint = new Paint();
        mBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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

        mOpaquePaint = new Paint();
        TypedValue opaqueColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.opaqueBackground, opaqueColor, true)) {
            mOpaquePaint.setColor(opaqueColor.data);
        }
        mOpaquePaint.setStyle(Paint.Style.FILL);
    }

    protected void mapXPoints()
    {
        if (!mBordersSet)
            return;

        mMappedPointsX = mapXPoints(mPosX, mPos1, mPos2);
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

    protected void mapYPoints(ChartBar bar)
    {
        if (!mBordersSet)
            return;


        if (bar != null && bar.IsVisible()){
            bar.mMappedPointsY = mapYPoints(bar.Data, 0, bar.mYMaxAnimator.mMaxY);
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

    protected void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
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

    private void drawYLabels (long height, long yMax, int alpha, boolean left, Canvas canvas) {
        float xCoord;
        if (left) {
            mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
            xCoord = mDrawingAreaStartX;
        }
        else {
            mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
            xCoord = mDrawingAreaEndX;
        }
        float spaceBetweenDividers = (float)yMax / height * mDrawingAreaHeight / Y_DIVIDERS_COUNT;

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

    protected void drawBars(ChartBar bar, Canvas canvas, long yMin, long yMax) {
        float halfBarWidth = (mMappedPointsX[mMappedPointsX.length - 1] - mMappedPointsX[0]) / (mMappedPointsX.length - 1) / 2;
        float startY;

        for (int i = 0; i < bar.mMappedPointsY.length; i++) {
            startY = mDrawingAreaEndY;
            for (int j = 0; j < bar.mMappedPointsY[0].length; j++) {
                mBarRect.set(mMappedPointsX[i] - halfBarWidth, bar.mMappedPointsY[i][j], mMappedPointsX[i] + halfBarWidth, startY);
                mBarPaint.setColor(bar.Data[j].color);
                mBarPaint.setAlpha(bar.Alpha);
                canvas.drawRect(mBarRect, mBarPaint);
                startY = bar.mMappedPointsY[i][j];
        }
        }
    }

    protected float[][] mapYPoints(LineData[] lines, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[][] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1][lines.length];
        float startY;

        for (int i = 0; i < mapped.length; i++) {
            startY = mDrawingAreaEndY;
            for (int j = 0; j < lines.length; j++) {
                float percentage = (float) (lines[j].posY[i + mPointsMinIndex] - yMin) / (float) calculatedArea;
                mapped[i][j] = startY - mDrawingAreaHeight * percentage;
                startY = mapped[i][j];
            }
        }
        return mapped;
    }

    protected void drawOpaqueRects (Canvas canvas) {
        float halfBarWidth = (mMappedPointsX[mMappedPointsX.length - 1] - mMappedPointsX[0]) / (mMappedPointsX.length - 1) / 2;
        mOpaqueRect.set(0f, mDrawingAreaStartY, mMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - halfBarWidth, mDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
        mOpaqueRect.set(mMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + halfBarWidth, mDrawingAreaStartY, mViewWidth, mDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
    }

    protected void drawChosenPointPlate(Canvas canvas) {
        //plate
        float top = mDrawingAreaHeight * 0.05f + mDrawingAreaWidth * 0.05f;
        float bottom = top + mPlateHeightPx;
        float left;
        float right;
        float offset = mDrawingAreaWidth * 0.05f;
        if ((mMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset + mPlateWidthPx) >= mDrawingAreaEndX) {
            right = mMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - mPlateWidthPx;
        } else {
            left = mMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
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
        mPlateXValuePaint.setTextSize(mTextSizeLargePx);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.1f, mPlateXValuePaint);

        mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateNamePaint.setTextSize(mTextSizeMediumPx);
        mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
        mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
        float heightOffset = 0.2f;
        long sumOfChosenValues = 0;
        for (LineData line : mLines){
            mPlateYValuePaint.setColor(line.color);
            mPlateNamePaint.setColor(line.color);
            canvas.drawText(line.name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
            canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
            heightOffset += 0.1f;
            sumOfChosenValues += line.posY[mPositionOfChosenPoint];
        }
        mPlateNamePaint.setColor(Color.BLACK);
        mPlateYValuePaint.setColor(Color.BLACK);
        canvas.drawText("All", left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
        canvas.drawText(String.valueOf(sumOfChosenValues), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);

    }


    protected void showPointDetails(float xCoord) {
        if (mLines == null || mLines.length == 0)
            return;
        mXCoordinateOfTouch = xCoord;
        mPointIsChosen = true;
    }

    protected void hidePointDetails() {
        mPointIsChosen = false;
    }
}
