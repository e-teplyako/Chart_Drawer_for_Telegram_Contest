package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class BarChartDrawer extends BaseBarChartDrawer {
    private final float PLATE_WIDTH_PX;

    public BarChartDrawer(Context context, ChartData chartData) {
        super(context, chartData);

        PLATE_WIDTH_PX = MathUtils.dpToPixels(140, context);
    }

    protected void drawChosenPointPlate(Canvas canvas) {
        //plate
        float plateHeightPx = (3 + mLines.length) * VERTICAL_TEXT_OFFSET_PX + (1 + mLines.length) * TEXT_SIZE_MEDIUM_PX;
        float top = chartAreaHeightPx * 0.05f + chartAreaWidthPx * 0.05f;
        float bottom = top + plateHeightPx;
        float left;
        float right;
        float offset = chartAreaWidthPx * 0.05f;
        if ((mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset + PLATE_WIDTH_PX) >= chartAreaEndX) {
            right = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] - offset;
            left = right - PLATE_WIDTH_PX;
        } else {
            left = mChartMappedPointsX[mPositionOfChosenPoint - mPointsMinIndex] + offset;
            right = left + PLATE_WIDTH_PX;
        }
        RectF rectF = new RectF(left, top, right, bottom);
        int cornerRadius = 25;

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateBorder);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, plateFill);

        //text
        float textPosY = top + TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        plateXValue.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(DateTimeUtils.formatDateEEEdMMMYYYY(mPosX[mPositionOfChosenPoint]), left + PLATE_WIDTH_PX * 0.5f, textPosY, plateXValue);

        textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        for (LineData line : mLines){
            plateYValue.setColor(line.color);
            canvas.drawText(line.name, left + HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateName);
            canvas.drawText(String.valueOf(line.posY[mPositionOfChosenPoint]), right - HORIZONTAL_TEXT_OFFSET_PX, textPosY, plateYValue);
            textPosY += TEXT_SIZE_MEDIUM_PX + VERTICAL_TEXT_OFFSET_PX;
        }
    }
}
