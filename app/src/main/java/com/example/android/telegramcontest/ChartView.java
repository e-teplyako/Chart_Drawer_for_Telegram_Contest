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
import android.view.MotionEvent;
import android.view.View;

import com.example.android.telegramcontest.Utils.DateTimeUtils;
import com.example.android.telegramcontest.Utils.MathUtils;

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

    private Chart mFullChart;
    private long[] mXPointsPart;
    private int[][] mYPointsPart;
    private String[] mColorsPart;
    private String[] mChartNamesPart;
    private int[] mIncludedYLinesIndexes;
    private int mAdditionalPointLeft;
    private int mAdditionalPointRight;


    private Paint mChartPaint;


    private boolean mPointIsChosen = false;
    private float mXCoordinateOfChosenPoint;
    private int mPositionOfChosenPoint;
    private Paint mCirclePaint;
    private Paint mPlatePaint;
    private TextPaint mBaseLabelPaint;
    private TextPaint mPlateXValuePaint;
    private TextPaint mPlateYValuePaint;
    private TextPaint mPlateNamePaint;

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
        mBaseLabelPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));

        mPlateXValuePaint = new TextPaint();
        mPlateXValuePaint.setColor(Color.BLACK);
        mPlateXValuePaint.setTextAlign(Paint.Align.CENTER);
        mPlateXValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateYValuePaint = new TextPaint();
        mPlateYValuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));

        mPlateNamePaint = new TextPaint();
        mPlateNamePaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));


    }

    public void setChartParams(Chart chart) {
        mFullChart = chart;
        mXPointsPart = mFullChart.getXPoints();
        mYPointsPart = mFullChart.getYPointsAsArray();
        mColorsPart = mFullChart.getColors();
        mChartNamesPart = mFullChart.getNames();
        mAdditionalPointRight = -1;
        mAdditionalPointLeft = -1;
        invalidate();
    }

    public void setChartParams(Chart chart, float start, float percentage, @Nullable int[] indexesOfLinesToInclude) {
        mFullChart = chart;
        setPartialChartParams(start, percentage, indexesOfLinesToInclude);
        invalidate();
    }


    private void setPartialChartParams (float start, float percentage, @Nullable int[] indexesOfLinesToInclude) {
        long[] xPointsFull = mFullChart.getXPoints();
        int[][] yPointsFull = mFullChart.getYPointsAsArray();
        if ( xPointsFull == null)
            return;
        mIncludedYLinesIndexes = indexesOfLinesToInclude;
        if (mIncludedYLinesIndexes == null)
            return;
        int startPosition = (int) Math.floor(xPointsFull.length * start);
        if (startPosition < 0) startPosition = 0;
        if (startPosition != 0) mAdditionalPointLeft = startPosition - 1;
        else mAdditionalPointLeft = -1;
        //Log.e(LOG_TAG, "Start position: " + String.valueOf(startPosition));
        int amountOfPoints = (int) Math.ceil(xPointsFull.length * percentage);
        //Log.e(LOG_TAG, "Amount of points: " + String.valueOf(amountOfPoints));
        if (startPosition + amountOfPoints > xPointsFull.length) amountOfPoints = xPointsFull.length - startPosition;
        if (startPosition + amountOfPoints != xPointsFull.length) mAdditionalPointRight = startPosition + amountOfPoints;
        else mAdditionalPointRight = -1;
        int arrayLength = 0;
        int first = 0;
        int last = 0;
        if (mAdditionalPointLeft == -1 && mAdditionalPointRight == -1){
            //Log.e(LOG_TAG, "NO additional points");
            arrayLength = amountOfPoints;
            first = startPosition;
            last = startPosition + amountOfPoints - 1;
        }
        else if (mAdditionalPointLeft != -1 && mAdditionalPointRight == -1) {
            //Log.e(LOG_TAG, "1 additional point left");
            //Log.e(LOG_TAG, "Left: " + String.valueOf(DateTimeUtils.formatDateMMMd(mXPointsFull[mAdditionalPointLeft])));
            arrayLength = amountOfPoints;
            first = startPosition;
            last = startPosition + amountOfPoints - 1;
        }
        else if (mAdditionalPointLeft == -1 && mAdditionalPointRight != -1) {
            //Log.e(LOG_TAG, "1 additional point right");
            //Log.e(LOG_TAG, "Right: " + String.valueOf(DateTimeUtils.formatDateMMMd(mXPointsFull[mAdditionalPointRight])));
            arrayLength = amountOfPoints;
            first = startPosition;
            last = startPosition + amountOfPoints - 1;
        }
        else {
            //Log.e(LOG_TAG, "2 additional points");
            //Log.e(LOG_TAG, "Right: " + String.valueOf(DateTimeUtils.formatDateMMMd(mXPointsFull[mAdditionalPointRight])));
            //Log.e(LOG_TAG, "Left: " + String.valueOf(DateTimeUtils.formatDateMMMd(mXPointsFull[mAdditionalPointLeft])));
            arrayLength = amountOfPoints;
            first = startPosition;
            last = startPosition + amountOfPoints - 1;
        }
            mXPointsPart = new long[arrayLength];
            mColorsPart = new String[mIncludedYLinesIndexes.length];
            mChartNamesPart = new String[mIncludedYLinesIndexes.length];
            mYPointsPart = new int[mIncludedYLinesIndexes.length][arrayLength];
            for (int i = first, j = 0; i <= last; i++, j++) {
                mXPointsPart[j] = xPointsFull[i];
                for (int n = 0; n < mYPointsPart.length; n++) {
                    mYPointsPart[n][j] = yPointsFull[mIncludedYLinesIndexes[n]][i];
                    mColorsPart[n] = mFullChart.getColors()[mIncludedYLinesIndexes[n]];
                    mChartNamesPart[n] = mFullChart.getNames()[mIncludedYLinesIndexes[n]];
                }
            }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mSpaceForBottomLabels = getHeight() * 0.15f;
        mDrawingAreaWidth = getWidth() * 0.92f;
        mDrawingAreaWidthStart = getWidth() * 0.04f;
        mDrawingAreaWidthEnd = getWidth() - mDrawingAreaWidthStart;
        mDrawingAreaHeight = getHeight() - mSpaceForBottomLabels;
        mSpaceBetweenDividers = mDrawingAreaHeight / DIVIDERS_COUNT;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mDividerYCoords = new float[DIVIDERS_COUNT];


        float startX = mDrawingAreaWidthStart;
        float stopX = mDrawingAreaWidthEnd;
        float startY = mDrawingAreaHeight;
        float stopY = startY;

        mBaseLabelPaint.setTextSize(40);

        for (int i = 0; i < DIVIDERS_COUNT; i++) {
            canvas.drawLine(startX, startY, stopX, stopY, mDividerPaint);
            mDividerYCoords[i] = startY;
            startY -= mSpaceBetweenDividers;
            stopY = startY;
        }

        if (mIncludedYLinesIndexes != null && mIncludedYLinesIndexes.length != 0) {
            drawChart(canvas);
        }

        if (mPointIsChosen) {
            canvas.drawLine(mXCoordinateOfChosenPoint, 0f, mXCoordinateOfChosenPoint, mDrawingAreaHeight, mDividerPaint);
            drawChosenPointCircle(canvas);
            drawPlate(canvas);
            mPointIsChosen = false;
        }
    }

    private void drawPlate(Canvas canvas) {
        float top = mDrawingAreaHeight * 0.05f + mDrawingAreaWidth * 0.05f;
        float bottom = top * 3.5f;
        float height = bottom - top;
        float width = height * 2f;
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

        mPlateXValuePaint.setTextSize(height * 0.2f);
        String date = DateTimeUtils.formatDateEEEMMMd(mXPointsPart[mPositionOfChosenPoint]);
        canvas.drawText(date, (left + right) / 2, top + width * 0.15f, mPlateXValuePaint);

        switch (mYPointsPart.length) {
            case 1:
                mPlateYValuePaint.setTextSize(height * 0.25f);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateYValuePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(mYPointsPart[0][mPositionOfChosenPoint]), (left + right) / 2, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateNamePaint.setTextSize(height * 0.2f);
                mPlateNamePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mChartNamesPart[0], (left + right) / 2, top + height * 0.8f, mPlateNamePaint);
                break;
            case 2:
                mPlateYValuePaint.setTextSize(height * 0.2f);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateYValuePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(String.valueOf(mYPointsPart[0][mPositionOfChosenPoint]), left + width * 0.05f, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateNamePaint.setTextSize(height * 0.15f);
                mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(mChartNamesPart[0], left + width * 0.05f, top + height * 0.8f, mPlateNamePaint);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[1]));
                mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(mYPointsPart[1][mPositionOfChosenPoint]), right - width * 0.05f, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[1]));
                mPlateNamePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(mChartNamesPart[1], right - width * 0.05f, top + height * 0.8f, mPlateNamePaint);
                break;
            case 3:
                mPlateYValuePaint.setTextSize(height * 0.15f);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateYValuePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(String.valueOf(mYPointsPart[0][mPositionOfChosenPoint]), left + width * 0.05f, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[0]));
                mPlateNamePaint.setTextSize(height * 0.1f);
                mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(mChartNamesPart[0], left + width * 0.05f, top + height * 0.8f, mPlateNamePaint);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[2]));
                mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(mYPointsPart[2][mPositionOfChosenPoint]), right - width * 0.05f, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[2]));
                mPlateNamePaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(mChartNamesPart[2], right - width * 0.05f, top + height * 0.8f, mPlateNamePaint);
                mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[1]));
                mPlateYValuePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(mYPointsPart[1][mPositionOfChosenPoint]), (left + right) / 2, top + height * 0.6f, mPlateYValuePaint);
                mPlateNamePaint.setColor(Color.parseColor(mColorsPart[1]));
                mPlateNamePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(mChartNamesPart[1], (left + right) / 2, top + height * 0.8f, mPlateNamePaint);
                break;
            default:
                mPlateYValuePaint.setTextSize(height * 0.1f);
                mPlateNamePaint.setTextSize(height * 0.1f);
                mPlateYValuePaint.setTextAlign(Paint.Align.LEFT);
                mPlateNamePaint.setTextAlign(Paint.Align.RIGHT);
                float heightOffset = 0.6f;
                for (int i = 0; i < mYPointsPart.length; i++){
                    mPlateYValuePaint.setColor(Color.parseColor(mColorsPart[i]));
                    mPlateNamePaint.setColor(Color.parseColor(mColorsPart[i]));
                    canvas.drawText(String.valueOf(mYPointsPart[i][mPositionOfChosenPoint]), left + width * 0.05f, top + height * heightOffset, mPlateYValuePaint);
                    canvas.drawText(mChartNamesPart[i], right - width * 0.05f, top + height * heightOffset, mPlateNamePaint);
                    heightOffset += 0.1f;
                }
                break;

        }


    }

    private void drawChosenPointCircle(Canvas canvas) {
        for (int i = 0; i < mYPointsPart.length; i++){
            mCirclePaint.setColor(Color.parseColor(mColorsPart[i]));
            float yCoordinateOfChosenPoint = mapYPoint(i, mPositionOfChosenPoint, MathUtils.getMin(mYPointsPart), MathUtils.getMax(mYPointsPart));
            canvas.drawCircle(mXCoordinateOfChosenPoint, yCoordinateOfChosenPoint, 16f, mCirclePaint);
            mCirclePaint.setColor(Color.WHITE);
            canvas.drawCircle(mXCoordinateOfChosenPoint, yCoordinateOfChosenPoint, 8f, mCirclePaint);
        }
    }


    private void labelScales (Canvas canvas) {
        labelAxisY(canvas);
        labelAxisX(canvas);
    }

    private void labelAxisY(Canvas canvas) {
        long ordMin = MathUtils.getMin(mYPointsPart);
        long ordMax = MathUtils.getMax(mYPointsPart);
        long diffBetweenOrdMinMax = ordMax - ordMin;
        diffBetweenOrdMinMax = MathUtils.nearestSixDivider(diffBetweenOrdMinMax);

        long ordStep = diffBetweenOrdMinMax / DIVIDERS_COUNT;

        float ordXCoord = mDrawingAreaWidthStart;

        long ordLabel = ordMin;

        for (int i = 0; i < mDividerYCoords.length; i++) {
            canvas.drawText(String.valueOf(ordLabel), ordXCoord, mDividerYCoords[i] - 4f, mBaseLabelPaint);
            ordLabel += ordStep;
        }
    }

    private void labelAxisX(Canvas canvas) {
        long min = MathUtils.getMin(mXPointsPart);
        long max = MathUtils.getMax(mXPointsPart);
        long diffBetweenAbsMinMax = max - min;
        long days = TimeUnit.MILLISECONDS.toDays(diffBetweenAbsMinMax);


        float yCoord = mDrawingAreaHeight +  0.5f * mSpaceForBottomLabels;
        float xCoord = mDrawingAreaWidthStart;
        canvas.drawText(DateTimeUtils.formatDateMMMd(mXPointsPart[0]), xCoord, yCoord, mBaseLabelPaint);
        mBaseLabelPaint.setTextAlign(Paint.Align.RIGHT);
        xCoord = mDrawingAreaWidthEnd;
        canvas.drawText(DateTimeUtils.formatDateMMMd(mXPointsPart[mXPointsPart.length - 1]), xCoord, yCoord, mBaseLabelPaint);
        mBaseLabelPaint.setTextAlign(Paint.Align.LEFT);
        //Log.e(LOG_TAG, String.valueOf(days));
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

    private float[] mapXPoints () {
        long[] xPointsFull = mFullChart.getXPoints();
        long max = MathUtils.getMax(mXPointsPart);
        long min = MathUtils.getMin(mXPointsPart);
        long calculatedArea = max - min;
        float[] mapped;
        if (mAdditionalPointLeft == -1 && mAdditionalPointRight == -1) {
            mapped = new float[mXPointsPart.length];
            for (int i = 0; i < mapped.length; i++) {
                float percentage = (float) (mXPointsPart[i] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
            }
        }
        else if (mAdditionalPointLeft != -1 && mAdditionalPointRight == -1) {
            mapped = new float[mXPointsPart.length + 1];
            float addLeftPercentage = (float) (xPointsFull[mAdditionalPointLeft] - min) / (float) calculatedArea;
            mapped[0] = mDrawingAreaWidth * addLeftPercentage + mDrawingAreaWidthStart;
            for (int i = 1, j = 0; i < mapped.length; i++, j++) {
                float percentage = (float) (mXPointsPart[j] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
            }
        }
        else if (mAdditionalPointLeft == -1 && mAdditionalPointRight != -1) {
            mapped = new float[mXPointsPart.length + 1];
            for (int i = 0; i < mapped.length - 1; i++) {
                float percentage = (float) (mXPointsPart[i] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
            }
            float addRightPercentage = (float) (xPointsFull[mAdditionalPointRight] - min) / (float) calculatedArea;
            mapped[mapped.length - 1] = mDrawingAreaWidth * addRightPercentage + mDrawingAreaWidthStart;
        }
        else {
            mapped = new float[mXPointsPart.length + 2];
            float addLeftPercentage = (float) (xPointsFull[mAdditionalPointLeft] - min) / (float) calculatedArea;
            mapped[0] = mDrawingAreaWidth * addLeftPercentage + mDrawingAreaWidthStart;
            for (int i = 1, j = 0; i < mapped.length - 1; i++, j++) {
                float percentage = (float) (mXPointsPart[j] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
            }
            float addRightPercentage = (float) (xPointsFull[mAdditionalPointRight] - min) / (float) calculatedArea;
            mapped[mapped.length - 1] = mDrawingAreaWidth * addRightPercentage + mDrawingAreaWidthStart;
        }

        return mapped;
    }

    private float[] mapYPoints (int[] yPts, long min, long max, int position) {
        int[][] yPointsFull = mFullChart.getYPointsAsArray();
        long calculatedArea = MathUtils.nearestSixDivider(max - min);
        float[] mapped;
        if (mAdditionalPointLeft == -1 && mAdditionalPointRight == -1) {
            mapped = new float[yPts.length];
            for (int i = 0; i < yPts.length; i++) {
                float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage;
                mapped[i] = mDrawingAreaHeight - mapped[i];
            }
        }
        else if (mAdditionalPointLeft != -1 && mAdditionalPointRight == -1) {
            mapped = new float[yPts.length + 1];
            float addLeftPercentage = (float) (yPointsFull[mIncludedYLinesIndexes[position]][mAdditionalPointLeft] - min) / (float) calculatedArea;
            mapped[0] = mDrawingAreaHeight * addLeftPercentage;
            mapped[0] = mDrawingAreaHeight - mapped[0];
            for (int i = 1, j = 0; i < mapped.length; i++, j++) {
                float percentage = (float) (yPts[j] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage;
                mapped[i] = mDrawingAreaHeight - mapped[i];
            }
        }
        else if (mAdditionalPointLeft == -1 && mAdditionalPointRight != -1) {
            mapped = new float[yPts.length + 1];
            for (int i = 0; i < mapped.length - 1; i++) {
                float percentage = (float) (yPts[i] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage;
                mapped[i] = mDrawingAreaHeight - mapped[i];
            }
            float addRightPercentage = (float) (yPointsFull[mIncludedYLinesIndexes[position]][mAdditionalPointRight] - min) / (float) calculatedArea;
            mapped[mapped.length - 1] = mDrawingAreaHeight * addRightPercentage;
            mapped[mapped.length - 1] = mDrawingAreaHeight - mapped[mapped.length - 1];
        }
        else {
            mapped = new float[yPts.length + 2];
            float addLeftPercentage = (float) (yPointsFull[mIncludedYLinesIndexes[position]][mAdditionalPointLeft] - min) / (float) calculatedArea;
            mapped[0] = mDrawingAreaHeight * addLeftPercentage;
            mapped[0] = mDrawingAreaHeight - mapped[0];
            for (int i = 1, j = 0; i < mapped.length - 1; i++, j++) {
                float percentage = (float) (yPts[j] - min) / (float) calculatedArea;
                mapped[i] = mDrawingAreaHeight * percentage;
                mapped[i] = mDrawingAreaHeight - mapped[i];
            }
            float addRightPercentage = (float) (yPointsFull[mIncludedYLinesIndexes[position]][mAdditionalPointRight] - min) / (float) calculatedArea;
            mapped[mapped.length - 1] = mDrawingAreaHeight * addRightPercentage;
            mapped[mapped.length - 1] = mDrawingAreaHeight - mapped[mapped.length - 1];
        }
        return mapped;
    }

    private float mapXPoint (long point) {
        long calculatedArea = MathUtils.nearestSixDivider(MathUtils.getMax(mXPointsPart) - MathUtils.getMin(mXPointsPart));
        float percentage = ((float) (point - MathUtils.getMin(mXPointsPart))) / (float) calculatedArea;
        float mapped = mDrawingAreaWidth * percentage + mDrawingAreaWidthStart;
        return mapped;
    }

    private float mapYPoint (int arrayPosition, int itemPosition, long min, long max) {
        long calculatedArea = MathUtils.nearestSixDivider(max - min);
        float mapped;
        float percentage = (float) (mYPointsPart[arrayPosition][itemPosition] - min) / (float) calculatedArea;
        mapped = mDrawingAreaHeight * percentage;
        mapped = mDrawingAreaHeight - mapped;
        return mapped;
    }

    private int mapCoordinateToPoint (float xCoord) {
        float calculatedArea = (float) MathUtils.nearestSixDivider(MathUtils.getMax(mXPointsPart) - MathUtils.getMin(mXPointsPart));
        float point = ((xCoord - mDrawingAreaWidthStart) * calculatedArea) / mDrawingAreaWidth + MathUtils.getMin(mXPointsPart);

        int position = 0;
        long closestToPoint = mXPointsPart[position];
        for (int i = 0; i < mXPointsPart.length; i ++) {
            if (Math.abs(mXPointsPart[i] - point) < Math.abs(closestToPoint - point)) {
                position = i;
                closestToPoint = mXPointsPart[i];
            }
        }
        return position;
    }



    private void drawChart (Canvas canvas) {
        if (mXPointsPart == null || mYPointsPart == null) return;
        labelScales(canvas);

        mChartPaint.setColor(Color.RED);

        long maxY = MathUtils.getMax(mYPointsPart);
        long minY = MathUtils.getMin(mYPointsPart);

        float[] mappedX = mapXPoints();

        for (int i = 0; i < mYPointsPart.length; i++) {
            float[] mappedY = mapYPoints(mYPointsPart[i], minY, maxY, i);
            mChartPaint.setColor(Color.parseColor(mColorsPart[i]));
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
        if (mIncludedYLinesIndexes == null || mIncludedYLinesIndexes.length == 0)
            return;
        int pointPosition = mapCoordinateToPoint(xCoord);
        float pointCoordinate = mapXPoint(mXPointsPart[pointPosition]);
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
