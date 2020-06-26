package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class ChartLinearLayout extends LinearLayout implements Themed{
	public ChartLinearLayout(Context context) {
		super(context);
	}

	public ChartLinearLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ChartLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setBackgroundColor(themeHelper.getPrimaryBgColor());
		invalidate();
	}
}
