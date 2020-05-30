package com.teplyakova.april.telegramcontest.Animators;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

public class LineAlphaAnimator {
	public static final String ALPHA = "alpha";
	private ValueAnimator _animator;

	public LineAlphaAnimator() {

	}

	public void start(int startAlpha, int endAlpha, ValueAnimator.AnimatorUpdateListener... listeners) {
		if (startAlpha == endAlpha)
			return;
		
		PropertyValuesHolder propertyAlpha = PropertyValuesHolder.ofInt(ALPHA, startAlpha, endAlpha);

		if (_animator != null) {
			_animator.cancel();
			_animator = null;
		}
		_animator = new ValueAnimator();
		_animator.setValues(propertyAlpha);
		_animator.setInterpolator(new DecelerateInterpolator());
		_animator.setDuration(400);
		for (ValueAnimator.AnimatorUpdateListener listener : listeners) {
			if (listener != null) {
				_animator.addUpdateListener(listener);
			}
		}
		_animator.start();
	}

	public boolean isRunning() {
		return (_animator != null && _animator.isRunning());
	}
}
