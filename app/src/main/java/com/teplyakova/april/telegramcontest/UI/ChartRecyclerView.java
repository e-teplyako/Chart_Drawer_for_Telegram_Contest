package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ChartRecyclerView extends RecyclerView implements Themed{
	public ChartRecyclerView(@NonNull Context context) {
		super(context);
	}

	public ChartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ChartRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		int state = this.getScrollState();
		if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
			return super.onInterceptTouchEvent(e);
		}
		return false;
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setBackgroundColor(themeHelper.getPrimaryBgColor());
		invalidate();
	}
}
