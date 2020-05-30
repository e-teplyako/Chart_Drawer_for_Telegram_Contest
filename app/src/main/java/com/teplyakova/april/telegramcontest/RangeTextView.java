package com.teplyakova.april.telegramcontest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.teplyakova.april.telegramcontest.Events.ChosenAreaChangedEvent;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RangeTextView extends View {
	private ChartData _chartData;
	private LocalChartData _localChartData;
	private float _startRange = 0f;
	private float _endRange = 1f;
	private TextPaint _textPaint;

	public RangeTextView(Context context) {
		super(context);
		EventBus.getDefault().register(this);
	}

	public RangeTextView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		EventBus.getDefault().register(this);
	}

	public RangeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		EventBus.getDefault().register(this);
	}

	public void init(ChartData chartData) {
		_chartData = chartData;
		_localChartData = new LocalChartData(chartData);
		_textPaint = new TextPaint();
		_textPaint.setColor(Color.BLACK);
		_textPaint.setTextSize(36);
		_textPaint.setTextAlign(Paint.Align.RIGHT);
		_textPaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText(DateTimeUtils.formatDatedMMMMMyyyy(_chartData.getXPoints()[_localChartData.getFirstInRangeIndex(_startRange)]) +
				" - " +
				DateTimeUtils.formatDatedMMMMMyyyy(_chartData.getXPoints()[_localChartData.getLastInRangeIndex(_endRange)]), getWidth(), getHeight() / 2, _textPaint);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onRangeChanged(ChosenAreaChangedEvent event) {
		_startRange = event.getStart();
		_endRange = event.getEnd();
		invalidate();
	}
}
