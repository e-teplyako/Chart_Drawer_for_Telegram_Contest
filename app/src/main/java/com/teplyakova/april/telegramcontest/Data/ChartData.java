package com.teplyakova.april.telegramcontest.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChartData {
	private long[] _xPoints;
	private LineData[] _lines;
	public String type;
	private Map<LineData, Boolean> _lineToItsState = new HashMap<>();

	public void init(LineData[] lines, long[] xPoints) {
		_lines = lines.clone();
		_xPoints = xPoints.clone();
		setAllLinesState(true);
	}

	public void init(LineData[] lines) {
		_lines = lines.clone();
		setAllLinesState(true);
	}

	public LineData[] getLines() {
		return _lines.clone();
	}

	public long[] getXPoints() {
		return _xPoints;
	}

	public void setXPoints(long[] points) {
		_xPoints = points;
	}

	public void setLineState (LineData line, boolean isActive) {
		_lineToItsState.put(line, isActive);
	}

	public void setAllLinesState(boolean isActive) {
		for (LineData line : _lines) {
			setLineState(line, isActive);
		}
	}

	public LineData[] getActiveLines() {
		ArrayList<LineData> lines = new ArrayList<>();
		for (Map.Entry<LineData, Boolean> entry : _lineToItsState.entrySet()) {
			if (entry.getValue())
				lines.add(entry.getKey());
		}
		return lines.toArray(new LineData[lines.size()]);
	}

	public boolean isLineActive(LineData line) {
		if (_lineToItsState != null) {
			if (_lineToItsState.get(line) != null) {
				return _lineToItsState.get(line);
			}
		}
		return true;
	}

	public boolean isLineActive(int index) {
		return isLineActive(_lines[index]);
	}
}
