package com.teplyakova.april.telegramcontest.UI;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChartDrawer;
import com.teplyakova.april.telegramcontest.Events.Publisher;
import com.teplyakova.april.telegramcontest.Events.Subscriber;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.HashSet;

public class SliderView extends View implements ValueAnimator.AnimatorUpdateListener, Publisher, Themed {
	private static final float CHOSEN_AREA_START_DFLT = 0f;
	private static final float CHOSEN_AREA_END_DFLT = 1f;
	private static final float ROUNDING_RADIUS = 15f;
	private static final int HANDLER_WIDTH_DP = 12;
	private static final int CHOSEN_AREA_MIN_WIDTH_DP = 48;
	private static final int BG_COLOR_DFLT = Color.parseColor("#99E2EEF9");
	private static final int HANDLER_COLOR_DFLT = Color.parseColor("#8086A9C4");

	private float _handlerWidthPx;
	private float _chosenAreaMinWidthPx;

	private Paint _bgTintPaint;
	private Paint _handlerPaint;
	private int _primaryBgColor;

	private Path _bgPath = new Path();
	private Path _handlerPath = new Path();

	private float _chosenAreaStart;
	private float _chosenAreaEnd;

	private boolean _leftHandlerIsCaught;
	private boolean _rightHandlerIsCaught;
	private boolean _chosenAreaIsCaught;
	private float _currentChosenAreaPosition;

	private ChartData _chartData;
	private ChartDrawer _chartDrawer;

	private Bitmap _chartBitMap;
	private boolean _transitionJustEnded;
	private boolean _themeJustRefreshed;

	private HashSet<Subscriber> _subscribers = new HashSet<Subscriber>();

	public SliderView(Context context) {
		super(context);
		setupPaints();
		setupSizes(context);
		setChosenAreaPositions(CHOSEN_AREA_START_DFLT, CHOSEN_AREA_END_DFLT);
	}

	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupPaints();
		setupSizes(context);
		setChosenAreaPositions(CHOSEN_AREA_START_DFLT, CHOSEN_AREA_END_DFLT);
	}

	public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setupPaints();
		setupSizes(context);
		setChosenAreaPositions(CHOSEN_AREA_START_DFLT, CHOSEN_AREA_END_DFLT);
	}

	public void init(ChartData chartData) {
		_chartData = chartData;
		_chartDrawer = new BarChartDrawer(chartData);
	}

	public void setChosenAreaPositions(float startChosenArea, float endChosenArea) {
		if (startChosenArea < 0 || startChosenArea > 1)
			throw new IllegalArgumentException("Argument must be in [0, 1] range, real value = " + startChosenArea);
		if (endChosenArea < 0 || endChosenArea > 1)
			throw new IllegalArgumentException("Argument must be in [0, 1] range, real value = " + endChosenArea);

		_chosenAreaStart = startChosenArea;
		_chosenAreaEnd = endChosenArea;
		notifySubscribers();
	}

	public void setBgColor(int color) {
		_bgTintPaint.setColor(color);
	}

	public void setHandlerColor(int color) {
		_handlerPaint.setColor(color);
	}

	public void setLines() {
		_chartDrawer.setLinesAndAnimate(this);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (_chartDrawer.isInSetLinesTransition()) {
			_chartDrawer.draw(canvas);
		}
		else if(transitionJustEnded() || isThemeJustRefreshed()) {
			_chartBitMap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.RGB_565);
			Canvas canvasForBitmap = new Canvas(_chartBitMap);
			canvasForBitmap.drawColor(_primaryBgColor); //TODO: fix
			canvasForBitmap = _chartDrawer.draw(canvasForBitmap);
			canvasForBitmap.setBitmap(_chartBitMap);
			setTransitionJustEnded(false);
			setThemeJustRefreshed(false);
			canvas.drawBitmap(_chartBitMap, 0, 0, null);
		}
		else if (_chartBitMap != null){
			canvas.drawBitmap(_chartBitMap, 0, 0, null);
		}
		else {
			canvas.drawColor(_primaryBgColor); //TODO: fix
			_chartDrawer.draw(canvas);
		}
		canvas.drawPath(_bgPath, _bgTintPaint);
		canvas.drawPath(_handlerPath, _handlerPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (isLeftHandlerCaught()){
					float max = MathUtils.clamp(_chosenAreaEnd - _chosenAreaMinWidthPx / getWidth(), 0f, 1f);
					setChosenAreaPositions(MathUtils.clamp(x / getWidth(), 0f, max), _chosenAreaEnd);
				}
				else if (isRightHandlerCaught()) {
					float min = MathUtils.clamp(_chosenAreaStart + _chosenAreaMinWidthPx / getWidth(), 0f, 1f);
					setChosenAreaPositions(_chosenAreaStart, MathUtils.clamp(x / getWidth(), min, 1f));
				}
				else if (isChosenAreaCaught()) {
					float deltaX = x - _currentChosenAreaPosition;
					_currentChosenAreaPosition = x;
					float startPosition = MathUtils.clamp((_chosenAreaStart * getWidth() + deltaX) / getWidth(),
							0f,
							1f - (_chosenAreaEnd - _chosenAreaStart));
					setChosenAreaPositions(startPosition,
							MathUtils.clamp(startPosition + _chosenAreaEnd - _chosenAreaStart, 0f, 1f));
				}
				preparePaths(_chosenAreaStart, _chosenAreaEnd);
				this.getParent().requestDisallowInterceptTouchEvent(true);
				invalidate();
				return true;
			case MotionEvent.ACTION_DOWN:
				if ((x >= _chosenAreaStart * getWidth() - _handlerWidthPx) && (x <= _chosenAreaStart * getWidth() + _handlerWidthPx)) {
					setLeftHandlerCaught(true);
				}
				else if ((x >= _chosenAreaEnd * getWidth() - _handlerWidthPx) &&(x <= _chosenAreaEnd * getWidth() + _handlerWidthPx)) {
					setRightHandlerCaught(true);
				}
				else if ((x >= _chosenAreaStart * getWidth() + _handlerWidthPx) && (x <= _chosenAreaEnd * getWidth() - _handlerWidthPx)) {
					setChosenAreaCaught(true);
					_currentChosenAreaPosition = x;
				}
				preparePaths(_chosenAreaStart, _chosenAreaEnd);
				this.getParent().requestDisallowInterceptTouchEvent(true);
				invalidate();
				return true;
			case MotionEvent.ACTION_UP:
				setRightHandlerCaught(false);
				setLeftHandlerCaught(false);
				setChosenAreaCaught(false);
				preparePaths(_chosenAreaStart, _chosenAreaEnd);
				this.getParent().requestDisallowInterceptTouchEvent(true);
				invalidate();
				return true;
		}
		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		preparePaths(_chosenAreaStart, _chosenAreaEnd);
		float proportion = _chosenAreaMinWidthPx / getWidth();
		_chartDrawer.setMargins(0, w, 0, h, 0);
		_chartDrawer.setRangeAndAnimate(0f, 1f, this);
	}

	private void setupPaints() {
		_bgTintPaint = new Paint();
		_bgTintPaint.setColor(BG_COLOR_DFLT);
		_bgTintPaint.setStyle(Paint.Style.FILL);

		_handlerPaint = new Paint();
		_handlerPaint.setColor(HANDLER_COLOR_DFLT);
		_handlerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		_handlerPaint.setStrokeWidth(4);
	}

	private void setupSizes(Context context) {
		_handlerWidthPx = MathUtils.dpToPixels(HANDLER_WIDTH_DP, context);
		_chosenAreaMinWidthPx = MathUtils.dpToPixels(CHOSEN_AREA_MIN_WIDTH_DP, context);
	}

	private void preparePaths(float startChosenArea, float endChosenArea) {
		prepareBgPath(startChosenArea, endChosenArea);
		prepareHandlerPath(startChosenArea, endChosenArea);
	}

	private void prepareBgPath(float startChosenArea, float endChosenArea) {
		RectF bgLeftRect = new RectF(0f, 0f, startChosenArea * getWidth() + _handlerWidthPx, getHeight());
		RectF bgRightRect = new RectF(endChosenArea * getWidth() - _handlerWidthPx, 0f, getWidth(), getHeight());
		_bgPath.reset();
		_bgPath.addRoundRect(bgLeftRect, ROUNDING_RADIUS, ROUNDING_RADIUS, Path.Direction.CW);
		_bgPath.addRoundRect(bgRightRect, ROUNDING_RADIUS, ROUNDING_RADIUS, Path.Direction.CW);
		_bgPath.addRect(ROUNDING_RADIUS, 0f,startChosenArea * getWidth() + _handlerWidthPx, (float) getHeight(), Path.Direction.CW);
		_bgPath.addRect(endChosenArea * getWidth() - _handlerWidthPx, 0f,getWidth() - ROUNDING_RADIUS, (float) getHeight(), Path.Direction.CW);
	}

	private void prepareHandlerPath(float startChosenArea, float endChosenArea) {
		RectF handlerLeftRect = new RectF(startChosenArea * getWidth(),
				0f,
				startChosenArea * getWidth() + _handlerWidthPx,
				getHeight());
		RectF handlerRightRect = new RectF(endChosenArea * getWidth() - _handlerWidthPx,
				0f,
				endChosenArea * getWidth(),
				getHeight());

		_handlerPath.reset();
		_handlerPath.addRoundRect(handlerLeftRect, ROUNDING_RADIUS, ROUNDING_RADIUS, Path.Direction.CW);
		_handlerPath.addRoundRect(handlerRightRect, ROUNDING_RADIUS, ROUNDING_RADIUS, Path.Direction.CW);
		_handlerPath.addRect(startChosenArea * getWidth() + ROUNDING_RADIUS, 0f,startChosenArea * getWidth() + _handlerWidthPx, (float) getHeight(), Path.Direction.CW);
		_handlerPath.addRect(endChosenArea * getWidth() - _handlerWidthPx, 0f,endChosenArea * getWidth() - ROUNDING_RADIUS, (float) getHeight(), Path.Direction.CW);
		_handlerPath.moveTo(startChosenArea * getWidth() + _handlerWidthPx, 0f);
		_handlerPath.lineTo(endChosenArea * getWidth() - _handlerWidthPx, 0f);
		_handlerPath.moveTo(startChosenArea * getWidth() + _handlerWidthPx, getHeight());
		_handlerPath.lineTo(endChosenArea * getWidth() - _handlerWidthPx, getHeight());
	}

	private boolean isLeftHandlerCaught() {
		return _leftHandlerIsCaught;
	}

	private boolean isRightHandlerCaught() {
		return _rightHandlerIsCaught;
	}

	private boolean isChosenAreaCaught() {
		return _chosenAreaIsCaught;
	}

	private void setLeftHandlerCaught(boolean isCaught) {
		_leftHandlerIsCaught = isCaught;
	}

	private void setRightHandlerCaught(boolean isCaught) {
		_rightHandlerIsCaught = isCaught;
	}

	private void setChosenAreaCaught(boolean isCaught) {
		_chosenAreaIsCaught = isCaught;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		if (animation.getAnimatedFraction() >= 1)
			setTransitionJustEnded(true);
		else
			setTransitionJustEnded(false);
		invalidate();
	}

	private boolean transitionJustEnded() {
		return _transitionJustEnded;
	}

	private void setTransitionJustEnded(boolean justEnded) {
		_transitionJustEnded = justEnded;
	}

	@Override
	public void addSubscriber(Subscriber subscriber) {
		_subscribers.add(subscriber);
	}

	@Override
	public void removeSubscriber(Subscriber subscriber) {
		_subscribers.remove(subscriber);
	}

	@Override
	public void notifySubscribers() {
		for (Subscriber s : _subscribers) {
			s.updateRange(_chosenAreaStart, _chosenAreaEnd);
		}
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setBgColor(themeHelper.getSliderBgColor());
		setHandlerColor(themeHelper.getSliderHandlerColor());
		_primaryBgColor = themeHelper.getPrimaryBgColor();
		setThemeJustRefreshed(true);
		invalidate();
	}

	private void setThemeJustRefreshed(boolean value) {
		_themeJustRefreshed = value;
	}

	private boolean isThemeJustRefreshed() {
		return _themeJustRefreshed;
	}
}
