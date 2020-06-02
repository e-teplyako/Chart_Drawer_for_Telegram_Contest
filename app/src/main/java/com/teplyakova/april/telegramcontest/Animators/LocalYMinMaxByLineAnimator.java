package com.teplyakova.april.telegramcontest.Animators;



import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.teplyakova.april.telegramcontest.Data.LineData;

import java.util.HashMap;

public class LocalYMinMaxByLineAnimator implements ValueAnimator.AnimatorUpdateListener {
	public static final String MIN = "min";
	public static final String MAX = "max";
	private ValueAnimator _animator;
	private LineData _line;
	private HashMap<LineData, Integer> _localMins;
	private HashMap<LineData, Integer> _localMaxes;

	public LocalYMinMaxByLineAnimator(LineData line, HashMap<LineData, Integer> localMins, HashMap<LineData, Integer> localMaxes) {
		_line = line;
		_localMins = localMins;
		_localMaxes = localMaxes;
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
		_animator.addUpdateListener(this);
		_animator.start();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		_localMins.put(_line, (int) animation.getAnimatedValue(MIN));
		_localMaxes.put(_line, (int) animation.getAnimatedValue(MAX));
	}
}
