package com.teplyakova.april.telegramcontest.UI;

public enum Theme {
	DAY(1), NIGHT(2);

	int _value;

	Theme(int value) {
		_value = value;
	}

	public int getValue() { return _value;}

	public static Theme fromValue(int value) {
		switch (value) {
			case 1: default: return DAY;
			case 2: return NIGHT;
		}
	}
}
