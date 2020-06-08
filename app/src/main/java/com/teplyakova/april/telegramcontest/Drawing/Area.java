package com.teplyakova.april.telegramcontest.Drawing;

import com.teplyakova.april.telegramcontest.Animators.BarAppearingAnimator;
import com.teplyakova.april.telegramcontest.Data.LineData;

public class Area {
	public LineData Line;
	public int[] Percentages;
	public float PosYCoefficient;
	public float[] MappedPointsY;
	public android.graphics.Path Path;
	public BarAppearingAnimator Animator = new BarAppearingAnimator();
}
