package com.teplyakova.april.telegramcontest.Drawing.Chart;

import android.graphics.Path;

import com.teplyakova.april.telegramcontest.Animators.BarAppearingAnimator;
import com.teplyakova.april.telegramcontest.Data.LineData;

public class Bar {
	LineData Line;
	public float PosYCoefficient;
	float[] MappedPointsY;
	Path Path;
	BarAppearingAnimator Animator = new BarAppearingAnimator();
}
