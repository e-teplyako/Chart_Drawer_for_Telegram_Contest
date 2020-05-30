package com.teplyakova.april.telegramcontest.Animators;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ScaleAnimator {
	public static final String ALPHA = "alpha";
	public static final String T = "t";

	private float _amplitude = 0.2f;

	private ValueAnimator _animator;

	public ScaleAnimator () {
	}

	public void start(int initAlpha, ValueAnimator.AnimatorUpdateListener... listeners) {
		PropertyValuesHolder propertyAlpha = PropertyValuesHolder.ofInt(ALPHA, initAlpha, 0, 0, 255);
		PropertyValuesHolder t = PropertyValuesHolder.ofFloat(T, 0, _amplitude, 0);

		if (_animator != null) {
			_animator.cancel();
			_animator = null;
		}
		_animator = new ValueAnimator();
		_animator.setValues(propertyAlpha, t);
		_animator.setInterpolator(new AccelerateDecelerateInterpolator());
		_animator.setDuration(800);
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
