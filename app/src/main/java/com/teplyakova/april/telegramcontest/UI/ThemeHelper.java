package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.teplyakova.april.telegramcontest.R;

public class ThemeHelper {

	private Context _context;

	private Theme _baseTheme;
	private int _primaryColor;
	private int _accentColor;

	public ThemeHelper(Context context) {
		_context = context;
		_baseTheme = Preferences.getInstance(context).getTheme();
	}

	public static ThemeHelper getInstance(Context context) {
		return new ThemeHelper(context);
	}

	public static ThemeHelper getInstanceLoaded(Context context) {
		ThemeHelper instance = getInstance(context);
		instance.updateTheme();
		return instance;
	}

	public void updateTheme(){
		//TODO: query Shared Preferences
		/*_primaryColor = Hawk.get(context.getString(R.string.preference_primary_color),
				getColor(R.color.md_indigo_500));
		_accentColor = Hawk.get(context.getString(R.string.preference_accent_color),
				getColor(R.color.md_light_blue_500));
		_baseTheme = Theme.fromValue(Hawk.get(context.getString(R.string.preference_base_theme), 1));*/
	}

	public int getPrimaryColor() {
		return _primaryColor;
	}

	public int getAccentColor() {
		return _accentColor;
	}

	public Theme getBaseTheme(){
		return _baseTheme;
	}

	public void setBaseTheme(Theme baseTheme) {
		_baseTheme = baseTheme;
		Preferences.getInstance(_context).setTheme(baseTheme);
	}

	public int getPlateFillColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.plateBackgroundColorDay);
		else
			return ContextCompat.getColor(_context, R.color.plateBackgroundColorNight);
	}

	public int getPrimaryBgColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.primaryBackgroundColorDay);
		else
			return ContextCompat.getColor(_context, R.color.primaryBackgroundColorNight);
	}

	public int getSliderBgColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.chartScrollViewBackgroundColorDay);
		else
			return ContextCompat.getColor(_context, R.color.chartScrollViewBackgroundColorNight);
	}

	public int getSliderHandlerColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.sliderColorDay);
		else
			return ContextCompat.getColor(_context, R.color.sliderColorNight);
	}

	public int getDividerColor(){
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.dividerColorDay);
		else
			return ContextCompat.getColor(_context, R.color.dividerColorNight);
	}

	public int getMainTextColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.labelTextColorDay);
		else
			return ContextCompat.getColor(_context, R.color.labelTextColorNight);
	}

	public int getLabelColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.baseLabelColorDay);
		else
			return ContextCompat.getColor(_context, R.color.baseLabelColorNight);
	}

	public int getOpaquePlateColor() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getColor(_context, R.color.opaqueBackgroundDay);
		else
			return ContextCompat.getColor(_context, R.color.opaqueBackgroundNight);
	}

	public Drawable getMenuButtonIcon() {
		if (_baseTheme == Theme.DAY)
			return ContextCompat.getDrawable(_context, R.drawable.ic_day_mode);
		else
			return ContextCompat.getDrawable(_context, R.drawable.ic_night_mode);
	}
}
