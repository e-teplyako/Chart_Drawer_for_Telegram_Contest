package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.android.telegramcontest.DateTimeUtils;

public class ChartView extends View {

    private final String LOG_TAG = ChartView.class.getSimpleName();

    private final int DIVIDERS_COUNT = 6;
    private final int DIVIDER_STROKE_WIDTH = 3;
    private final int X_LABELS_COUNT = 6;

    private float mDrawingAreaWidth;
    private float mSpaceBetweenDividers;
    private float mSpaceForBottomLabels;
    private float mDrawingAreaHeight;
    private float[] mDividerYCoords;

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mSpaceForBottomLabels = (float) (getHeight() * 0.1);
        mDrawingAreaWidth = getWidth();
        mDrawingAreaHeight = getHeight() - mSpaceForBottomLabels;
        mSpaceBetweenDividers = mDrawingAreaHeight / DIVIDERS_COUNT;
        mDividerYCoords = new float[DIVIDERS_COUNT];

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(DIVIDER_STROKE_WIDTH);

        float startX = 0f;
        float stopX = getWidth();
        float startY = mDrawingAreaHeight;
        float stopY = startY;

        for (int i = 0; i < DIVIDERS_COUNT; i++) {
            canvas.drawLine(startX, startY, stopX, stopY, paint);
            mDividerYCoords[i] = startY;
            startY -= mSpaceBetweenDividers;
            stopY = startY;
        }


        long[] x  = {1542412800000L,
                1542499200000L,
                1542585600000L,
                1542672000000L,
                1542758400000L,
                1542844800000L,
                1542931200000L,
                1543017600000L,
                1543104000000L,
                1543190400000L};
        long[][] y = {{37,
                20,
                32,
                39,
                32,
                35,
                19,
                65,
                36,
                62},
                {22,
                 12,
                 30,
                 40,
                 33,
                 23,
                 18,
                 41,
                 45,
                 69
                }};

        long[][] b = {{0, 4, 2, 5 ,1, 10 , 15, 3, 5, 13}, {12, 15, 9, 9, 9, 12, 15, 9, 9, 9}};
        drawChart(x, b, canvas);


    }

    //    Helper function for mapping points values
    private long nearestSixDivider(long num) {
        if (num % 6 == 0)
            return num;
        return (num + (6 - num % 6));
    }





    //    Helper function for mapping points values
    private long getMax(long[][] array) {
        long max = array[0][0];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] > max) {
                    max = array[i][j];
                }
            }
        }
        return max;
    }

    //    Helper function for mapping points values
    private long getMin(long[][] array) {
        long min = array[0][0];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] < min) {
                    min = array[i][j];
                }
            }
        }
        return min;
    }

    //    Helper function for mapping points values
    private long getMax(long[] array) {
        long max = array[0];

        for (int i = 0; i < array.length; i++)
                if (array[i] > max) {
                    max = array[i];
                }
        return max;
    }

    //    Helper function for mapping points values
    private long getMin(long[] array) {
        long min = array[0];

        for (int i = 0; i < array.length; i++) {
                if (array[i] < min) {
                    min = array[i];
                }
        }
        return min;
    }



    private void labelScales (long[] timestamps, long[][] pts, Canvas canvas) {
//        for ordinate axis
        long ordMin = getMin(pts);
        long ordMax = getMax(pts);
        long diffBetweeenOrdMinMax = ordMax - ordMin;
        diffBetweeenOrdMinMax = nearestSixDivider(diffBetweeenOrdMinMax);

        long ordStep = diffBetweeenOrdMinMax / DIVIDERS_COUNT;

        float ordXCoord = 0f;

        long ordLabel = ordMin;

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setTextSize(40);

        for (int i = 0; i < mDividerYCoords.length; i++) {
            canvas.drawText(String.valueOf(ordLabel), ordXCoord, mDividerYCoords[i] - 4f, paint);
            ordLabel += ordStep;
        }

//        for abs axis
        long absMin = getMin(timestamps);
        long absMax = getMax(timestamps);
        long diffBetweenAbsMinMax = absMax - absMin;
        diffBetweenAbsMinMax = nearestSixDivider(diffBetweenAbsMinMax);

        long absStep = diffBetweenAbsMinMax / X_LABELS_COUNT;

        float absYCoord = mDrawingAreaHeight +  0.5f * mSpaceForBottomLabels;
        float absXCoord = 0f;

        long absLabel = absMin;

        for (int i = 0; i < X_LABELS_COUNT; i++) {
            canvas.drawText(DateTimeUtils.formatDate(absLabel), absXCoord, absYCoord, paint);
            absLabel += absStep;
            absXCoord = mapXPoint(absLabel, absMin, absMax);
        }
    }

    private float[] mapXPoints (long[] xPts, long min, long max) {
        long calculatedArea = nearestSixDivider(max - min);
        float[] mapped = new float[xPts.length];
        for (int i = 0; i < xPts.length; i++) {
            float percentage = (float)(xPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaWidth * percentage;
        }
        return mapped;
    }

    private float[] mapYPoints (long[] yPts, long min, long max) {
        long calculatedArea = nearestSixDivider(max - min);
        float[] mapped = new float[yPts.length];
        for (int i = 0; i < yPts.length; i++) {
            float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaHeight * percentage;
            mapped[i] = mDrawingAreaHeight - mapped[i];
        }
        return mapped;
    }

    private float mapXPoint (long point, long min, long max) {
        long calculatedArea = nearestSixDivider(max - min);
        float percentage = ((float) (point - min)) / (float) calculatedArea;
        float mapped = mDrawingAreaWidth * percentage;
        return mapped;
    }


    public void drawChart (long[] xPts, long[][] yPts, Canvas canvas) {
        labelScales(xPts, yPts, canvas);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);

        long maxX = getMax(xPts);
        long minX = getMin(xPts);
        long maxY = getMax(yPts);
        long minY = getMin(yPts);

        float[] mappedX = mapXPoints(xPts, minX, maxX);

        for (int i = 0; i < yPts.length; i++) {
            float[] mappedY = mapYPoints(yPts[i], minY, maxY);
            for (int j = 0; j < mappedY.length - 1; j++){
                canvas.drawLine(mappedX[j], mappedY[j], mappedX[j+1], mappedY[j+1], paint);
            }
        }


    }



}
