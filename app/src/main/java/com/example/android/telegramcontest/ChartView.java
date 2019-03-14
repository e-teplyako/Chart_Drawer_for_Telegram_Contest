package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class ChartView extends View {

    private final String LOG_TAG = ChartView.class.getSimpleName();

    private final int DIVIDERS_COUNT = 6;
    private final int DIVIDER_STROKE_WIDTH = 2;
    private final int LABELS_COLOR = Color.parseColor("#9E9E9E");
    private final int DIVIDER_COLOR = Color.parseColor("#E0E0E0");
    private final int CHART_STROKE_WIDTH = 6;

    private float mDrawingAreaWidth;
    private float mDrawingAreaWidthStart;
    private float mDrawingAreaWidthEnd;
    private float mSpaceBetweenDividers;
    private float mSpaceForBottomLabels;
    private float mDrawingAreaHeight;
    private float[] mDividerYCoords;
    private Paint mDividerPaint;

    private long[] mXPoints;
    private long[][] mYPoints;
    private String[] mColors;
    private Paint mChartPaint;


    private boolean mPointIsChosen = false;
    private float mXCoordinateOfChosenPoint;
    private int mPositionOfChosenPoint;
    private Paint mCirclePaint;
    private Paint mPlatePaint;
    private TextPaint mBaseLabelPaint;
    private TextPaint mPlateLabelPaint;

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    private void init() {
        mDividerPaint = new Paint();
        mDividerPaint.setColor(DIVIDER_COLOR);
        mDividerPaint.setStrokeWidth(DIVIDER_STROKE_WIDTH);

        mChartPaint = new Paint();
        mChartPaint.setStrokeWidth(CHART_STROKE_WIDTH);

        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(CHART_STROKE_WIDTH);

        mPlatePaint = new Paint();

        mBaseLabelPaint = new TextPaint();
        mBaseLabelPaint.setColor(LABELS_COLOR);
        mBaseLabelPaint.setTextSize(40);

        mPlateLabelPaint = new TextPaint();
        mPlateLabelPaint.setColor(Color.BLACK);
        mPlateLabelPaint.setTextSize(40);
        mPlateLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

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

        Log.e(LOG_TAG, "OnDraw() called");

        mSpaceForBottomLabels = getHeight() * 0.15f;
        mDrawingAreaWidth = getWidth() * 0.92f;
        mDrawingAreaWidthStart = getWidth() * 0.04f;
        mDrawingAreaWidthEnd = getWidth() - mDrawingAreaWidthStart;
        mDrawingAreaHeight = getHeight() - mSpaceForBottomLabels;
        mSpaceBetweenDividers = mDrawingAreaHeight / DIVIDERS_COUNT;
        mDividerYCoords = new float[DIVIDERS_COUNT];


        float startX = mDrawingAreaWidthStart;
        float stopX = mDrawingAreaWidthEnd;
        float startY = mDrawingAreaHeight;
        float stopY = startY;

        for (int i = 0; i < DIVIDERS_COUNT; i++) {
            canvas.drawLine(startX, startY, stopX, stopY, mDividerPaint);
            mDividerYCoords[i] = startY;
            startY -= mSpaceBetweenDividers;
            stopY = startY;
        }

        if (mXPoints != null && mYPoints != null) {
            drawChart(canvas);
        }

        if (mPointIsChosen) {
            canvas.drawLine(mXCoordinateOfChosenPoint, 0f, mXCoordinateOfChosenPoint, mDrawingAreaHeight, mDividerPaint);
            drawChosenPointCircle(canvas);
            drawPlate(canvas);
        }
    }

    private void drawPlate(Canvas canvas) {
        float top = mDrawingAreaHeight * 0.1f;
        float bottom = top * 3f;
        float width = (bottom - top) * 1.8f;
        float left;
        float right;
        float offset = getWidth() * 0.05f;
        if ((mXCoordinateOfChosenPoint + offset + width) >= mDrawingAreaWidthEnd) {
            right = mXCoordinateOfChosenPoint - offset;
            left = right - width;
        }
        else {
            left = mXCoordinateOfChosenPoint + offset;
            right = left + width;
        }
        RectF rectF = new RectF(left,
                top,
                right,
                bottom
                );
        int cornerRadius = 25;

        mPlatePaint.setColor(DIVIDER_COLOR);
        mPlatePaint.setStrokeWidth(DIVIDER_STROKE_WIDTH);
        mPlatePaint.setStyle(Paint.Style.STROKE);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        mPlatePaint.setStyle(Paint.Style.FILL);
        mPlatePaint.setColor(Color.WHITE);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        String date = DateTimeUtils.formatDateEEEMMMd(mXPoints[mPositionOfChosenPoint]);
        canvas.drawText(date, left + width * 0.1f, top + width * 0.15f, mPlateLabelPaint);

    }

    private void drawChosenPointCircle(Canvas canvas) {
        for (int i = 0; i < mYPoints.length; i++){
            mCirclePaint.setColor(Color.parseColor(mColors[i]));
            float yCoordinateOfChosenPoint = mapYPoints(mYPoints[i], getMin(mYPoints), getMax(mYPoints))[mPositionOfChosenPoint];
            canvas.drawCircle(mXCoordinateOfChosenPoint, yCoordinateOfChosenPoint, 16f, mCirclePaint);
            mCirclePaint.setColor(Color.WHITE);
            canvas.drawCircle(mXCoordinateOfChosenPoint, yCoordinateOfChosenPoint, 8f, mCirclePaint);
        }
    }

    //    Helper function for mapping points values
    private long nearestSixDivider(long num) {
        if (num % 6 == 0)
            return (num + 6);
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



    private void labelScales (Canvas canvas) {
        labelAxisY(canvas);
        labelAxisX(canvas);
    }

    private void labelAxisY(Canvas canvas) {
        long ordMin = getMin(mYPoints);
        long ordMax = getMax(mYPoints);
        long diffBetweenOrdMinMax = ordMax - ordMin;
        diffBetweenOrdMinMax = nearestSixDivider(diffBetweenOrdMinMax);

        long ordStep = diffBetweenOrdMinMax / DIVIDERS_COUNT;

        float ordXCoord = mDrawingAreaWidthStart;

        long ordLabel = ordMin;

        for (int i = 0; i < mDividerYCoords.length; i++) {
            canvas.drawText(String.valueOf(ordLabel), ordXCoord, mDividerYCoords[i] - 4f, mBaseLabelPaint);
            ordLabel += ordStep;
        }
    }

    private void labelAxisX(Canvas canvas) {
        long min = getMin(mXPoints);
        long max = getMax(mXPoints);
        long diffBetweenAbsMinMax = max - min;
        long days = TimeUnit.MILLISECONDS.toDays(diffBetweenAbsMinMax);


        float yCoord = mDrawingAreaHeight +  0.5f * mSpaceForBottomLabels;
        float xCoord = mDrawingAreaWidthStart;
        canvas.drawText(DateTimeUtils.formatDateMMMd(mXPoints[0]), xCoord, yCoord, mBaseLabelPaint);
        mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
        xCoord = mDrawingAreaWidthEnd;
        canvas.drawText(DateTimeUtils.formatDateMMMd(mXPoints[mXPoints.length - 1]), xCoord, yCoord, mBaseLabelPaint);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
        Log.e(LOG_TAG, String.valueOf(days));
        if ((days % 2) == 0) {
            long middle = days / 2;
            long middleInMillis = TimeUnit.DAYS.toMillis(middle);
            xCoord = mapXPoint(min + middleInMillis);
            mBaseLabelPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(DateTimeUtils.formatDateMMMd(min + middleInMillis), xCoord, yCoord, mBaseLabelPaint);
            if (middle % 2 == 0) {
                long middleSide = middle / 2;
                long middleSideInMillis = TimeUnit.DAYS.toMillis(middleSide);
                float leftXCoord = mapXPoint(min + middleSideInMillis);
                float rightXCoord = mapXPoint(max - middleSideInMillis);
                canvas.drawText(DateTimeUtils.formatDateMMMd(min + middleSideInMillis), leftXCoord, yCoord, mBaseLabelPaint);
                canvas.drawText(DateTimeUtils.formatDateMMMd(max - middleSideInMillis), rightXCoord, yCoord, mBaseLabelPaint);

            }
        }
        else {

        }

        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
    }

    private float[] mapXPoints (long[] xPts, long min, long max) {
        long calculatedArea = nearestSixDivider(max - min);
        float[] mapped = new float[xPts.length];
        for (int i = 0; i < xPts.length; i++) {
            float percentage = (float)(xPts[i] - min) / (float) calculatedArea;
            mapped[i] = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
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

    private float mapXPoint (long point) {
        long calculatedArea = nearestSixDivider(getMax(mXPoints) - getMin(mXPoints));
        float percentage = ((float) (point - getMin(mXPoints))) / (float) calculatedArea;
        float mapped = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
        return mapped;
    }

    private int mapCoordinateToPoint (float xCoord) {
        float calculatedArea = (float) nearestSixDivider(getMax(mXPoints) - getMin(mXPoints));
        float point = ((xCoord - mDrawingAreaWidthStart) * calculatedArea) / mDrawingAreaWidth + getMin(mXPoints);

        int position = 0;
        long closestToPoint = mXPoints[position];
        for (int i = 0; i < mXPoints.length; i ++) {
            if (Math.abs(mXPoints[i] - point) < Math.abs(closestToPoint - point)) {
                position = i;
                closestToPoint = mXPoints[i];
            }
        }
        return position;
    }


    private void drawChart (Canvas canvas) {
        if (mXPoints == null || mYPoints == null) return;
        labelScales(canvas);

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                if (y >= mDrawingAreaHeight) {
                    hideVerticalDivider();
                }
                else {
                    showPointDetails(x);
                }
                break;
            case MotionEvent.ACTION_UP:
                return false;
        }
        return true;
    }

    private void showPointDetails(float xCoord) {
        int pointPosition = mapCoordinateToPoint(xCoord);
        float pointCoordinate = mapXPoint(mXPoints[pointPosition]);
        showVerticalDivider(pointCoordinate, pointPosition);
    }


    private void showVerticalDivider(float xCoord, int position) {
        mPointIsChosen = true;
        mXCoordinateOfChosenPoint = xCoord;
        mPositionOfChosenPoint = position;
        invalidate();
    }

    private void hideVerticalDivider() {
        mPointIsChosen = false;
        invalidate();
    }


}
