package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class TestLayout extends LinearLayout {
	ViewConfiguration vc = ViewConfiguration.get(this.getContext());
	int mSlop = vc.getScaledTouchSlop();
	int mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
	int mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

	public TestLayout(Context context) {
		super(context);
	}

	public TestLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public TestLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	float mDownY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.e(getClass().getSimpleName(), "INTERCEPTED");
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownY = ev.getRawY();
				return false;
			case MotionEvent.ACTION_MOVE:
				float delta = ev.getRawY() - mDownY;
				if (Math.abs(delta) > 150) {
					Log.e(getClass().getSimpleName(), "SCROLLED");
					super.onInterceptTouchEvent(ev);
					return true;
				}
		}
		return false;
	}
}