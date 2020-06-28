package com.teplyakova.april.telegramcontest.Drawing.Scale;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.teplyakova.april.telegramcontest.Animators.ScaleAnimator;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class StandardScaleDrawer extends AbsScaleDrawer {
	private int _minValue = MIN_VALUE_DFLT;
	private int _maxValue = MAX_VALUE_DFLT;
	private ScaleAnimator _animator = new ScaleAnimator();
	private int _futureMinValue;
	private int _futureMaxValue;
	private Paint _textPaint = new Paint();

	public StandardScaleDrawer() {

	}

	public StandardScaleDrawer(ChartData chartData) {
		_chartData = chartData;
		setMinValue(MathUtils.getLocalMin(_chartData.getActiveLines(), 0, _chartData.getXPoints().length - 1));
		setMaxValue(MathUtils.getLocalMax(_chartData.getActiveLines(), 0, _chartData.getXPoints().length - 1));
		setUpPaint();
	}

	public void setTextColor(int color) {
		_textPaint.setColor(color);
	}

	public void setAlpha(int alpha) {
		_linePaint.setAlpha(alpha);
		_textPaint.setAlpha(alpha);
	}

	@Override
	public void setLinesAndAnimate(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		setMinMaxValueAndAnimate(MathUtils.getLocalMin(_chartData.getActiveLines(),firstVisibleIndex, lastVisibleIndex),
				MathUtils.getLocalMax(_chartData.getActiveLines(), firstVisibleIndex, lastVisibleIndex), listener);
	}

	@Override
	public void chosenAreaChanged(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		setMinMaxValueAndAnimate(MathUtils.getLocalMin(_chartData.getActiveLines(),	firstVisibleIndex, lastVisibleIndex),
				MathUtils.getLocalMax(_chartData.getActiveLines(), firstVisibleIndex, lastVisibleIndex), listener);
	}

	void setMinValue(int minValue) {
		_minValue = minValue;
	}

	void setMaxValue(int maxValue) {
		_maxValue = maxValue;
	}

	public int getAlpha() {
		return _linePaint.getAlpha();
	}

	public int getMinValue() {
		return _minValue;
	}

	public int getMaxValue() {
		return _maxValue;
	}

	void setMinMaxValueAndAnimate(int minValue, int maxValue, ValueAnimator.AnimatorUpdateListener listener) {
		if (minValue == _minValue && maxValue == _maxValue)
			return;

		_futureMinValue = minValue;
		_futureMaxValue = maxValue;
		_animator.start(getAlpha(), this, listener);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		int alpha = (int) animation.getAnimatedValue(ScaleAnimator.ALPHA);
		_t = (float) animation.getAnimatedValue(ScaleAnimator.T);

		if (alpha < 1) {
			setMinValue(_futureMinValue);
			setMaxValue(_futureMaxValue);
		}

		setAlpha(alpha);
	}

	void setUpPaint() {
		_linePaint.setColor(LINE_COLOR_DFLT);
		_linePaint.setStrokeWidth(LINE_WIDTH_DFLT);

		_textPaint.setColor(TEXT_COLOR_DFLT);
		_textPaint.setTextSize(TEXT_SIZE_DFLT);
		_textPaint.setAntiAlias(true);
	}

	void drawText(Canvas canvas) {
		float x = _startX;
		float y = _endY - TEXT_MARGIN_BOTTOM;
		float step = (_maxValue - _minValue) / (float) (_segmentsNumber);
		float value = _minValue;

		for (int i  = 0; i < _segmentsNumber; i++) {
			DecimalFormat df = new DecimalFormat("##.#");
			df.setRoundingMode(RoundingMode.DOWN);
			canvas.drawText(df.format(value), x, y, _textPaint);
			float margin = canvas.getHeight() * _t;
			y = y - (_endY - _startY + margin)/ _segmentsNumber;
			value += step;
		}
	}

	@Override
	public void setDividerColor(int color) {
		_linePaint.setColor(color);
	}

	@Override
	public void setLabelColor(int color) {
		_textPaint.setColor(color);
	}
}
