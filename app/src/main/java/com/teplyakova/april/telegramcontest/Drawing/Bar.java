package com.teplyakova.april.telegramcontest.Drawing;

import com.teplyakova.april.telegramcontest.Data.LineData;

public class Bar {
	public LineData Line;

	public float PosYCoefficientStart;
	public float PosYCoefficient;
	public float PosYCoefficientEnd;
	public float[] MappedPointsY;

	public boolean isVisible() {
		return (PosYCoefficient > 0 || PosYCoefficientEnd > 0);
	}
}
