package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
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
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class BaseBarChartDrawer implements ChartDrawer{
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

        public Path[] ChartPaths;
        public Path[] ScrollPaths;

        public int      Alpha;
        public int      AlphaStart;
        public int      AlphaEnd;

        public float[][] mChartMappedPointsY;
        public float[][] mScrollMappedPointsY;

        public YMaxAnimator mYMaxAnimator;

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

            mapYPointsForChartView(mBar);
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
                    mapYPointsForChartView(mBar);

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

    protected final int   Y_DIVIDERS_COUNT         = 6;
    protected final int   TEXT_SIZE_DP             = 12;
    protected final int   TEXT_LABEL_WIDTH_DP      = 36;
    protected final int   TEXT_LABEL_DISTANCE_DP   = 22;
    protected final int   PLATE_WIDTH_DP           = 140;
    protected final int   PLATE_HEIGHT_DP          = 180;
    protected final int   TEXT_SIZE_SMALL_DP       = 8;
    protected final int   TEXT_SIZE_MEDIUM_DP      = 12;
    protected final int   TEXT_SIZE_LARGE_DP       = 14;
    protected final int   SLIDER_WIDTH_DP                   = 10;
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

    protected Paint mBarPaint;
    protected Paint     mDividerPaint;
    protected TextPaint mBaseLabelPaint;
    protected Paint     mPlatePaint;
    protected TextPaint mPlateXValuePaint;
    protected TextPaint mPlateYValuePaint;
    protected TextPaint mPlateNamePaint;
    protected Paint mOpaquePaint;
    protected Paint mBackgroundPaint;
    protected Paint mSliderPaint;

    protected long[] mPosX;
    protected long  mPos1 = -1;
    protected long  mPos2 = -1;
    protected int mPosFirstVisible;
    protected boolean mBordersSet;
    protected float[] mChartMappedPointsX;
    protected float[] mScrollMappedPointsX;
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

    protected float mSliderPositionLeft;
    protected float mSliderPositionRight;
    protected float mNormSliderPosLeft = 0.8f;
    protected float mNormSliderPosRight = 1;

    protected Path mScrollBackground;
    protected Path mSlider;

    protected boolean mLeftSliderIsCaught = false;
    protected boolean mRightSliderIsCaught = false;
    protected boolean mChosenAreaIsCaught = false;
    protected float mCurrChosenAreaPosition;
    protected float mCurrChosenAreaWidth;
    protected float mChosenAreaMinimalWidth;

    protected boolean mSliderPositionsSet = false;


    public BaseBarChartDrawer(Context context, ChartData chartData) {
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

        setUpPaints();

        mLines = chartData.lines;
        mPosX = chartData.posX;

        mCurrentBar = new ChartBar();
        mCurrentBar.Data = chartData.lines;
        mCurrentBar.Alpha = 255;
        mCurrentBar.AlphaEnd = 255;
        mCurrentBar.mYMaxAnimator = new YMaxAnimator(mCurrentBar, true);

        mBarRect = new RectF();
        mOpaqueRect = new RectF();
        mScrollBackground = new Path();
        mSlider = new Path();
    }

    @Override
    public void draw(Canvas canvas) {

        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!mBordersSet || (!mCurrentBar.IsVisible() && !mOldBar.IsVisible()) || mLines == null || mLines.length == 0) {
            drawScaleY(100, 100, 255, canvas);
            drawRects(canvas);
            return;
        }

        if (mCurrentBar != null && mCurrentBar.IsVisible() && mCurrentBar.Data != null && mCurrentBar.Data.length > 0) {
            drawBars(mCurrentBar, canvas, mChartMappedPointsX, mCurrentBar.mChartMappedPointsY, mChartDrawingAreaEndY);
            drawBars(mCurrentBar, canvas, mScrollMappedPointsX, mCurrentBar.mScrollMappedPointsY, mScrollDrawingAreaEndY);
        }
        if (mOldBar != null && mOldBar.IsVisible() && mOldBar.Data != null && mOldBar.Data.length > 0) {
            drawBars(mOldBar, canvas, mChartMappedPointsX, mOldBar.mChartMappedPointsY, mChartDrawingAreaEndY);
            drawBars(mOldBar, canvas, mScrollMappedPointsX, mOldBar.mScrollMappedPointsY, mScrollDrawingAreaEndY);
        }

        if (mCurrentBar != null) {
            for (YScale yScale : mCurrentBar.mYMaxAnimator.mYScales) {
                drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
                drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, true, canvas);
            }
        }

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mChartMappedPointsX, mXCoordinateOfTouch);
            drawOpaqueRects(canvas);
            drawChosenPointPlate(canvas);
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
        mapYPointsForChartView(mCurrentBar);
        mapYPointsForScrollView(mCurrentBar);
        mapYPointsForChartView(mOldBar);
        mapYPointsForScrollView(mOldBar);
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

    @Override
    public void setLines(LineData[] lines) {
        boolean animate = false;

        if (mSetLinesFirstTime) {
            animate = false;
        }

        if (!MathUtils.equals(lines, mLines)) {
            if (!mSetLinesFirstTime) {
                animate = true;
                mOldBar = new ChartBar();
                mOldBar.Data = mCurrentBar.Data;
                mOldBar.Alpha = mCurrentBar.Alpha;
                mOldBar.AlphaEnd = 0;
                mOldBar.AlphaStart = mOldBar.Alpha;
                mOldBar.mYMaxAnimator = new YMaxAnimator(mOldBar, true);
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
        mapXPointsForChartView();
        mapYPointsForChartView(mCurrentBar);
        mapYPointsForChartView(mOldBar);
        mapXPointsForScrollView();
        mapYPointsForScrollView(mCurrentBar);
        mapYPointsForScrollView(mOldBar);

        hidePointDetails();

        mSetLinesFirstTime = false;
    }

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

        long distanceToScreenBorder = (long) Math.ceil (((mPos2 - mPos1) * mDrawingAreaOffsetXPx) / mChartDrawingAreaWidth);

        mPointsMinIndex = MathUtils.getIndexOfNearestLeftElement(mPosX, mPos1 - distanceToScreenBorder);
        mPointsMaxIndex = MathUtils.getIndexOfNearestRightElement(mPosX,  mPos2 + distanceToScreenBorder);

        mapXPointsForChartView();
        mapYPointsForChartView(mCurrentBar);

        if (mCurrentBar != null) {
            mCurrentBar.mYMaxAnimator.updateMaxY();
        }
        if (mOldBar != null) {
            mOldBar.mYMaxAnimator.updateMaxY();
        }
        hidePointDetails();

        mPosFirstVisible = MathUtils.getIndexOfNearestRightElement(mPosX, mPos1);

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
        mDividerPaint.setAlpha(255);
        mDividerPaint.setStrokeWidth(1);

        mBaseLabelPaint = new TextPaint();
        mBaseLabelPaint.setTextSize(mTextSizePx);
        TypedValue baseLabelColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.baseLabelColor, baseLabelColor, true)) {
            mBaseLabelPaint.setColor(baseLabelColor.data);
        }
        mBaseLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        mBaseLabelPaint.setAntiAlias(true);

        mPlatePaint = new Paint();

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
        mPlateYValuePaint.setAntiAlias(true);

        mPlateNamePaint = new TextPaint();
        mPlateNamePaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        mPlateNamePaint.setColor(textColor.data);
        mPlateNamePaint.setAntiAlias(true);

        mOpaquePaint = new Paint();
        TypedValue opaqueColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.opaqueBackground, opaqueColor, true)) {
            mOpaquePaint.setColor(opaqueColor.data);
        }
        mOpaquePaint.setStyle(Paint.Style.FILL);

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
        mSliderPaint.setStrokeWidth(2);
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

    protected void mapYPointsForChartView(ChartBar bar)
    {
        if (!mBordersSet)
            return;


        if (bar != null && bar.IsVisible()){
            bar.mChartMappedPointsY = mapYPointsForChartView(bar.Data, 0, bar.mYMaxAnimator.mMaxY);
            preparePaths(bar, mChartMappedPointsX, bar.mChartMappedPointsY, mChartDrawingAreaEndY, true);
        }
    }

    protected void mapYPointsForScrollView(ChartBar bar)
    {
        if (bar != null && bar.IsVisible() && bar.Data != null && bar.Data.length > 0){
            bar.mScrollMappedPointsY = mapYPointsForScrollView(bar.Data);
            preparePaths(bar, mScrollMappedPointsX, bar.mScrollMappedPointsY, mScrollDrawingAreaEndY, false);
        }
    }

    protected float[][] mapYPointsForChartView(LineData[] lines, long yMin, long yMax) {
        long calculatedArea = yMax - yMin;
        float[][] mapped = new float[mPointsMaxIndex - mPointsMinIndex + 1][lines.length];
        float startY;

        for (int i = 0; i < mapped.length; i++) {
            startY = mChartDrawingAreaEndY;
            for (int j = 0; j < lines.length; j++) {
                float percentage = (float) (lines[j].posY[i + mPointsMinIndex] - yMin) / (float) calculatedArea;
                mapped[i][j] = startY - mChartDrawingAreaHeight * percentage;
                startY = mapped[i][j];
            }
        }
        return mapped;
    }

    protected float[][] mapYPointsForScrollView(LineData[] lines) {
        long yMax = MathUtils.getMaxYForStackedChart(lines, 0, lines[0].posY.length - 1);
        long calculatedArea = yMax;
        float[][] mapped = new float[mScrollMappedPointsX.length][lines.length];
        float startY;

        for (int i = 0; i < mapped.length; i++) {
            startY = mScrollDrawingAreaEndY;
            for (int j = 0; j < lines.length; j++) {
                float percentage = (float) (lines[j].posY[i]) / (float) calculatedArea;
                mapped[i][j] = startY - mScrollDrawingAreaHeight * percentage;
                startY = mapped[i][j];
            }
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

    protected void drawScaleY (long height, long yMax, int alpha, Canvas canvas)
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
        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / Y_DIVIDERS_COUNT;

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

    protected void preparePaths(ChartBar bar, float[] mappedX, float[][] mappedY, float startDrawingPointY, boolean chartPaths) {
        float halfBarWidth = (mappedX[mappedX.length - 1] - mappedX[0]) / (mappedX.length - 1) / 2;
        float startX = mappedX[0] - halfBarWidth;
        float startY = startDrawingPointY;
        if (chartPaths) {
            bar.ChartPaths = new Path[bar.Data.length];
        }
        else {
            bar.ScrollPaths = new Path[bar.Data.length];
        }

        float[] previous = new float[mappedY.length];
        Arrays.fill(previous, startDrawingPointY);
        Path path = new Path();
        path.moveTo(startX, startY);
        for (int i = 0; i < bar.Data.length; i ++) {
            for (int j = 0; j < mappedY.length; j++) {
                path.lineTo(mappedX[j] - halfBarWidth, mappedY[j][i]);
                path.lineTo(mappedX[j] + halfBarWidth, mappedY[j][i]);
            }
            for (int n = previous.length - 1; n >= 0 ; n--) {
                path.lineTo(mappedX[n] + halfBarWidth, previous[n]);
                path.lineTo(mappedX[n] - halfBarWidth, previous[n]);
                previous[n] = mappedY[n][i];
            }
            if (chartPaths) {
                bar.ChartPaths[i] = path;
            }
            else{
                bar.ScrollPaths[i] = path;
            }
            path = new Path();
            path.moveTo(mappedX[0] - halfBarWidth, mappedY[0][i]);
        }

    }

    protected void drawBars(ChartBar bar, Canvas canvas, float[] mappedX, float[][] mappedY, float startDrawingPoint) {
        for (int i = 0; i < bar.ChartPaths.length; i++) {
            mBarPaint.setColor(bar.Data[i].color);
            mBarPaint.setAlpha(bar.Alpha);
            canvas.drawPath(bar.ChartPaths[i], mBarPaint);

            canvas.save();
            Path clipPath = new Path();
            RectF clipRect = new RectF(mScrollDrawingAreaStartX, mScrollDrawingAreaStartY, mScrollDrawingAreaEndX, mScrollDrawingAreaEndY);
            clipPath.addRoundRect(clipRect, 20, 20, Path.Direction.CW);
            canvas.clipPath(clipPath);

            canvas.drawPath(bar.ScrollPaths[i], mBarPaint);

            canvas.restore();
        }
    }

    protected void drawTopDatesText (Canvas canvas) {
        String dateText = DateTimeUtils.formatDatedMMMMMyyyy(mPosX[mPosFirstVisible]) + " - " + DateTimeUtils.formatDatedMMMMMyyyy(mPos2);
        mPlateXValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateXValuePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateText, mChartDrawingAreaEndX, mTopDatesOffsetYPx, mPlateXValuePaint);
    }

    protected void drawOpaqueRects (Canvas canvas) {
        float halfBarWidth = (mChartMappedPointsX[mChartMappedPointsX.length - 1] - mChartMappedPointsX[0]) / (mChartMappedPointsX.length - 1) / 2;
        mOpaqueRect.set(0f, mChartDrawingAreaStartY, mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - halfBarWidth, mChartDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
        mOpaqueRect.set(mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + halfBarWidth, mChartDrawingAreaStartY, mViewWidth, mChartDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
    }

    protected void drawRects(Canvas canvas) {
        canvas.drawPath(mScrollBackground, mBackgroundPaint);
        canvas.drawPath(mSlider, mSliderPaint);
    }

    protected abstract void drawChosenPointPlate(Canvas canvas);

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
