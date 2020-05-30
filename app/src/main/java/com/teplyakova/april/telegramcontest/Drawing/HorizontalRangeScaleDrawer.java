package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;
import java.util.HashMap;

public class HorizontalRangeScaleDrawer {
	private long[] _allElems;
	private float _elemWidthPx;
	private float _distanceBtwElemsPx;
	private float _minVisibleAreaWidthPx;
	private float _textSizePx;

	private float _width;
	private float _startX;
	private float _yPosition;

	private HashMap<Integer, Float> _periodicityToWidthPx = new HashMap<>();
	private TextPaint _paint;

	private float _startVisibleArea = 0f;
	private float _endVisibleArea = 1f;

	private float _sliderMinWidthToWholeWidth;

	public HorizontalRangeScaleDrawer(Context context, long[] elems) {
		setupSizes(context);
		setupPaints();
		_allElems = elems;
	}

	public void draw(Canvas canvas) {
		drawScale(canvas);
	}

	public void setMargins(float startX, float endX, float startY, float endY) {
		_yPosition = startY + (endY - startY) / 2;
		_width = endX - startX;
		_startX = startX;
		_sliderMinWidthToWholeWidth = _minVisibleAreaWidthPx / _width;
		calculatePeriodicity();
	}

	private void calculatePeriodicity() {
		_periodicityToWidthPx = new HashMap<>();
		float minWidth = _width;
		float maxWidth = _width / _sliderMinWidthToWholeWidth;
		for (int i = 1; true; i = i * 2) {
			int labelsCount = _allElems.length / i;
			float width = _elemWidthPx * labelsCount + _distanceBtwElemsPx * (labelsCount - 1);
			if (width >= minWidth && width <= maxWidth)
				_periodicityToWidthPx.put(i, width);
			else if (width < minWidth) {
				_periodicityToWidthPx.put(i, minWidth);
				break;
			}
		}
	}

	private void setupSizes(Context context) {
		_elemWidthPx = MathUtils.dpToPixels(36, context);
		_distanceBtwElemsPx = MathUtils.dpToPixels(22, context);
		_minVisibleAreaWidthPx = MathUtils.dpToPixels(48, context);
		_textSizePx = MathUtils.dpToPixels(12, context);
	}

	private void drawScale(Canvas canvas) {
		int periodicity = getCurrentPeriodicity();
		float startRange = _allElems[0] + (_allElems[_allElems.length - 1] - _allElems[0]) * _startVisibleArea;
		float endRange = _allElems[0] + (_allElems[_allElems.length - 1] - _allElems[0]) * _endVisibleArea;
		_paint.setAlpha(255);
		for (int i = _allElems.length - 1; i >= 0; i -= periodicity) {
			if (_allElems[i] >= startRange && _allElems[i] <= endRange) {
				float x = mapPoint(_allElems[i], startRange, endRange);
				canvas.drawText(DateTimeUtils.formatDateMMMdd(_allElems[i]), x, _yPosition, _paint);
			}
		}

		drawInbetweenOpaqueLabels(canvas, periodicity, startRange, endRange);
	}

	private void drawInbetweenOpaqueLabels(Canvas canvas, int periodicity, float startRange, float endRange) {
		float chartWidthPx = _width / (_endVisibleArea - _startVisibleArea);

		if (_periodicityToWidthPx.get(periodicity) == chartWidthPx)
			return;

		if (_periodicityToWidthPx.containsKey(periodicity / 2)) {
			float alphaMultiplier = MathUtils.inverseLerp(_periodicityToWidthPx.get(periodicity),
					_periodicityToWidthPx.get(periodicity / 2),
					chartWidthPx);
			alphaMultiplier = (alphaMultiplier - 0.334f) * 4;
			alphaMultiplier = MathUtils.clamp(alphaMultiplier, 0, 1);

			_paint.setAlpha((int) Math.floor(255 * alphaMultiplier));
			for (int i = _allElems.length - 1; i >= 0; i -= periodicity / 2) {
				if (_allElems[i] >= startRange && _allElems[i] <= endRange) {
					float x = mapPoint(_allElems[i], startRange, endRange);
					canvas.drawText(DateTimeUtils.formatDateMMMdd(_allElems[i]), x, _yPosition, _paint);
				}
			}
		}
	}

	private int getCurrentPeriodicity() {
		float chartWidthPx = _width / (_endVisibleArea - _startVisibleArea);
		int periodicity;

		for (periodicity = 1; true; periodicity = periodicity * 2) {
			if (_periodicityToWidthPx.containsKey(periodicity)){
				if (_periodicityToWidthPx.get(periodicity) <= chartWidthPx)
					break;
			}
		}
		return periodicity;
	}

	private float mapPoint(long point, float startRange, float endRange) {
		return ((point - startRange) / (endRange - startRange)) * _width + _startX;
	}

	private void setupPaints() {
		_paint = new TextPaint();
		_paint.setTextSize(_textSizePx);
		_paint.setColor(Color.GRAY);
		_paint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
		_paint.setTextAlign(Paint.Align.CENTER);
		_paint.setAntiAlias(true);
	}

	public void onChosenAreaChanged(float start, float end) {
		_startVisibleArea = start;
		_endVisibleArea = end;
	}
}
