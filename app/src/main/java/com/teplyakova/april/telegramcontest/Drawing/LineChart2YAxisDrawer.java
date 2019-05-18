package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class LineChart2YAxisDrawer extends BaseLineChartDrawer {

    class YScale extends BaseLineChartDrawer.YScale {
    }

    class ChartLine extends BaseLineChartDrawer.ChartLine {
        YMaxAnimator mYMaxAnimator;
    }

    class YMaxAnimator extends BaseLineChartDrawer.YMaxAnimator{
        ChartLine mLine;
        boolean mLeft;

        YMaxAnimator(ChartLine line, boolean left) {
            mLine = line;
            mLeft = left;
        }

        void updateMinMaxY() {

            if (!mBordersSet || !mLine.isVisible())
                return;

            long newYMax = MathUtils.getMax(mLine.Data.posY, mPointsMinIndex, mPointsMaxIndex);
            long newYMin = MathUtils.getMin(mLine.Data.posY, mPointsMinIndex, mPointsMaxIndex);
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

    private Paint mAxisLabelPaint;

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

    @Override
    public void setLines(LineData[] lines) {
        super.setLines(lines);

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            LineChart2YAxisDrawer.ChartLine derivedLine = (LineChart2YAxisDrawer.ChartLine) line;
            derivedLine.mYMaxAnimator.updateMinMaxY();
        }
    }

    @Override
    public boolean setBorders(float normPosX1, float normPosX2) {
        boolean result =  super.setBorders(normPosX1, normPosX2);

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            LineChart2YAxisDrawer.ChartLine derivedLine = (LineChart2YAxisDrawer.ChartLine) line;
            derivedLine.mYMaxAnimator.updateMinMaxY();
        }

        return result;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBordersSet) {
            drawScaleX(mChartMappedPointsX, canvas);
            drawTopDatesText(canvas);
        }

        if (!showChartLines() || !mBordersSet) {
            drawScaleY(100, 100, 255, canvas);
            drawRects(canvas);
            return;
        }

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            LineChart2YAxisDrawer.ChartLine derivedLine = (LineChart2YAxisDrawer.ChartLine) line;
            for (BaseLineChartDrawer.YScale yScale : derivedLine.mYMaxAnimator.mYScales) {
                drawScaleY(yScale.Height, yScale.MaxY, yScale.Alpha, canvas);
            }
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

        for (BaseLineChartDrawer.ChartLine line : mLines) {
            LineChart2YAxisDrawer.ChartLine derivedLine = (LineChart2YAxisDrawer.ChartLine) line;
            if (line.isVisible()) {
                for (BaseLineChartDrawer.YScale yScale : derivedLine.mYMaxAnimator.mYScales) {
                    drawYLabels(yScale.Height, yScale.MaxY, yScale.MinY, getChartLineAlpha(yScale.Alpha, line), derivedLine.mYMaxAnimator.mLeft, line.Data.color, canvas);
                }
            }
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
            LineChart2YAxisDrawer.ChartLine derivedLine = (LineChart2YAxisDrawer.ChartLine) line;
            if (line.isVisible()){
                line.mChartMappedPointsY = mapYPointsForChartView(line.Data.posY, derivedLine.mYMaxAnimator.mMinY, derivedLine.mYMaxAnimator.mMaxY);
            }
        }
    }

    @Override
    protected void setUpPaints() {
        super.setUpPaints();
        mAxisLabelPaint = new Paint();
        mAxisLabelPaint.setTextSize(mTextSizeMediumPx);
        mAxisLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
        mAxisLabelPaint.setAntiAlias(true);
    }

    private void drawYLabels (long height, long yMax, long yMin, int alpha, boolean left, int color, Canvas canvas) {
        float xCoord;
        if (left) {
            mAxisLabelPaint.setTextAlign(Paint.Align.LEFT);
            xCoord = mChartDrawingAreaStartX;
        }
        else {
            mAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);
            xCoord = mChartDrawingAreaEndX;
        }

        float spaceBetweenDividers = (float)yMax / height * mChartDrawingAreaHeight / Y_DIVIDERS_COUNT;

        long step = yMin;
        float yLabelCoord = mChartDrawingAreaEndY * 0.99f;

        mAxisLabelPaint.setColor(color);
        mAxisLabelPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mAxisLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += (yMax - yMin) / Y_DIVIDERS_COUNT;
        }
    }

    private int getChartLineAlpha(int alpha, BaseLineChartDrawer.ChartLine line) {
        return line.Alpha > alpha ? alpha : line.Alpha;
    }
}

