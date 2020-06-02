package com.teplyakova.april.telegramcontest.Drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.teplyakova.april.telegramcontest.Data.Item;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.Set;

public class PlateDrawer {
	private static final int FONT_SIZE_DP = 12;
	private static final int VERT_MARGIN_TEXT_DP = 4;
	private static final int HOR_MARGIN_TEXT_DP = 7;
	private static final float VERT_MARGIN_PLATE_DP = 42;
	private static final float HOR_MARGIN_PLATE_DP = 16;
	private static final float PLATE_WIDTH_DP = 140;
	private static final float PLATE_CORNER_RADIUS = 25;

	private float _fontSizePx;
	private float _vertMarginTextPx;
	private float _horMarginTextPx;
	private float _vertMarginPlatePx;
	private float _horMarginPlatePx;
	private float _plateWidthPx;

	private RectF _outlineRect;

	private Paint _borderPaint;
	private Paint _fillPaint;
	private TextPaint _headerPaint;
	private TextPaint _itemPaint;
	private TextPaint _valuePaint;

	private int _borderColor = Color.GRAY;
	private int _fillColor = Color.WHITE;

	public PlateDrawer(Context context) {
		setupSizes(context);
		setupPaints();
	}

	public void setBorderColor(int color) {
		_borderPaint.setColor(color);
	}

	public void setFillColor(int color) {
		_fillPaint.setColor(color);
	}

	public void draw(Canvas canvas, float pointPosition, String header, Set<Item> items) {
		drawOutline(canvas, pointPosition, items.size());
		drawText(canvas, header, items);
	}

	private void drawOutline(Canvas canvas, float pointPosition, int numberOfItems) {
		float heightPx = (numberOfItems + 3) * _vertMarginTextPx + (numberOfItems + 1) * _fontSizePx;

		float top = 0f + _vertMarginPlatePx;
		float bottom = top + heightPx;
		float left;
		float right;
		if (shouldDrawOnRightSide(pointPosition, canvas.getWidth())) {
			left = pointPosition + _horMarginPlatePx;
			right = left + _plateWidthPx;
		}
		else {
			right = pointPosition - _horMarginPlatePx;
			left = right - _plateWidthPx;
		}
		_outlineRect = new RectF(left, top, right, bottom);

		canvas.drawRoundRect(_outlineRect, PLATE_CORNER_RADIUS, PLATE_CORNER_RADIUS, _borderPaint);
		canvas.drawRoundRect(_outlineRect, PLATE_CORNER_RADIUS, PLATE_CORNER_RADIUS, _fillPaint);
	}

	private void drawText(Canvas canvas, String header, Set<Item> items) {
		drawHeader(canvas, header);
		drawItems(canvas, items);
	}

	private void drawHeader(Canvas canvas, String header) {
		float headerPosX = _outlineRect.centerX();
		float headerPosY = _outlineRect.top + _vertMarginTextPx + _fontSizePx;
		canvas.drawText(header, headerPosX, headerPosY, _headerPaint);
	}

	private void drawItems(Canvas canvas, Set<Item> items) {
		float namePosX = _outlineRect.left + _horMarginTextPx;
		float itemPosY = _outlineRect.top + 2 * (_vertMarginTextPx + _fontSizePx);
		float valuePosX = _outlineRect.right - _horMarginTextPx;

		for (Item item : items) {
			canvas.drawText(item.getName(), namePosX, itemPosY, _itemPaint);

			_valuePaint.setColor(item.getColor());
			canvas.drawText(String.valueOf(item.getValue()), valuePosX, itemPosY, _valuePaint);
			itemPosY += _vertMarginTextPx + _fontSizePx;
		}
	}

	private void setupSizes(Context context) {
		_fontSizePx = MathUtils.dpToPixels(FONT_SIZE_DP, context);
		_vertMarginTextPx = MathUtils.dpToPixels(VERT_MARGIN_TEXT_DP, context);
		_horMarginTextPx = MathUtils.dpToPixels(HOR_MARGIN_TEXT_DP, context);
		_vertMarginPlatePx = MathUtils.dpToPixels(VERT_MARGIN_PLATE_DP, context);
		_horMarginPlatePx = MathUtils.dpToPixels(HOR_MARGIN_PLATE_DP, context);
		_plateWidthPx = MathUtils.dpToPixels(PLATE_WIDTH_DP, context);
	}

	private boolean shouldDrawOnRightSide(float pointPosition, float canvasWidth) {
		return ((pointPosition + _horMarginPlatePx + _plateWidthPx) < canvasWidth - 10f);
	}

	private void setupPaints() {
		_borderPaint = new Paint();
		_borderPaint.setColor(_borderColor);
		_borderPaint.setStyle(Paint.Style.STROKE);
		_borderPaint.setStrokeWidth(2);

		_fillPaint = new Paint();
		_fillPaint.setStyle(Paint.Style.FILL);
		_fillPaint.setColor(_fillColor);

		_headerPaint = new TextPaint();
		_headerPaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
		_headerPaint.setTextSize(_fontSizePx);
		_headerPaint.setColor(Color.BLACK);
		_headerPaint.setTextAlign(Paint.Align.CENTER);
		_headerPaint.setAntiAlias(true);

		_itemPaint = new TextPaint();
		_itemPaint.setTypeface(Typeface.create("Roboto", Typeface.NORMAL));
		_itemPaint.setTextSize(_fontSizePx);
		_itemPaint.setColor(Color.BLACK);
		_itemPaint.setTextAlign(Paint.Align.LEFT);
		_itemPaint.setAntiAlias(true);

		_valuePaint = new TextPaint();
		_valuePaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
		_valuePaint.setTextSize(_fontSizePx);
		_valuePaint.setTextAlign(Paint.Align.RIGHT);
		_valuePaint.setAntiAlias(true);
	}
}
