package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Arrays;

public class ChartView2 extends View{

    private Context mContext;

    private long[] mPosX;
    private float  mPos1 = -1;
    private float  mPos2 = -1;
    private LineData[] mLines;
    private long mYMin;
    private long mYMax;

    public ChartView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void init (ChartData chartData) {
        mPosX = chartData.posX;
    }

    public void setBorders (float pos1, float pos2) {
        if (mPos1 != pos1 || mPos2 != pos2)
            invalidate();

        mPos1 = pos1;
        mPos2 = pos2;
    }

    public void setLines (LineData[] lines) {
        if (mLines == null || !Arrays.equals(mLines, lines)) 
            invalidate();
        
        mLines = lines;

    }
    
    private void drawLine (LineData line, long yMin, long yMax, int alpha) {
        
    }

    private void drawScaleX () {

    }

    private void drawScaleY (long yMin, long yMax, int alpha) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mPosX == null || mLines == null || mLines.length == 0 || mPos1 < 0 || mPos2 < 0)
            return;

        drawScaleX();
        drawScaleY(mYMin, mYMax, 255);

        for (LineData line : mLines)
            drawLine(line, mYMin, mYMax, 255);
    }
}
