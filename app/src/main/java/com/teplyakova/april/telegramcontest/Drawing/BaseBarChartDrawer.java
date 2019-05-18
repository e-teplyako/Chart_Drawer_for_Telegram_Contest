package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class BaseBarChartDrawer extends BaseChartDrawer{
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

    public class ChartArea
    {
        public LineData Data;

        public float    PosYCoefficientStart;
        public float    PosYCoefficient;
        public float    PosYCoefficientEnd;

        float[]         mChartMappedPointsY;
        float[]         mScrollMappedPointsY;

        boolean isVisible() {
            return (PosYCoefficient > 0 || PosYCoefficientEnd > 0);
        }
    }

    class YMaxAnimator {
        long              mMaxY         = -1;
        long              mTargetMaxY   = -1;
        ValueAnimator     mMaxYAnimator;
        ArrayList<YScale> mYScales      = new ArrayList<YScale>();

        YMaxAnimator() {
        }

        void updateMaxY() {

            if (!mBordersSet || !showChartAreas())
                return;

            LineData[] lines = getActiveChartLines();
            long newYMax = MathUtils.getMaxYForStackedChart(lines, mPointsMinIndex, mPointsMaxIndex);
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

            mapYPointsForChartView(getMaxPosYCoefficient(), mYMaxAnimator.mMaxY);
        }

        void startAnimationYMax() {
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
                    mapYPointsForChartView(getMaxPosYCoefficient(), mYMaxAnimator.mMaxY);

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

    private final int            Y_DIVIDERS_COUNT   = 6;

    private Paint                mBarPaint;
    private Paint                mOpaquePaint;

    ArrayList<ChartArea>         mAreas             = new ArrayList<>();
    LineData[]                   mLines;

    RectF                        mBarRect;
    RectF                        mOpaqueRect;

    private boolean              mSetLinesFirstTime = true;

    private YMaxAnimator         mYMaxAnimator;

    private ValueAnimator        mSetLinesAnimator;

    private Path[]               mChartPaths;
    private Path[]               mScrollPaths;


    BaseBarChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mLines = chartData.lines;

        for (LineData lineData : chartData.lines)
        {
            ChartArea chartArea = new ChartArea();
            chartArea.Data = lineData;
            chartArea.mChartMappedPointsY = new float[lineData.posY.length];
            chartArea.mScrollMappedPointsY = new float[lineData.posY.length];
            chartArea.PosYCoefficient = 1;
            chartArea.PosYCoefficientStart = 1;
            chartArea.PosYCoefficientEnd = 1;
            mAreas.add(chartArea);
        }

        mYMaxAnimator = new YMaxAnimator();

        mBarRect = new RectF();
        mOpaqueRect = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {

        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!mBordersSet || !showChartAreas()) {
            drawScaleY(100, 100, 255, canvas);
            drawRects(canvas);
            return;
        }

        drawAreas(canvas);

        for (YScale yScale : mYMaxAnimator.mYScales) {
            drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
            drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
        }

        if (mPointIsChosen) {
            drawOpaqueRects(canvas);
            drawChosenPointPlate(canvas);
        }

        drawRects(canvas);
    }

    @Override
    public void setViewDimens(float width, float height, float drawingAreaOffsetXPx, float drawingAreaOffsetYPx, float scrollDrawingAreaHeightPx) {
        super.setViewDimens(width, height, drawingAreaOffsetXPx, drawingAreaOffsetYPx, scrollDrawingAreaHeightPx);

        mapXPointsForScrollView();
        mapYPointsForScrollView(getMaxPosYCoefficient());
    }

    @Override
    public void setLines(LineData[] lines) {
        if (lines == null || lines.length == 0)
            hidePointDetails();

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

        mYMaxAnimator.updateMaxY();

        mSetLinesFirstTime = false;
    }

    public boolean setBorders(float normPos1, float normPos2) {
        boolean result = super.setBorders(normPos1, normPos2);

        mapXPointsForChartView();
        mapYPointsForChartView(getMaxPosYCoefficient(), mYMaxAnimator.mMaxY);

        mYMaxAnimator.updateMaxY();

        return result;
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
                    if (area.PosYCoefficient != area.PosYCoefficientEnd) {
                        area.PosYCoefficient = MathUtils.lerp(area.PosYCoefficientStart, area.PosYCoefficientEnd, t);
                    }
                }

                mapYPointsForChartView(getMaxPosYCoefficient(), mYMaxAnimator.mMaxY);
                mapYPointsForScrollView(getMaxPosYCoefficient());

            }
        });

        mSetLinesAnimator.addUpdateListener(mViewAnimatorListener);

        mSetLinesAnimator.start();
    }

    private void mapYPointsForChartView(float coefficient, long yMax) {
        if (!mBordersSet)
            return;

        long[] previous = new long[mPointsMaxIndex - mPointsMinIndex + 1];
        for (ChartArea area : mAreas) {
            if (area.isVisible()) {
                area.mChartMappedPointsY = new float[mPointsMaxIndex - mPointsMinIndex + 1];
                for (int i = 0, j = mPointsMinIndex; i < area.mChartMappedPointsY.length; i++, j++) {
                    float percentage = (area.Data.posY[j] * area.PosYCoefficient + previous[i]) / yMax;
                    area.mChartMappedPointsY[i] = mChartDrawingAreaEndY - coefficient * mChartDrawingAreaHeight * percentage;
                    previous[i] += area.Data.posY[j] * area.PosYCoefficient;
                }
            }
        }
        prepareChartPaths();
    }

    private void mapYPointsForScrollView(float coefficient)
    {   if (!showChartAreas())
        return;

        ChartArea[] areas = getVisibleChartAreas();
        float calculatedArea = MathUtils.getMaxYForStackedChart(areas, 0, areas[0].Data.posY.length - 1);

        long[] previous = new long[mScrollMappedPointsX.length];
        for (ChartArea area : areas) {
            area.mScrollMappedPointsY = new float[mScrollMappedPointsX.length];
            for (int i = 0; i < area.mScrollMappedPointsY.length; i++) {
                float percentage =  (area.Data.posY[i] * area.PosYCoefficient + previous[i]) / calculatedArea;
                area.mScrollMappedPointsY[i] = mScrollDrawingAreaEndY - coefficient * mScrollDrawingAreaHeight * percentage;
                previous[i] += area.Data.posY[i] * area.PosYCoefficient;
            }
        }
        prepareScrollPaths();
    }

    private void prepareChartPaths() {
        float halfBarWidth = (mChartMappedPointsX[mChartMappedPointsX.length - 1] - mChartMappedPointsX[0]) / (mChartMappedPointsX.length - 1) / 2;
        float startX = mChartMappedPointsX[0] - halfBarWidth;
        float startY = mChartDrawingAreaEndY;
        ChartArea[] areas = getVisibleChartAreas();
        mChartPaths = new Path[areas.length];

        float[] previous = new float[mChartMappedPointsX.length];
        Arrays.fill(previous, mChartDrawingAreaEndY);
        Path path = new Path();
        path.moveTo(startX, startY);
        for (int i = 0; i < areas.length; i++) {
            for (int j = 0; j < areas[i].mChartMappedPointsY.length; j++) {
                path.lineTo(mChartMappedPointsX[j] - halfBarWidth, areas[i].mChartMappedPointsY[j]);
                path.lineTo(mChartMappedPointsX[j] + halfBarWidth, areas[i].mChartMappedPointsY[j]);
            }
            for (int n = previous.length - 1; n >= 0; n--) {
                path.lineTo(mChartMappedPointsX[n] + halfBarWidth, previous[n]);
                path.lineTo(mChartMappedPointsX[n] - halfBarWidth, previous[n]);
                previous[n] = areas[i].mChartMappedPointsY[n];
            }
            mChartPaths[i] = path;
            path = new Path();
            path.moveTo(mChartMappedPointsX[0] - halfBarWidth, areas[i].mChartMappedPointsY[0]);
        }

    }

    private void prepareScrollPaths() {
        float halfBarWidth = (mScrollMappedPointsX[mScrollMappedPointsX.length - 1] - mScrollMappedPointsX[0]) / (mScrollMappedPointsX.length - 1) / 2;
        float startX = mScrollMappedPointsX[0] - halfBarWidth;
        float startY = mScrollDrawingAreaEndY;
        ChartArea[] areas = getVisibleChartAreas();
        mScrollPaths = new Path[areas.length];

        float[] previous = new float[mScrollMappedPointsX.length];
        Arrays.fill(previous, mScrollDrawingAreaEndY);
        Path path = new Path();
        path.moveTo(startX, startY);
        for (int i = 0; i < areas.length; i++) {
            for (int j = 0; j < areas[i].mScrollMappedPointsY.length; j++) {
                path.lineTo(mScrollMappedPointsX[j] - halfBarWidth, areas[i].mScrollMappedPointsY[j]);
                path.lineTo(mScrollMappedPointsX[j] + halfBarWidth, areas[i].mScrollMappedPointsY[j]);
            }
            for (int n = previous.length - 1; n >= 0; n--) {
                path.lineTo(mScrollMappedPointsX[n] + halfBarWidth, previous[n]);
                path.lineTo(mScrollMappedPointsX[n] - halfBarWidth, previous[n]);
                previous[n] = areas[i].mScrollMappedPointsY[n];
            }
            mScrollPaths[i] = path;
            path = new Path();
            path.moveTo(mScrollMappedPointsX[0] - halfBarWidth, areas[i].mScrollMappedPointsY[0]);
        }
    }

    @Override
    protected void setUpPaints() {
        super.setUpPaints();

        mBarPaint = new Paint();
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setStrokeCap(Paint.Cap.SQUARE);
        mBarPaint.setAntiAlias(true);

        mOpaquePaint = new Paint();
        TypedValue opaqueColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.opaqueBackground, opaqueColor, true)) {
            mOpaquePaint.setColor(opaqueColor.data);
        }
        mOpaquePaint.setStyle(Paint.Style.FILL);
    }

    private void drawScaleY (long height, long yMax, int alpha, Canvas canvas) {
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

    private void drawYLabels (long height, long yMax, int alpha, Canvas canvas) {
        float xCoord = mChartDrawingAreaStartX;
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / Y_DIVIDERS_COUNT;

        long step = 0;
        float yLabelCoord = mChartDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }
    }

    private void drawAreas(Canvas canvas) {
        ChartArea[] areas = getVisibleChartAreas();

        if (areas == null || areas.length == 0)
            return;

        for (int i = 0; i < mChartPaths.length; i++) {
            mBarPaint.setColor(areas[i].Data.color);
            canvas.drawPath(mChartPaths[i], mBarPaint);

            canvas.save();
            Path clipPath = new Path();
            RectF clipRect = new RectF(mScrollDrawingAreaStartX, mScrollDrawingAreaStartY, mScrollDrawingAreaEndX, mScrollDrawingAreaEndY);
            clipPath.addRoundRect(clipRect, 20, 20, Path.Direction.CW);
            canvas.clipPath(clipPath);

            canvas.drawPath(mScrollPaths[i], mBarPaint);

            canvas.restore();
        }
    }

    private void drawOpaqueRects (Canvas canvas) {
        float halfBarWidth = (mChartMappedPointsX[mChartMappedPointsX.length - 1] - mChartMappedPointsX[0]) / (mChartMappedPointsX.length - 1) / 2;
        mOpaqueRect.set(0f, mChartDrawingAreaStartY, mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - halfBarWidth, mChartDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
        mOpaqueRect.set(mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + halfBarWidth, mChartDrawingAreaStartY, mViewWidth, mChartDrawingAreaEndY);
        canvas.drawRect(mOpaqueRect, mOpaquePaint);
    }

    protected abstract void drawChosenPointPlate(Canvas canvas);

    @Override
    protected void showPointDetails(float xCoord) {
        if (mLines == null || mLines.length == 0)
            return;

        super.showPointDetails(xCoord);
    }

    private boolean showChartAreas() {
        for (ChartArea area : mAreas)
            if (area.isVisible())
                return true;

        return false;
    }

    LineData[] getActiveChartLines()
    {
        ArrayList<LineData> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.PosYCoefficientEnd > 0)
                arrayList.add(area.Data);

        return arrayList.toArray(new LineData[arrayList.size()]);
    }

    private ChartArea[] getVisibleChartAreas()
    {
        ArrayList<ChartArea> arrayList = new ArrayList<>();

        for (ChartArea area : mAreas)
            if (area.isVisible())
                arrayList.add(area);

        return arrayList.toArray(new ChartArea[arrayList.size()]);
    }

    private float getMaxPosYCoefficient() {
        float max = 0;
        ChartArea[] areas = getVisibleChartAreas();
        for (ChartArea area : areas) {
            if (area.PosYCoefficient > max)
                max = area.PosYCoefficient;
        }

        return max;
    }
}
