package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	private static Preferences _instance;

	private static SharedPreferences _sharedPrefs;

	private static final String THEME = "theme";

	private Preferences(){}

	public static Preferences getInstance(Context context) {
		if (_instance == null) {
			_instance = retrievePrefs(context);
		}
		return _instance;
	}

	private static Preferences retrievePrefs(Context context) {
		Preferences prefs = new Preferences();
		_sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs;
	}

	public Theme getTheme() {
		return Theme.fromValue(_sharedPrefs.getInt(THEME, 1));
	}

	public void setTheme(Theme theme) {
		SharedPreferences.Editor editor = _sharedPrefs.edit();
		editor.putInt(THEME, theme.getValue());
		editor.apply();
	}
}
