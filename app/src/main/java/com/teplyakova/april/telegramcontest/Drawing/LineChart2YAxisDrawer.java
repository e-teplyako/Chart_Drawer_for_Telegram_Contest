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

public class LineChart2YAxisDrawer extends BaseLineChartDrawer {

    public class YScale extends BaseLineChartDrawer.YScale {
    }

    public class ChartLine extends BaseLineChartDrawer.ChartLine {
    }

    public class YMaxAnimator extends BaseLineChartDrawer.YMaxAnimator{

        YMaxAnimator(ChartLine line, boolean left) {
            super(line, left);
        }

        public void updateMaxY() {

            if (!mBordersSet || !mLine.IsVisible())
                return;

            LineData[] lines = {mLine.Data};
            long newYMax = MathUtils.getMaxY(lines, mPointsMinIndex, mPointsMaxIndex);
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
                    drawYLabels(yScale.Height, yScale.MaxY, yScale.Alpha, line.mYMaxAnimator.mLeft, line.Data.color, canvas);
                }
            }
        }

        if (mPointIsChosen) {
            drawChosenPointPlate(mMappedPointsX, canvas);
        }
    }

    private void drawYLabels (long height, long yMax, int alpha, boolean left, int color, Canvas canvas) {
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

        mBaseLabelPaint.setColor(color);
        mBaseLabelPaint.setAlpha(alpha);

        for (int i = 0; i < Y_DIVIDERS_COUNT; i++) {
            canvas.drawText(MathUtils.getFriendlyNumber(step), xCoord, yLabelCoord, mBaseLabelPaint);
            yLabelCoord -= spaceBetweenDividers;
            step += yMax / Y_DIVIDERS_COUNT;
        }

    }

}

