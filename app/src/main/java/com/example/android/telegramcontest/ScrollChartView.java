package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class ScrollChartView extends View {

    private final String LOG_TAG = ScrollView.class.getSimpleName();

    private Paint mChartPaint;
    private final int CHART_STROKE_WIDTH = 4;

    private float mDrawingAreaWidth;
    private float mDrawingAreaHeight;

    private long[] mXPoints;
    private long[][] mYPoints;
    private String[] mColors;

    public ScrollChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    private void init(){
        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(CHART_STROKE_WIDTH);
    }

    public void setChartParams(long[] xPts, long[][] yPts, String[] colors) {
        mXPoints = xPts;
        mYPoints = yPts;
        mColors = colors;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawingAreaWidth = getWidth();
        mDrawingAreaHeight = getHeight();

        if (mXPoints != null && mYPoints != null) {
            drawChart(canvas);
        }

    }

    private void drawChart (Canvas canvas) {
        if (mXPoints == null || mYPoints == null) return;

        mChartPaint.setColor(Color.RED);

        long maxX = getMax(mXPoints);
        long minX = getMin(mXPoints);
        long maxY = getMax(mYPoints);
        long minY = getMin(mYPoints);

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

    private float[] mapYPoints (long[] yPts, long min, long max) {
        long calculatedArea = max - min;
        float[] mapped = new float[yPts.length];
        for (int i = 0; i < yPts.length; i++) {
            float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaHeight * percentage;
            mapped[i] = mDrawingAreaHeight - mapped[i];
        }
        return mapped;
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


}
