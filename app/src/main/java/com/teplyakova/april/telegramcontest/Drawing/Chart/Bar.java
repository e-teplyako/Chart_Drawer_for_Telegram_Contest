package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.graphics.Path;

import com.teplyakova.april.telegramcontest.Animators.BarAppearingAnimator;
import com.teplyakova.april.telegramcontest.Data.LineData;

public class Bar {
	public LineData Line;
	public float PosYCoefficient;
	public float[] MappedPointsY;
	public Path Path;
	public BarAppearingAnimator Animator = new BarAppearingAnimator();
}
