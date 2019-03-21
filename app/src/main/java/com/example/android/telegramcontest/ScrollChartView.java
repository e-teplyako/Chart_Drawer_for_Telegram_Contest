package com.example.android.telegramcontest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.example.android.telegramcontest.Interfaces.WidthObservable;
import com.example.android.telegramcontest.Interfaces.WidthObserver;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;

public class ScrollChartView extends View implements WidthObservable {

    private final String LOG_TAG = ScrollView.class.getSimpleName();

    private Context mContext;
    private Resources.Theme mTheme;

    ArrayList<WidthObserver> mWidthObservers;

    private Paint mChartPaint;
    private final int CHART_STROKE_WIDTH = 4;

    private Paint mBackgroundPaint;
    private Paint mHighlightedPaint;
    private Paint mSliderPaint;
    private final int HIGHLIGHTED_BORDER_COLOR = Color.parseColor("#66BDBDBD");
    private final int HIGHLIGHTED_BORDER_STROKE_WIDTH = 10;

    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;

    private long[] mXPoints;
    private int[][] mYPoints;
    private String[] mColors;
    private int[] mIndexesOfLinesToInclude;
    private Chart mChart;

    private float mHighlightedAreaLeftBorder;
    private float mHighlightedAreaRightBorder;
    private RectF mBackgroundRectLeft;
    private RectF mBackgroundRectRight;
    private RectF mHighlightedRect;
    private RectF mSliderLeft;
    private RectF mSliderRight;
    private float mSliderWidth;

    private boolean mLeftSliderIsCaught;
    private boolean mRightSliderIsCaught;
    private boolean mHighlightedAreaIsCaught;
    private float mHighlightedAreaMinimalWidth;
    private float mCurrentHighlightedAreaPosition;
    private float mCurrentHighlightedAreaWidth;



    public ScrollChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        setWillNotDraw(false);
    }

    private void init(){
        mTheme = mContext.getTheme();

        mWidthObservers = new ArrayList<>();

        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(CHART_STROKE_WIDTH);

        mBackgroundPaint = new Paint();
        TypedValue backgroundColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.chartScrollViewBackgroundColor, backgroundColor, true)) {
            mBackgroundPaint.setColor(backgroundColor.data);
        }

        mHighlightedPaint = new Paint();
        mHighlightedPaint.setColor(HIGHLIGHTED_BORDER_COLOR);
        mHighlightedPaint.setStyle(Paint.Style.STROKE);
        mHighlightedPaint.setStrokeWidth(HIGHLIGHTED_BORDER_STROKE_WIDTH);

        mSliderPaint = new Paint();
        mSliderPaint.setColor(HIGHLIGHTED_BORDER_COLOR);
        mSliderPaint.setStyle(Paint.Style.FILL);

        mBackgroundRectLeft = new RectF();
        mBackgroundRectRight = new RectF();
        mHighlightedRect = new RectF();
        mSliderLeft = new RectF();
        mSliderRight = new RectF();

        mLeftSliderIsCaught = false;
        mRightSliderIsCaught = false;
        mHighlightedAreaIsCaught = false;
    }

    public void setChartParams(Chart chart, @Nullable int[] indexesOfLinesToInclude) {
        mChart = chart;
        mXPoints = chart.getXPoints();
        mIndexesOfLinesToInclude = indexesOfLinesToInclude;
        if (mIndexesOfLinesToInclude != null) {
            mYPoints = new int[indexesOfLinesToInclude.length][chart.getSizeOfSingleArray()];
            mColors = new String[indexesOfLinesToInclude.length];
            for (int i = 0; i < indexesOfLinesToInclude.length; i++) {
                mYPoints[i] = chart.getYPoints().get(indexesOfLinesToInclude[i]);
                mColors[i] = chart.getColor(indexesOfLinesToInclude[i]);
            }

        }
        notifyObservers();
        invalidate();
    }

    public void setChartParams(@Nullable int[] indexesOfLinesToInclude) {
        if (mChart == null) return;
        mIndexesOfLinesToInclude = indexesOfLinesToInclude;
        if (mIndexesOfLinesToInclude != null) {
            mYPoints = new int[indexesOfLinesToInclude.length][mChart.getSizeOfSingleArray()];
            mColors = new String[indexesOfLinesToInclude.length];
            for (int i = 0; i < indexesOfLinesToInclude.length; i++) {
                mYPoints[i] = mChart.getYPoints().get(indexesOfLinesToInclude[i]);
                mColors[i] = mChart.getColor(indexesOfLinesToInclude[i]);
            }
        }
        notifyObservers();
        invalidate();
    }

    public void setChartParams (Chart chart, @Nullable int[] indexesOfLinesToInclude, float start, float percentage) {
        mChart = chart;
        mXPoints = chart.getXPoints();
        mIndexesOfLinesToInclude = indexesOfLinesToInclude;
        if (mIndexesOfLinesToInclude != null) {
            mYPoints = new int[indexesOfLinesToInclude.length][chart.getSizeOfSingleArray()];
            mColors = new String[indexesOfLinesToInclude.length];
            for (int i = 0; i < indexesOfLinesToInclude.length; i++) {
                mYPoints[i] = chart.getYPoints().get(indexesOfLinesToInclude[i]);
                mColors[i] = chart.getColor(indexesOfLinesToInclude[i]);
            }

        }
        mHighlightedAreaLeftBorder = start * mDrawingAreaWidth;
        mHighlightedAreaRightBorder = mHighlightedAreaLeftBorder + mDrawingAreaWidth * percentage;
        notifyObservers();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawingAreaWidth = getWidth();
        mDrawingAreaHeight = getHeight();
        mHighlightedAreaLeftBorder = mDrawingAreaWidth - (mDrawingAreaWidth * 0.3f);
        mHighlightedAreaRightBorder = mDrawingAreaWidth;
        mSliderWidth = mDrawingAreaWidth * 0.02f;
        mHighlightedAreaMinimalWidth = mDrawingAreaWidth * 0.2f;
        notifyObservers();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIndexesOfLinesToInclude != null && mIndexesOfLinesToInclude.length != 0) {
            drawChart(canvas);
        }
        mBackgroundRectLeft.set(0f, 0f, mHighlightedAreaLeftBorder, mDrawingAreaHeight);
        mBackgroundRectRight.set(mHighlightedAreaRightBorder, 0f, mDrawingAreaWidth, mDrawingAreaHeight);
        mHighlightedRect.set(mHighlightedAreaLeftBorder + mSliderWidth, 0f, mHighlightedAreaRightBorder - mSliderWidth, mDrawingAreaHeight);
        mSliderLeft.set(mHighlightedAreaLeftBorder, 0f, mHighlightedAreaLeftBorder + mSliderWidth, mDrawingAreaHeight);
        mSliderRight.set(mHighlightedAreaRightBorder - mSliderWidth, 0f, mHighlightedAreaRightBorder, mDrawingAreaHeight);
        canvas.drawRect(mBackgroundRectLeft, mBackgroundPaint);
        canvas.drawRect(mBackgroundRectRight, mBackgroundPaint);
        canvas.drawRect(mHighlightedRect, mHighlightedPaint);
        canvas.drawRect(mSliderLeft, mSliderPaint);
        canvas.drawRect(mSliderRight, mSliderPaint);
    }

    private void drawChart (Canvas canvas) {
        if (mIndexesOfLinesToInclude == null) return;

        mChartPaint.setColor(Color.RED);

        long maxX = MathUtils.getMax(mXPoints);
        long minX = MathUtils.getMin(mXPoints);
        long maxY = MathUtils.getMax(mYPoints);
        long minY = MathUtils.getMin(mYPoints);

        float[] mappedX = mapXPoints(mXPoints, minX, maxX);

        for (int i = 0; i < mYPoints.length; i++) {
            float[] mappedY = mapYPoints(mYPoints[i], minY, maxY);
            mChartPaint.setColor(Color.parseColor(mColors[i]));
            for (int j = 0; j < mappedY.length - 1; j++){
                canvas.drawLine(mappedX[j], mappedY[j], mappedX[j+1], mappedY[j+1], mChartPaint);
            }
        }
    }

    private float[] mapXPoints (long[] xPts, long min, long max) {
        long calculatedArea = max - min;
        float[] mapped = new float[xPts.length];
        for (int i = 0; i < xPts.length; i++) {
            float percentage = (float)(xPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaWidth * percentage;
        }
        return mapped;
    }

    private float[] mapYPoints (int[] yPts, long min, long max) {
        long calculatedArea = max - min;
        float[] mapped = new float[yPts.length];
        for (int i = 0; i < yPts.length; i++) {
            float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaHeight * percentage;
            mapped[i] = mDrawingAreaHeight - mapped[i];
        }
        return mapped;
    }

    void SetSlidersPos(float pos1, float pos2)
    {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if ((x >= mHighlightedAreaLeftBorder - 3f * mSliderWidth) && (x <= mHighlightedAreaLeftBorder + 3f * mSliderWidth)) {
                    mLeftSliderIsCaught = true;
                }
                else if ((x >= mHighlightedAreaRightBorder - 3f * mSliderWidth) &&(x <= mHighlightedAreaRightBorder + 3f * mSliderWidth)) {
                    mRightSliderIsCaught = true;
                }
                else if (mHighlightedRect.contains(x, y)) {
                    mHighlightedAreaIsCaught = true;
                    mCurrentHighlightedAreaPosition = x;
                    mCurrentHighlightedAreaWidth = mHighlightedAreaRightBorder - mHighlightedAreaLeftBorder;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                if (mLeftSliderIsCaught){
                    mHighlightedAreaLeftBorder = MathUtils.clamp(x,mHighlightedAreaRightBorder - mHighlightedAreaMinimalWidth, 0f);
                    notifyObservers();
                    invalidate();
                }
                else if (mRightSliderIsCaught) {
                    mHighlightedAreaRightBorder = MathUtils.clamp(x, mDrawingAreaWidth, mHighlightedAreaLeftBorder + mHighlightedAreaMinimalWidth);
                    notifyObservers();
                    invalidate();
                }
                else if (mHighlightedAreaIsCaught) {
                    float deltaX = x - mCurrentHighlightedAreaPosition;
                    mHighlightedAreaRightBorder = MathUtils.clamp(mHighlightedAreaRightBorder + deltaX, mDrawingAreaWidth, mCurrentHighlightedAreaWidth);
                    mHighlightedAreaLeftBorder = MathUtils.clamp(mHighlightedAreaLeftBorder + deltaX, mDrawingAreaWidth - mCurrentHighlightedAreaWidth, 0f);
                    mCurrentHighlightedAreaPosition = x;
                    notifyObservers();
                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                mRightSliderIsCaught = false;
                mLeftSliderIsCaught = false;
                mHighlightedAreaIsCaught = false;
                return true;
        }
        return false;
    }

    @Override
    public void registerObserver(WidthObserver widthObserver) {
        mWidthObservers.add(widthObserver);
    }

    @Override
    public void removeObserver(WidthObserver widthObserver) {
        int i = mWidthObservers.indexOf(widthObserver);
        if (i >= 0) {
            mWidthObservers.remove(i);
        }
    }

    @Override
    public void notifyObservers() {
        float start = mHighlightedAreaLeftBorder / mDrawingAreaWidth;
        float percentage = (mHighlightedAreaRightBorder - mHighlightedAreaLeftBorder) / mDrawingAreaWidth;
        for (int i = 0; i < mWidthObservers.size(); i++) {
            WidthObserver widthObserver = mWidthObservers.get(i);
            widthObserver.update(mChart, start, percentage, mIndexesOfLinesToInclude);
        }
    }
}
