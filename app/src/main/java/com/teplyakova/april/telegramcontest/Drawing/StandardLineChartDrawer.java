package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class StandardLineChartDrawer extends BaseLineChartDrawer {

    public class YScale extends BaseLineChartDrawer.YScale {
    }

    public class ChartLine extends BaseLineChartDrawer.ChartLine {
    }

    public class YMaxAnimator extends BaseLineChartDrawer.YMaxAnimator {

        YMaxAnimator(ChartLine line, boolean left) {
            super(line, left);
        }

        public void updateMaxY() {
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
    }

    public StandardLineChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        for (LineData lineData : chartData.lines)
        {
            ChartLine chartLine = new ChartLine();
            chartLine.Data      = lineData;
            chartLine.Alpha     = 255;
            chartLine.AlphaEnd  = 255;
            chartLine.mYMaxAnimator = new YMaxAnimator(chartLine, true);
            mLines.add(chartLine);
        }
    }

    public void draw(Canvas canvas) {
        if (mBordersSet)
            drawScaleX(mMappedPointsX, canvas);

        if (!showChartLines() || !mBordersSet)
        {
            drawScaleY(100, 100, 255, canvas);
            return;
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            for (BaseLineChartDrawer.YScale yScale : line.mYMaxAnimator.mYScales) {
                drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
            }
        }

        if (mPointIsChosen) {
            mPositionOfChosenPoint = mapCoordinateToPoint(mMappedPointsX, mXCoordinateOfTouch);
            drawVerticalDivider(mMappedPointsX, canvas);
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.IsVisible())
                drawChartLine(line, canvas);
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            if (line.IsVisible()) {
                for (BaseLineChartDrawer.YScale yScale : line.mYMaxAnimator.mYScales) {
                    drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, line.mYMaxAnimator.mLeft, canvas);
                }
            }
        }

        if (mPointIsChosen) {
            drawChosenPointPlate(mMappedPointsX, canvas);
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
}
