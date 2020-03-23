package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class StandardLineChartDrawer extends BaseLineChartDrawer {

    class YScale extends BaseLineChartDrawer.YScale {
    }

    class ChartLine extends BaseLineChartDrawer.ChartLine {
    }

    class YMaxAnimator extends BaseLineChartDrawer.YMaxAnimator {

        YMaxAnimator() {
            super();
        }

        void updateMinMaxY() {
            LineData[] activeLines = getActiveChartLines();

            if (!mBordersSet || activeLines.length == 0)
                return;

            long newYMax = MathUtils.getMaxY(activeLines, mPointsMinIndex, mPointsMaxIndex);
            long newYMin = MathUtils.getMinY(activeLines, mPointsMinIndex, mPointsMaxIndex);
            newYMax = (((newYMax - newYMin) / Y_DIVIDERS_COUNT) + 1) * Y_DIVIDERS_COUNT + newYMin;


            if (newYMax != mTargetMaxY || newYMin != mTargetMinY)
            {
                boolean firstTime = (mTargetMaxY < 0 || mTargetMinY < 0);
                mTargetMaxY = newYMax;
                mTargetMinY = newYMin;

                if (firstTime)
                {
                    mMaxY = mTargetMaxY;
                    mMinY = mTargetMinY;

                    YScale yScale = new YScale();
                    yScale.Height = mMaxY;
                    yScale.MaxY   = mMaxY;
                    yScale.MinY   = mMinY;
                    yScale.Alpha  = 255;
                    mYScales.add(yScale);
                }
                else {
                    startAnimationYMax();
                }
            }

            mapYPointsForChartView();
        }
    }

    private YMaxAnimator mYMaxAnimator;

    long                  mGlobalMinY;
    long                  mGlobalMaxY;

    public StandardLineChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mGlobalMinY = MathUtils.getMinY(chartData.getLines());
        mGlobalMaxY = MathUtils.getMaxY(chartData.getLines());

        for (LineData lineData : chartData.getLines())
        {
            ChartLine chartLine = new ChartLine();
            chartLine.Data      = lineData;
            chartLine.Alpha     = 255;
            chartLine.AlphaEnd  = 255;
            mLines.add(chartLine);
        }

        mYMaxAnimator = new YMaxAnimator();
    }

    @Override
    public void setLines(LineData[] lines) {
        super.setLines(lines);

        mYMaxAnimator.updateMinMaxY();
    }

    @Override
    public boolean setBorders(float normPosX1, float normPosX2) {
        boolean result =  super.setBorders(normPosX1, normPosX2);

        mYMaxAnimator.updateMinMaxY();

       return result;
    }

    public void draw(Canvas canvas) {
        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!showChartLines() || !mBordersSet)
        {
            drawScaleY(100, 100, 255, canvas);
            drawRects(canvas);
            return;
        }

        for (BaseLineChartDrawer.YScale yScale : mYMaxAnimator.mYScales) {
            drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
        }

        if (mPointIsChosen) {
            drawVerticalDivider(mChartMappedPointsX, canvas);
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.isVisible()) {
                mChartPaint.setStrokeWidth(6);
                drawChartLineInChartView(line, canvas, mChartMappedPointsX, line.mChartMappedPointsY);
                mChartPaint.setStrokeWidth(4);
                drawChartLineInScrollView(line, canvas, line.mScrollOptimizedPointsX, line.mScrollOptimizedPointsY);
            }
        }

        if (mPointIsChosen)
            for (BaseLineChartDrawer.ChartLine line : mLines) {
                if (line.isVisible())
                    drawChosenPointCircle(mChartMappedPointsX, line.mChartMappedPointsY, line.Data.color, line.Alpha, canvas);
            }



        for (BaseLineChartDrawer.YScale yScale : mYMaxAnimator.mYScales) {
            drawYLabels(yScale.Height, yScale.MaxY, yScale.MinY, getChartLineAlpha(yScale.Alpha), canvas);
        }

        if (mPointIsChosen) {
            drawChosenPointPlate(mChartMappedPointsX, canvas);
        }

        drawRects(canvas);
    }

    protected void mapYPointsForChartView() {
        if (!mBordersSet)
            return;

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.isVisible()){
                line.mChartMappedPointsY = mapYPointsForChartView(line.Data.posY, mYMaxAnimator.mMinY, mYMaxAnimator.mMaxY);
            }
        }
    }


    void mapYPointsForScrollView() {
        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.isVisible()){
                line.mScrollMappedPointsY = mapYPointsForScrollView(line.Data.posY, mGlobalMinY, mGlobalMaxY);
                optimizeScrollPoints(line);
            }
        }
    }

    private void drawYLabels (long height, long yMax, long yMin, int alpha, Canvas canvas) {
        float xCoord;
        xCoord = chartAreaStartX;
        float spaceBetweenDividers = (float)yMax / height * chartAreaHeightPx / Y_DIVIDERS_COUNT;

        long step = yMin;
        float yLabelCoord = chartAreaEndY * 0.99f;

        label.setAlpha(alpha);
        label.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, label);
            yLabelCoord -= spaceBetweenDividers;
            step += (yMax - yMin) / Y_DIVIDERS_COUNT;
        }

    }

    private int getChartLineAlpha(int alpha) {
        int maxAlpha = 0;
        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.Alpha > maxAlpha)
                maxAlpha = line.Alpha;
        }
        return maxAlpha > alpha ? alpha : maxAlpha;
    }
}
