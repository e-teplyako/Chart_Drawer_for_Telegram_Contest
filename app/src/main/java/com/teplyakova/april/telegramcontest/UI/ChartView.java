package com.teplyakova.april.telegramcontest.UI;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Drawing.DrawingManager;
import com.teplyakova.april.telegramcontest.Events.Publisher;
import com.teplyakova.april.telegramcontest.Events.Subscriber;
import com.teplyakova.april.telegramcontest.Data.LineData;

public class ChartView extends View implements ValueAnimator.AnimatorUpdateListener, Subscriber, Themed {
	private Context _context;
	private DrawingManager _drawingManager;
	private float _downX;

	public ChartView(Context context) {
		super(context);
		_context = context;
	}

	public ChartView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		_context = context;
	}

	public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		_context = context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		float chartAreaWidthPx = getWidth() * 0.9f;
		float chartAreaMarginX = (getWidth() - chartAreaWidthPx) / 2;
		float chartAreaHeightPx = getHeight() * 0.9f;
		float chartAreaTopMarginY = (getHeight() - chartAreaHeightPx) * 0.2f;
		float chartAreaBottomMarginY = (getHeight() - chartAreaHeightPx) * 0.8f;
		float chartAreaStartX = 0 + chartAreaMarginX;
		float chartAreaEndX = getWidth() - chartAreaMarginX;
		float chartAreaStartY = 0 + chartAreaTopMarginY;
		float chartAreaEndY = getHeight() - chartAreaBottomMarginY;

		_drawingManager.setMargins(chartAreaStartX, chartAreaEndX, chartAreaStartY, chartAreaEndY, chartAreaMarginX, getHeight());
	}

	public void init(ChartData chartData, Publisher publisher) {
		publisher.addSubscriber(this);
		if (publisher == null)
			Log.e(getClass().getSimpleName(), "Publisher == null");
		_drawingManager = new DrawingManager(chartData, _context, this);

		if (_drawingManager == null)
			Log.e(getClass().getSimpleName(), "DrawingManager == null");
	}

	public void setLines() {
		_drawingManager.setLines();
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				_downX = event.getRawX();
				return true;
			case MotionEvent.ACTION_MOVE:
				float deltaX = event.getRawX() - _downX;
				if (Math.abs(deltaX) > 60) {
					super.onTouchEvent(event);
					this.getParent().requestDisallowInterceptTouchEvent(true);
					_drawingManager.onTouch(x);
					invalidate();
					return true;
				}
			case MotionEvent.ACTION_UP:
				_drawingManager.onTouch(x);
				invalidate();
				return true;

		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		_drawingManager.draw(canvas);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}

	@Override
	public void updateRange(float start, float end) {
		_drawingManager.updateRange(start, end);
		invalidate();
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		_drawingManager.refreshTheme(themeHelper);
		invalidate();
	}
}
