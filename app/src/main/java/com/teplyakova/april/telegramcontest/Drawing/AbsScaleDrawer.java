package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.teplyakova.april.telegramcontest.ChartData;

public abstract class AbsScaleDrawer implements ValueAnimator.AnimatorUpdateListener {
	final int MIN_VALUE_DFLT = 0;
	final int MAX_VALUE_DFLT = 420;
	final int SEGMENTS_NUMBER_DFLT = 6;
	final float LINE_WIDTH_DFLT = 2;
	final int LINE_COLOR_DFLT = Color.LTGRAY;
	final int TEXT_COLOR_DFLT = Color.GRAY;
	final float TEXT_MARGIN_BOTTOM = 6;
	final int TEXT_SIZE_DFLT = 36;

	ChartData _chartData;

	int _segmentsNumber = SEGMENTS_NUMBER_DFLT;
	float _startX;
	float _startY;
	float _endX;
	float _endY;
	Paint _linePaint = new Paint();
	float _t;

	public void setLineColor(int color) {
		_linePaint.setColor(color);
	}

	public void setLineWidth(int widthPx) {
		_linePaint.setStrokeWidth(widthPx);
	}

	public abstract void setAlpha(int alpha);

	public void setSegmentsNumber(int number) {
		_segmentsNumber = number;
	}

	public void setMargins(float startX, float endX, float startY, float endY) {
			_startX = startX;
			_endX = endX;
			_startY = startY;
			_endY = endY;
	}

	public abstract void setLinesAndAnimate(int firstVisibleIndex,
											int lastVisibleIndex,
											ValueAnimator.AnimatorUpdateListener listener);

	public abstract void chosenAreaChanged(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener);

	public void drawChosenPointLine(Canvas canvas, float pointPosition) {
		canvas.drawLine(pointPosition, _startY, pointPosition, _endY, _linePaint);
	}

	public void draw(Canvas canvas) {
		drawLines(canvas);
		drawText(canvas);
	}

	abstract void setUpPaint();

	private void drawLines(Canvas canvas) {
		float x1 = _startX;
		float x2 = _endX;
		float y = _endY;
		for (int i  = 0; i < _segmentsNumber; i++) {
			canvas.drawLine(x1, y, x2, y, _linePaint);
			float margin = canvas.getHeight() * _t;
			y -= (_endY - _startY + margin)/ _segmentsNumber;
		}
	}

	abstract void drawText(Canvas canvas);
}
