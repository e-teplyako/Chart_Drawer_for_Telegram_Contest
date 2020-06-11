package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ChartRecyclerView extends RecyclerView implements Themed {

	ViewConfiguration vc = ViewConfiguration.get(this.getContext());
	public ChartRecyclerView(@NonNull Context context) {
		super(context);
	}

	public ChartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ChartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	float mDownY;
	float mDownX;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		super.onInterceptTouchEvent(ev);
		int state = this.getScrollState();
		if (state == ChartRecyclerView.SCROLL_STATE_DRAGGING || state == ChartRecyclerView.SCROLL_STATE_SETTLING) {
			return super.onInterceptTouchEvent(ev);
		}
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownY = ev.getRawY();
				mDownX = ev.getRawX();
				return false;
			case MotionEvent.ACTION_MOVE:
				float deltaY = ev.getRawY() - mDownY;
				float deltaX = ev.getRawX() - mDownX;
				if (Math.abs(deltaY) > 60 && Math.abs(deltaX) < 100) {
					Log.e(getClass().getSimpleName(), "SCROLLED");
					super.onInterceptTouchEvent(ev);
					return true;
				}
		}
		return false;
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setBackgroundColor(themeHelper.getPrimaryBgColor());
		invalidate();
	}








}
