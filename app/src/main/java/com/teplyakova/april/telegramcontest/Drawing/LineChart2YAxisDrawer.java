package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class LineChart2YAxisDrawer extends BaseLineChartDrawer {

    public class YScale extends BaseLineChartDrawer.YScale {
    }

    public class ChartLine extends BaseLineChartDrawer.ChartLine {
    }

    public class YMaxAnimator extends BaseLineChartDrawer.YMaxAnimator{

        YMaxAnimator(ChartLine line, boolean left) {
            super(line, left);
        }

        public void updateMinMaxY() {

            if (!mBordersSet || !mLine.IsVisible())
                return;

            LineData[] lines = {mLine.Data};
            long newYMax = MathUtils.getMaxY(lines, mPointsMinIndex, mPointsMaxIndex);
            long newYMin = MathUtils.getMinY(lines, mPointsMinIndex, mPointsMaxIndex);
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

    public LineChart2YAxisDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        boolean left = true;
        for (LineData lineData : chartData.lines)
        {
            ChartLine chartLine = new ChartLine();
            chartLine.Data      = lineData;
            chartLine.Alpha     = 255;
            chartLine.AlphaEnd  = 255;
            chartLine.mYMaxAnimator = new YMaxAnimator(chartLine, left);
            mLines.add(chartLine);
            left = !left;
        }
    }

    protected void mapYPointsForChartView()
    {
        if (!mBordersSet || !showChartLines())
            return;

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.IsVisible()){
                line.mChartMappedPointsY = mapYPointsForChartView(line.Data.posY, line.mYMaxAnimator.mMinY, line.mYMaxAnimator.mMaxY);
            }
        }
    }

    @Override
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

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            for (BaseLineChartDrawer.YScale yScale : line.mYMaxAnimator.mYScales) {
                drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
            }
        }

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mChartMappedPointsX, mXCoordinateOfTouch);
            drawVerticalDivider(mChartMappedPointsX, canvas);
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.IsVisible()) {
                mChartPaint.setStrokeWidth(6);
                drawChartLineInChartView(line, canvas, mChartMappedPointsX, line.mChartMappedPointsY);
                mChartPaint.setStrokeWidth(4);
                drawChartLineInScrollView(line, canvas, line.mScrollOptimizedPointsX, line.mScrollOptimizedPointsY);
            }
        }

        if (mPointIsChosen)
            for (BaseLineChartDrawer.ChartLine line : mLines) {
                if (line.IsVisible())
                    drawChosenPointCircle(mChartMappedPointsX, line.mChartMappedPointsY, line.Data.color, canvas);
            }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.IsVisible()) {
                for (BaseLineChartDrawer.YScale yScale : line.mYMaxAnimator.mYScales) {
                    drawYLabels(yScale.Height, yScale.MaxY, yScale.MinY, yScale.Alpha, line.mYMaxAnimator.mLeft, line.Data.color, canvas);
                }
            }
        }

        if (mPointIsChosen) {
            drawChosenPointPlate(mChartMappedPointsX, canvas);
        }

        drawRects(canvas);
    }

    private void drawYLabels (long height, long yMax, long yMin, int alpha, boolean left, int color, Canvas canvas) {
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

        long step = yMin;
        float yLabelCoord = mChartDrawingAreaEndY * 0.99f;

        mBaseLabelPaint.setColor(color);
        mBaseLabelPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += (yMax - yMin) / Y_DIVIDERS_COUNT;
        }

    }

}

