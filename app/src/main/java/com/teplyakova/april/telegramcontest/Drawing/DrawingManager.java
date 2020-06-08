package com.teplyakova.april.telegramcontest.Drawing;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.Item;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.Data.LocalChartData;
import com.teplyakova.april.telegramcontest.Drawing.Chart.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Chart.ChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Chart.IndependentLineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Chart.LineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Chart.StackedAreaChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Scale.AbsScaleDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Scale.ScaleDrawer;
import com.teplyakova.april.telegramcontest.Drawing.Scale.TwoSidedScaleDrawer;
import com.teplyakova.april.telegramcontest.UI.ThemeHelper;
import com.teplyakova.april.telegramcontest.UI.ThemedDrawer;
import com.teplyakova.april.telegramcontest.Utils.DateTimeUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class DrawingManager {
	private Context _context;
	private ChartData _chartData;
	private LocalChartData _localChartData;
	private ValueAnimator.AnimatorUpdateListener _listener;
	private AbsScaleDrawer _scaleDrawer;
	private PlateDrawer _plateDrawer;
	private ChartDrawer _chartDrawer;
	private HorizontalRangeScaleDrawer _hRangeScaleDrawer;
	private float _chartAreaWidthPx;
	private float _chartAreaMarginX;
	private float _startRange = 0f;
	private float _endRange = 1f;

	private boolean _isPointChosen;
	private int _chosenPointIndex;
	private float _chosenPointXPosition;

	public DrawingManager(ChartData chartData, Context context, ValueAnimator.AnimatorUpdateListener listener) {
		_context = context;
		_chartData = chartData;
		_localChartData = new LocalChartData(_chartData);
		_listener = listener;
		createDrawers();
	}

	public void setMargins(float startX, float endX, float startY, float endY, float chartAreaWidthMarginPx, float height) {
		_chartAreaMarginX = chartAreaWidthMarginPx;
		_chartAreaWidthPx = endX - startX;
		_scaleDrawer.setMargins(startX, endX, startY, endY);
		_hRangeScaleDrawer.setMargins(startX, endX, endY, height);
		_chartDrawer.setMargins(startX, endX, startY, endY, chartAreaWidthMarginPx);
		_chartDrawer.setRangeAndAnimate(0f,1f,_listener);
	}

	public void setLines() {
		_chartDrawer.setLinesAndAnimate(_listener);
		_scaleDrawer.setLinesAndAnimate(_localChartData.getFirstVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx),
				_localChartData.getLastVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx), _listener);
	}

	public void updateRange(float start, float end) {
		setPointIsChosen(false);
		_startRange = start;
		_endRange = end;
		_chartDrawer.setRangeAndAnimate(start, end, _listener);
		_hRangeScaleDrawer.onChosenAreaChanged(start, end);
		_scaleDrawer.chosenAreaChanged(_localChartData.getFirstVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx),
				_localChartData.getLastVisibleIndex(_startRange, _endRange, _chartAreaMarginX, _chartAreaWidthPx), _listener);
	}

	public void draw(Canvas canvas) {
		if (_chartData.type == "LineChartStandard" || _chartData.type == "LineChart2OrdAxis") {
			_scaleDrawer.draw(canvas);
			_chartDrawer.draw(canvas);
		}
		else {
			_chartDrawer.draw(canvas);
			_scaleDrawer.draw(canvas);
		}
		_hRangeScaleDrawer.draw(canvas);
		if (isPointChosen()) {
			_chartDrawer.drawChosenPointHighlight(canvas, getChosenPointIndex());
			_plateDrawer.draw(canvas,
					getChosenPointPosition(),
					DateTimeUtils.formatDateEEEdMMMYYYY(_chartData.getXPoints()[getChosenPointIndex()]),
					getChosenPointDetails(getChosenPointIndex()));
		}
	}

	public void onTouch(float x) {
		int index = _chartDrawer.getTouchedPointIndex(x);
		setChosenPointPositions(index, _chartDrawer.getTouchedPointPosition(index));
		setPointIsChosen(true);
	}

	public void refreshTheme(ThemeHelper themeHelper) {
		setThemedColors(themeHelper, _plateDrawer);
		setThemedColors(themeHelper, _chartDrawer);
		setThemedColors(themeHelper, _scaleDrawer);
		setThemedColors(themeHelper, _hRangeScaleDrawer);
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

	private void createDrawers() {
		_chartDrawer = createChartDrawer(_chartData);
		_scaleDrawer = createScaleDrawer();
		_plateDrawer = createPlateDrawer();
		_hRangeScaleDrawer = createHRangeScaleDrawer();
	}

	public static ChartDrawer createChartDrawer(ChartData chartData) {
		switch (chartData.type) {
			case "LineChartStandard":
				return new LineChartDrawer(chartData);
			case "LineChart2OrdAxis":
				return new IndependentLineChartDrawer(chartData);
			case "BarChart":
			case "StackedBarChart":
				return new BarChartDrawer(chartData);
			case "StackedAreaChart":
				return new StackedAreaChartDrawer(chartData);
			default:
				return new LineChartDrawer(chartData);
		}
	}

	private AbsScaleDrawer createScaleDrawer() {
		switch (_chartData.type) {
			case "LineChart2OrdAxis":
				return new TwoSidedScaleDrawer(_chartData);
			default:
				return new ScaleDrawer(_chartData);
		}
	}

	private PlateDrawer createPlateDrawer() {
		return new PlateDrawer(_context);
	}

	private HorizontalRangeScaleDrawer createHRangeScaleDrawer() {
		return new HorizontalRangeScaleDrawer(_context, _chartData.getXPoints());
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
}
