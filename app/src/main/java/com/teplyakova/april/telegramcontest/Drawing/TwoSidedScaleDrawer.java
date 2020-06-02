package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.text.TextPaint;

import com.teplyakova.april.telegramcontest.Animators.ScaleAnimator;
import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static android.graphics.Paint.Align.RIGHT;

public class TwoSidedScaleDrawer extends AbsScaleDrawer {
	private int _minValueLeft = MIN_VALUE_DFLT;
	private int _maxValueLeft = MAX_VALUE_DFLT;
	private int _minValueRight = MIN_VALUE_DFLT;
	private int _maxValueRight = MAX_VALUE_DFLT;
	private ScaleAnimator _animator = new ScaleAnimator();
	private int _futureMinValueLeft;
	private int _futureMaxValueLeft;
	private int _futureMinValueRight;
	private int _futureMaxValueRight;
	private TextPaint _leftPaint = new TextPaint();
	private TextPaint _rightPaint = new TextPaint();

	public TwoSidedScaleDrawer(ChartData chartData) {
		_chartData = chartData;
		setMinValueLeft(MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[0]}, 0, _chartData.getXPoints().length - 1));
		setMaxValueLeft(MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[0]}, 0, _chartData.getXPoints().length - 1));
		setMinValueRight(MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[1]}, 0, _chartData.getXPoints().length - 1));
		setMaxValueRight(MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[1]}, 0, _chartData.getXPoints().length - 1));
		setUpPaint();
		setLeftTextColor(_chartData.getLines()[0].getColor());
		setRightTextColor(_chartData.getLines()[1].getColor());
	}

	private void setLeftTextColor(int color) {
		_leftPaint.setColor(color);
	}

	private void setRightTextColor(int color) {
		_rightPaint.setColor(color);
	}

	public void setAlpha(int alpha) {
		_linePaint.setAlpha(alpha);
		_leftPaint.setAlpha(alpha);
		_rightPaint.setAlpha(alpha);
	}

	@Override
	public void setLinesAndAnimate(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		int minLeft = MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[0]}, firstVisibleIndex, lastVisibleIndex);
		int maxLeft = MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[0]}, firstVisibleIndex, lastVisibleIndex);
		int minRight = MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[1]}, firstVisibleIndex, lastVisibleIndex);
		int maxRight = MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[1]}, firstVisibleIndex, lastVisibleIndex);
		setMinMaxValueAndAnimate(minLeft, maxLeft, minRight, maxRight, listener);
	}

	@Override
	public void chosenAreaChanged(int firstVisibleIndex, int lastVisibleIndex, ValueAnimator.AnimatorUpdateListener listener) {
		int minLeft = MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[0]}, firstVisibleIndex, lastVisibleIndex);
		int maxLeft = MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[0]}, firstVisibleIndex, lastVisibleIndex);
		int minRight = MathUtils.getLocalMin(new LineData[] {_chartData.getLines()[1]}, firstVisibleIndex, lastVisibleIndex);
		int maxRight = MathUtils.getLocalMax(new LineData[] {_chartData.getLines()[1]}, firstVisibleIndex, lastVisibleIndex);
		setMinMaxValueAndAnimate(minLeft, maxLeft, minRight, maxRight, listener);
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		int alpha = (int) animation.getAnimatedValue(ScaleAnimator.ALPHA);
		_t = (float) animation.getAnimatedValue(ScaleAnimator.T);

		if (alpha < 1) {
			setMinValueLeft(_futureMinValueLeft);
			setMaxValueLeft(_futureMaxValueLeft);
			setMinValueRight(_futureMinValueRight);
			setMaxValueRight(_futureMaxValueRight);
		}

		setAlpha(alpha);
	}

	private void setMinMaxValueAndAnimate(int minValueLeft, int maxValueLeft,
										  int minValueRight, int maxValueRight,
										  ValueAnimator.AnimatorUpdateListener listener) {
		if (minValueLeft == _minValueLeft &&
				maxValueLeft == _maxValueLeft &&
				minValueRight == _minValueRight &&
				maxValueRight == _maxValueRight)
			return;

		_futureMinValueLeft = minValueLeft;
		_futureMaxValueLeft = maxValueLeft;
		_futureMinValueRight = minValueRight;
		_futureMaxValueRight = maxValueRight;
		_animator.start(getAlpha(), this, listener);
	}

	private void setMinValueLeft(int minValue) {
		_minValueLeft = minValue;
	}

	private void setMaxValueLeft(int maxValue) {
		_maxValueLeft = maxValue;
	}

	private void setMinValueRight(int minValue) {
		_minValueRight = minValue;
	}

	private void setMaxValueRight(int maxValue) {
		_maxValueRight = maxValue;
	}

	public int getAlpha() {
		return _linePaint.getAlpha();
	}

	@Override
	void setUpPaint() {
		_linePaint.setColor(LINE_COLOR_DFLT);
		_linePaint.setStrokeWidth(LINE_WIDTH_DFLT);

		_leftPaint.setColor(TEXT_COLOR_DFLT);
		_leftPaint.setTextSize(TEXT_SIZE_DFLT);
		_leftPaint.setAntiAlias(true);

		_rightPaint.setColor(TEXT_COLOR_DFLT);
		_rightPaint.setTextSize(TEXT_SIZE_DFLT);
		_rightPaint.setAntiAlias(true);
		_rightPaint.setTextAlign(RIGHT);
	}

	@Override
	void drawText(Canvas canvas) {
		drawText(canvas, _minValueLeft, _maxValueLeft, _startX, _leftPaint);
		drawText(canvas, _minValueRight, _maxValueRight, _endX, _rightPaint);
	}

	@Override
	public void setDividerColor(int color) {
		_linePaint.setColor(color);
	}

	@Override
	public void setLabelColor(int color) {

	}

	private void drawText(Canvas canvas, int min, int max, float startPoint, TextPaint paint) {
		float x = startPoint;
		float y = _endY - TEXT_MARGIN_BOTTOM;
		float step = (max - min) / (float) (_segmentsNumber);
		float value = min;

		for (int i  = 0; i < _segmentsNumber; i++) {
			DecimalFormat df = new DecimalFormat("##.#");
			df.setRoundingMode(RoundingMode.DOWN);
			canvas.drawText(df.format(value), x, y, paint);
			float margin = canvas.getHeight() * _t;
			y = y - (_endY - _startY + margin)/ _segmentsNumber;
			value += step;
		}
	}
}
