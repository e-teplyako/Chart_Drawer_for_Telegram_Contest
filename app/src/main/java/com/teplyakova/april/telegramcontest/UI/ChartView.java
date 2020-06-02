package com.teplyakova.april.telegramcontest.UI;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Drawing.AbsScaleDrawer;
import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.HorizontalRangeScaleDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.PlateDrawer;
import com.teplyakova.april.telegramcontest.Drawing.ScaleDrawer;
import com.teplyakova.april.telegramcontest.Events.Publisher;
import com.teplyakova.april.telegramcontest.Events.Subscriber;
import com.teplyakova.april.telegramcontest.Data.Item;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Data.LocalChartData;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class ChartView extends View implements ValueAnimator.AnimatorUpdateListener, Subscriber, Themed {
	private Context _context;
	private AbsScaleDrawer _scaleDrawer;
	private PlateDrawer _plateDrawer;
	private ChartDrawer _chartDrawer;
	private HorizontalRangeScaleDrawer _hRangeScaleDrawer;
	private ChartData _chartData;
	private LocalChartData _localChartData;

	private float _chartAreaWidthPx;
	private float _chartAreaMarginX;

	private boolean _isPointChosen;
	private int _chosenPointIndex;
	private float _chosenPointXPosition;

	private float _startRange = 0f;
	private float _endRange = 1f;

	public ChartView(Context context) {
		super(context);
		_context = context;
	}

	public ChartView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		_context = context;
	}

	public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		_context = context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		_chartAreaWidthPx = getWidth() * 0.9f;
		_chartAreaMarginX = (getWidth() - _chartAreaWidthPx) / 2;
		float chartAreaHeightPx = getHeight() * 0.9f;
		float chartAreaTopMarginY = (getHeight() - chartAreaHeightPx) * 0.2f;
		float chartAreaBottomMarginY = (getHeight() - chartAreaHeightPx) * 0.8f;
		float chartAreaStartX = 0 + _chartAreaMarginX;
		float chartAreaEndX = getWidth() - _chartAreaMarginX;
		float chartAreaStartY = 0 + chartAreaTopMarginY;
		float chartAreaEndY = getHeight() - chartAreaBottomMarginY;

		_scaleDrawer.setMargins(chartAreaStartX, chartAreaEndX, chartAreaStartY, chartAreaEndY);
		_hRangeScaleDrawer.setMargins(chartAreaStartX, chartAreaEndX, chartAreaEndY, getHeight());
		_chartDrawer.setMargins(chartAreaStartX, chartAreaEndX, chartAreaStartY, chartAreaEndY, _chartAreaMarginX);
		//TODO: fix?
		_chartDrawer.setRangeAndAnimate(0f,1f,this);
	}

	public void init(ChartData chartData, Publisher publisher) {
		publisher.addSubscriber(this);
		_chartData = chartData;
		_localChartData = new LocalChartData(chartData);
		_plateDrawer = new PlateDrawer(_context);
		_scaleDrawer = new ScaleDrawer(chartData);
		_chartDrawer = new BarChartDrawer(chartData);
		_hRangeScaleDrawer = new HorizontalRangeScaleDrawer(_context, chartData.getXPoints());
	}

	public void setLines(LineData[] lines) {
		_chartDrawer.setLinesAndAnimate(this);
		_scaleDrawer.setLinesAndAnimate(_localChartData.getFirstVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx),
				_localChartData.getLastVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx), this);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_UP:
				int index = _chartDrawer.getTouchedPointIndex(x);
				setChosenPointPositions(index, _chartDrawer.getTouchedPointPosition(index));
				setPointIsChosen(true);
				this.getParent().requestDisallowInterceptTouchEvent(true);
				invalidate();
				return true;
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		_scaleDrawer.draw(canvas);
		if (isPointChosen()) {
			_scaleDrawer.drawChosenPointLine(canvas, getChosenPointPosition());
		}
		_chartDrawer.draw(canvas);
		_hRangeScaleDrawer.draw(canvas);
		if (isPointChosen()) {
			_plateDrawer.draw(canvas,
					getChosenPointPosition(),
					DateTimeUtils.formatDateEEEdMMMYYYY(_chartData.getXPoints()[getChosenPointIndex()]),
					getChosenPointDetails(getChosenPointIndex()));
			_chartDrawer.drawChosenPointHighlight(canvas, getChosenPointIndex());
		}
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		invalidate();
	}

	public void onDestroy() {
		//TODO: remove subscription?
	}

	private boolean isPointChosen() {
		return _isPointChosen && getChosenPointIndex() >= _localChartData.getFirstInRangeIndex(_startRange) && getChosenPointIndex() <= _localChartData.getLastInRangeIndex(_endRange);
	}

	private Set<Item> getChosenPointDetails(int index) {
		LinkedHashSet<Item> items = new LinkedHashSet<>();
		for (LineData line : _chartData.getActiveLines()) {
			String name = line.getName();
			int color = line.getColor();
			int value = line.getPoints()[index];
			Item item = new Item(name, color, value);
			items.add(item);
		}
		return items;
	}

	private void setChosenPointPositions(int index, float position) {
		_chosenPointIndex = index;
		_chosenPointXPosition = position;
	}

	private int getChosenPointIndex() {
		return _chosenPointIndex;
	}

	private float getChosenPointPosition() {
		return _chosenPointXPosition;
	}

	private void setPointIsChosen(boolean isChosen) {
		_isPointChosen = isChosen;
	}

	@Override
	public void updateRange(float start, float end) {
		setPointIsChosen(false);
		_startRange = start;
		_endRange = end;
		_chartDrawer.setRangeAndAnimate(start, end, this);
		_hRangeScaleDrawer.onChosenAreaChanged(start, end);
		_scaleDrawer.chosenAreaChanged(_localChartData.getFirstVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx),
				_localChartData.getLastVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx), this);
		invalidate();
	}

	@Override
	public void refreshTheme(ThemeHelper themeHelper) {
		setThemedColors(themeHelper, _plateDrawer);
		setThemedColors(themeHelper, _chartDrawer);
		setThemedColors(themeHelper, _scaleDrawer);
		setThemedColors(themeHelper, _hRangeScaleDrawer);
		invalidate();
	}

	private void setThemedColors(ThemeHelper themeHelper, ThemedDrawer drawer) {
		drawer.setPlateFillColor(themeHelper.getPlateFillColor());
		drawer.setPrimaryBgColor(themeHelper.getPrimaryBgColor());
		drawer.setSliderBgColor(themeHelper.getSliderBgColor());
		drawer.setSliderHandlerColor(themeHelper.getSliderHandlerColor());
		drawer.setDividerColor(themeHelper.getDividerColor());
		drawer.setMainTextColor(themeHelper.getMainTextColor());
		drawer.setLabelColor(themeHelper.getLabelColor());
		drawer.setOpaquePlateColor(themeHelper.getOpaquePlateColor());
	}
}
