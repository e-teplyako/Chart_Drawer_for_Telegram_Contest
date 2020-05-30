package com.teplyakova.april.telegramcontest.Animators;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class LocalYMinMaxAnimator {
	public static final String MIN = "min";
	public static final String MAX = "max";
	private ValueAnimator _animator;

	public LocalYMinMaxAnimator() {

	}

	public void start(int startMin, int endMin,
					  int startMax, int endMax,
					  ValueAnimator.AnimatorUpdateListener... listeners) {
		PropertyValuesHolder propertyMin = PropertyValuesHolder.ofInt(MIN, startMin, endMin);
		PropertyValuesHolder propertyMax = PropertyValuesHolder.ofInt(MAX, startMax, endMax);

		if (_animator != null) {
			_animator.cancel();
			_animator = null;
		}
		_animator = new ValueAnimator();
		_animator.setValues(propertyMin, propertyMax);
		_animator.setInterpolator(new AccelerateDecelerateInterpolator());
		_animator.setDuration(400);
		for (ValueAnimator.AnimatorUpdateListener listener : listeners) {
			if (listener != null) {
				_animator.addUpdateListener(listener);
			}
		}
		_animator.start();
	}
}
