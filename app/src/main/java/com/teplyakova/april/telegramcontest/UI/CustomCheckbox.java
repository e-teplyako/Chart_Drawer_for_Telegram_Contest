package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.widget.CompoundButton;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

public class CustomCheckbox extends CompoundButton {
	private static int ID = 1337;
	private int _id;

	private CustomCheckbox(Context context) {
		super(context);
	}

	public static CustomCheckbox getCheckbox(Context context, int color) {
		CustomCheckbox cb = new CustomCheckbox(context);
		cb.setButtonDrawable(null);
		cb.setBackground(null);
		cb.setWidth(Math.round(MathUtils.dpToPixels(78, context)));
		cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		cb.setUniqueId(++ID);
		cb.setBackground(getDrawable(color));
		cb.setPadding(20,20,20,20);
		return cb;
	}

	public void setUniqueId(int id) {
		_id = id;
	}

	public  int getUniqueId() {
		return _id;
	}

	private static StateListDrawable getDrawable(int color) {
		StateListDrawable drawable = new StateListDrawable();
		drawable.addState(new int[]{android.R.attr.state_checked}, getCheckedShape(color));
		drawable.addState(new int[]{}, getUncheckedShape(color));
		return drawable;
	}

	private static GradientDrawable getUncheckedShape(int color) {
		GradientDrawable shape = getBaseShape(color);
		shape.setStroke(8, color);
		return shape;
	}

	private static GradientDrawable getCheckedShape(int color) {
		GradientDrawable shape = getBaseShape(color);
		shape.setColor(color);
		return shape;
	}

	private static GradientDrawable getBaseShape(int color) {
		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.RECTANGLE);
		shape.setCornerRadius(100);
		return shape;
	}
}
