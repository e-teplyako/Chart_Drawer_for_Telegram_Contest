package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.teplyakova.april.telegramcontest.Animators.LineAlphaAnimator;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class LineDrawer implements ValueAnimator.AnimatorUpdateListener {
	private LineData _line;

	private int _minVisibleIndex;
	private int _maxVisibleIndex;

	private float[] _mappedXPoints;
	private float[] _mappedYPoints;

	private Paint _linePaint;
	private Paint _circlePaint;
	private int _primaryBgColor;
	private LineAlphaAnimator _alphaAnimator;
	private int _alpha = 255;

	private float _chartAreaStartY;
	private float _chartAreaEndY;

	public LineDrawer(LineData line) {
		_line = line;
		setupPaints();
	}

	public void setMargins(float chartAreaStartY, float chartAreaEndY) {
		_chartAreaStartY = chartAreaStartY;
		_chartAreaEndY = chartAreaEndY;
	}

	public void setStrokeWidth(int width) {
		_linePaint.setStrokeWidth(width);
	}

	public void setMappedXPoints(float[] mapped) {
		_mappedXPoints = mapped;
	}

	public void setMinMaxIndexes(int min, int max) {
		_minVisibleIndex = min;
		_maxVisibleIndex = max;
	}

	public LineData getLine() {
		return _line;
	}

	public Canvas draw(Canvas canvas, int minY, int maxY) {
		canvas = drawLine(minY, maxY, canvas);
		return canvas;
	}

	public void drawChosenPointCircle(Canvas canvas, int index, int minY, int maxY) {
		if (getAlpha() <= 0)
			return;

		_mappedYPoints = mapYPoints(_line.getPoints(), minY, maxY);
		_circlePaint.setColor(_line.getColor());
		_circlePaint.setAlpha(getAlpha());
		canvas.drawCircle(_mappedXPoints[index - _minVisibleIndex], _mappedYPoints[index - _minVisibleIndex], 16f, _circlePaint);
		//TODO: fix
		_circlePaint.setColor(_primaryBgColor);
		_circlePaint.setAlpha(getAlpha());
		canvas.drawCircle(_mappedXPoints[index - _minVisibleIndex], _mappedYPoints[index - _minVisibleIndex], 8f, _circlePaint);
	}

	public void animateAlpha(boolean isLineActive, ValueAnimator.AnimatorUpdateListener listener) {
		_alphaAnimator = new LineAlphaAnimator();
		int endAlpha = 0;
		if (isLineActive)
			endAlpha = 255;
		_alphaAnimator.start(getAlpha(), endAlpha, listener, this);
	}

	private Canvas drawLine(int yMin, int yMax, Canvas canvas) {
		if (getAlpha() <= 0)
			return canvas;

		_linePaint.setColor(_line.getColor());
		_linePaint.setAlpha(getAlpha());
		//TODO: fix
		_mappedYPoints = mapYPoints(_line.getPoints(), yMin, yMax);
		float[] drawingPoints = MathUtils.concatArraysForDrawing(_mappedXPoints, _mappedYPoints);
		if (drawingPoints != null) {
			canvas.drawLines(drawingPoints, _linePaint);
		}
		return canvas;
	}

	private void setupPaints() {
		_linePaint = new Paint();
		_linePaint.setStyle(Paint.Style.STROKE);
		_linePaint.setStrokeWidth(6);
		_linePaint.setAntiAlias(true);
		_linePaint.setStrokeCap(Paint.Cap.ROUND);

		_circlePaint = new Paint();
		_circlePaint.setStyle(Paint.Style.FILL);
		_circlePaint.setAntiAlias(true);
	}

	private float[] mapYPoints(int[] points, long min, long max) {
		long calculatedArea = max - min;
		float[] mapped = new float[_maxVisibleIndex - _minVisibleIndex + 1];

		for (int i = 0, j = _minVisibleIndex; i < mapped.length; i++, j++) {
			float percentage = (float) (points[j] - min) / (float) calculatedArea;
			mapped[i] = (_chartAreaEndY - _chartAreaStartY) * percentage + _chartAreaStartY;
			mapped[i] = _chartAreaEndY - mapped[i] + _chartAreaStartY;
		}
		return mapped;
	}

	private int getAlpha() {
		return _alpha;
	}

	private void setAlpha(int alpha) {
		_alpha = alpha;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		setAlpha((int) animation.getAnimatedValue(LineAlphaAnimator.ALPHA));
	}

	public boolean isInSetLinesTransion() {
		return (_alphaAnimator != null && _alphaAnimator.isRunning());
	}

	public void setPrimaryBgColor(int color) {
		_primaryBgColor = color;
	}

	public void setAntiAlias(boolean aa) {
		_linePaint.setAntiAlias(aa);
	}
}
