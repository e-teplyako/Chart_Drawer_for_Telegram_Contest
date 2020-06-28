package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

public class PreCachingLayoutManager extends LinearLayoutManager {
	private int _defaultExtraLayoutSpace = 600;
	private int _extraLayoutSpace = -1;

	public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout, int extraLayoutSpace) {
		super(context, orientation, reverseLayout);
		_extraLayoutSpace = extraLayoutSpace;
	}

	void setExtraLayoutSpace(int extraLayoutSpace) {
		_extraLayoutSpace = extraLayoutSpace;
	}

	@Override
	protected int getExtraLayoutSpace(ChartRecyclerView.State state) {
		return (_extraLayoutSpace > 0) ? _extraLayoutSpace : _defaultExtraLayoutSpace;
	}
}
