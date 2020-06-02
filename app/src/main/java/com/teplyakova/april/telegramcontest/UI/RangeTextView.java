package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Events.Publisher;
import com.teplyakova.april.telegramcontest.Events.Subscriber;
import com.teplyakova.april.telegramcontest.Data.LocalChartData;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;

public class RangeTextView extends View implements Subscriber, Themed {
	private ChartData _chartData;
	private LocalChartData _localChartData;
	private float _startRange = 0f;
	private float _endRange = 1f;
	private TextPaint _textPaint;

	public RangeTextView(Context context) {
		super(context);
	}

	public RangeTextView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public RangeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void init(ChartData chartData, Publisher publisher) {
		_chartData = chartData;
		_localChartData = new LocalChartData(chartData);
		_textPaint = new TextPaint();
		_textPaint.setColor(Color.BLACK);
		_textPaint.setTextSize(36);
		_textPaint.setTextAlign(Paint.Align.RIGHT);
		_textPaint.setTypeface(Typeface.create("Roboto", Typeface.BOLD));
		publisher.addSubscriber(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText(DateTimeUtils.formatDatedMMMMMyyyy(_chartData.getXPoints()[_localChartData.getFirstInRangeIndex(_startRange)]) +
				" - " +
				DateTimeUtils.formatDatedMMMMMyyyy(_chartData.getXPoints()[_localChartData.getLastInRangeIndex(_endRange)]), getWidth(), getHeight() / 2, _textPaint);
	}

	@Override
	public void updateRange(float start, float end) {
		_startRange = start;
		_endRange = end;
		invalidate();
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		_textPaint.setColor(themeHelper.getMainTextColor());
		invalidate();
	}
}
