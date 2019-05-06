package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class BarChartDrawer extends BaseBarChartDrawer {
    private final int   PLATE_HEIGHT_DP = 40;
    private final int   PLATE_WIDTH_DP  = 120;

    private final float mPlateHeightPx;
    private final float mPlateWidthPx;

    public BarChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mPlateHeightPx = MathUtils.dpToPixels(PLATE_HEIGHT_DP, context);
        mPlateWidthPx = MathUtils.dpToPixels(PLATE_WIDTH_DP, context);
    }

    protected void drawChosenPointPlate(Canvas canvas) {
        //plate
        float top = mChartDrawingAreaHeight * 0.05f + mChartDrawingAreaWidth * 0.05f;
        float bottom = top + mPlateHeightPx;
        float left;
        float right;
        float offset = mChartDrawingAreaWidth * 0.05f;
        if ((mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset + mPlateWidthPx) >= mChartDrawingAreaEndX) {
            right = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - mPlateWidthPx;
        } else {
            left = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
            right = left + mPlateWidthPx;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        int cornerRadius = 25;

        TypedValue dividerColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.dividerColor, dividerColor, true)) {
            mPlatePaint.setColor(dividerColor.data);
        }
        mPlatePaint.setStrokeWidth(2);
        mPlatePaint.setStyle(Paint.Style.STROKE);

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        mPlatePaint.setStyle(Paint.Style.FILL);
        TypedValue plateColor = new TypedValue();
        if (mTheme.resolveAttribute(R.attr.plateBackgroundColor, plateColor, true)) {
            mPlatePaint.setColor(plateColor.data);
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPlatePaint);

        //text
        mPlateXValuePaint.setTextSize(mTextSizeLargePx);
        mPlateXValuePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, top + mPlateHeightPx * 0.4f, mPlateXValuePaint);

        mPlateYValuePaint.setTextSize(mTextSizeMediumPx);
        mPlateNamePaint.setTextSize(mTextSizeMediumPx);
        mPlateYValuePaint.setTextAlign(Paint.Align.RIGHT);
        mPlateNamePaint.setTextAlign(Paint.Align.LEFT);
        float heightOffset = 0.8f;
        for (LineData line : mLines){
            mPlateYValuePaint.setColor(line.color);
            canvas.drawText(line.name, left + mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateNamePaint);
            canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), right - mPlateWidthPx * 0.05f, top + mPlateHeightPx * heightOffset, mPlateYValuePaint);
            heightOffset += 0.1f;
        }
    }
}
