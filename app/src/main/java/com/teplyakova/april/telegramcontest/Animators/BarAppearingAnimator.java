package com.teplyakova.april.telegramcontest.Animators;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.teplyakova.april.telegramcontest.Drawing.Chart.Bar;

public class BarAppearingAnimator implements ValueAnimator.AnimatorUpdateListener {
	public static final String COEFFICIENT = "coefficient";
	private Bar _bar;
	private ValueAnimator _animator;

	public BarAppearingAnimator() {

	}

	public void start(Bar bar, float startCoeff, float endCoeff, ValueAnimator.AnimatorUpdateListener... listeners) {
		if (startCoeff == endCoeff)
			return;

		_bar = bar;
		PropertyValuesHolder propertyCoefficient = PropertyValuesHolder.ofFloat(COEFFICIENT, startCoeff, endCoeff);

		if (_animator != null) {
			_animator.cancel();
			_animator = null;
		}

		_animator = new ValueAnimator();
		_animator.setValues(propertyCoefficient);
		_animator.setInterpolator(new AccelerateDecelerateInterpolator());
		_animator.setDuration(200);
		for (ValueAnimator.AnimatorUpdateListener listener : listeners) {
			if (listener != null) {
				_animator.addUpdateListener(listener);
			}
		}
		_animator.addUpdateListener(this);
		_animator.start();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		_bar.PosYCoefficient = (float) animation.getAnimatedValue(COEFFICIENT);
	}

	public boolean isRunning() {
		return (_animator != null && _animator.isRunning());
	}
}
