package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PreCachingLayoutManager extends LinearLayoutManager {
	private int _defaultExtraLayoutSpace = 600;
	private int _extraLayoutSpace = -1;
	private Context _context = null;

	public PreCachingLayoutManager(Context context) {
		super(context);
		_context = context;
	}

	public PreCachingLayoutManager(Context context, int extraLayoutSpace) {
		super(context);
		_context = context;
		_extraLayoutSpace = extraLayoutSpace;
	}

	public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
		_context = context;
	}

	public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout, int extraLayoutSpace) {
		super(context, orientation, reverseLayout);
		_context = context;
		_extraLayoutSpace = extraLayoutSpace;
	}

	public PreCachingLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		_context = context;
	}

	void setExtraLayoutSpace(int extraLayoutSpace) {
		_extraLayoutSpace = extraLayoutSpace;
	}

	@Override
	protected int getExtraLayoutSpace(RecyclerView.State state) {
		return (_extraLayoutSpace > 0) ? _extraLayoutSpace : _defaultExtraLayoutSpace;
	}
}
