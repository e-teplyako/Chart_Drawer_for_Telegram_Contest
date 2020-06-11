package com.teplyakova.april.telegramcontest.Animators;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ParameterAnimator {
	public static final String T = "t";

	private ValueAnimator _animator;

	public ParameterAnimator() {
	}

	public void start(float initT, ValueAnimator.AnimatorUpdateListener... listeners) {
		if (_animator != null) {
			_animator.cancel();
			_animator = null;
		}

		PropertyValuesHolder t = PropertyValuesHolder.ofFloat(T, initT, 1);

		_animator = new ValueAnimator();
		_animator.setValues(t);
		_animator.setInterpolator(new AccelerateDecelerateInterpolator());
		_animator.setDuration(250);
		for (ValueAnimator.AnimatorUpdateListener listener : listeners) {
			if (listener != null) {
				_animator.addUpdateListener(listener);
			}
		}
		_animator.start();
	}

	public boolean isInProgress() {
		return (_animator != null && _animator.isRunning());
	}

	public float getAnimatedFraction() {
		return _animator.getAnimatedFraction();
	}
}
