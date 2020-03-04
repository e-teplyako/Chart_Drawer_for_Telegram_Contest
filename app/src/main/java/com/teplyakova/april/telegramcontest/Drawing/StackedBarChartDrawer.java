package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class StackedBarChartDrawer extends BaseBarChartDrawer {
    private final float mPlateWidthPx;


    public StackedBarChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        mPlateWidthPx = MathUtils.dpToPixels(140, context);
    }

    protected void drawChosenPointPlate(Canvas canvas) {
        LineData[] lines = getActiveChartLines();
        //plate
        float plateHeightPx = (4 + lines.length) * VERTICAL_TEXT_OFFSET_PX + (2 + lines.length) * TEXT_SIZE_MEDIUM_PX;
        float top = chartAreaHeightPx * 0.05f + chartAreaWidthPx * 0.05f;
        float bottom = top + plateHeightPx;
        float left;
        float right;
        float offset = chartAreaWidthPx * 0.05f;
        if ((mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset + mPlateWidthPx) >= chartAreaEndX) {
            right = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - mPlateWidthPx;
        } else {
            left = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
            right = left + mPlateWidthPx;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        int cornerRadius = 25;

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateBorder);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateFill);

        //text
        float textPosY = top + TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        plateXValue.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + mPlateWidthPx * 0.5f, textPosY, plateXValue);

        textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        long sumOfChosenValues = 0;
        for (LineData line : lines){
            plateYValue.setColor(line.color);
            canvas.drawText(line.name, left + HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateName);
            canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), right - HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateYValue);
            textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
            sumOfChosenValues += line.posY[mPositionOfChosenPoint];
        }
        canvas.drawText("All", left + mPlateWidthPx * 0.05f, textPosY, plateName);
        canvas.drawText(String.valueOf(sumOfChosenValues), right - mPlateWidthPx * 0.05f, textPosY, plateYValue);
    }
}
