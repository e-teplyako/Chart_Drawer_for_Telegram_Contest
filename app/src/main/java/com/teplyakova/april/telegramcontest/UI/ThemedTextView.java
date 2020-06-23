package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ThemedTextView extends TextView implements Themed{
	public ThemedTextView(Context context) {
		super(context);
	}

	public ThemedTextView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ThemedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setTextColor(themeHelper.getMainTextColor());
		invalidate();
	}
}
