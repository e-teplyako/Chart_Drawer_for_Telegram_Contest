package com.teplyakova.april.telegramcontest.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	private static Preferences _instance;

	private static SharedPreferences _sharedPrefs;

	private static final String NIGHT_MODE_ENABLED_KEY = "night_mode";
	private static boolean _isInNightMode;

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
		prefs.setNightMode(_sharedPrefs.getBoolean(NIGHT_MODE_ENABLED_KEY, false));
		return prefs;
	}

	public boolean isInNightMode() {
		return _isInNightMode;
	}

	public void setNightMode(boolean enabled) {
		_isInNightMode = enabled;
		SharedPreferences.Editor editor = _sharedPrefs.edit();
		editor.putBoolean(NIGHT_MODE_ENABLED_KEY, enabled);
		editor.apply();
	}
}
