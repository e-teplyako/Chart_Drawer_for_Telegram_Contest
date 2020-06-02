package com.teplyakova.april.telegramcontest.Data;

public class Item {
	private String _name;
	private int _color;
	private int _value;

	public Item(String name, int color, int value) {
		_name = name;
		_color = color;
		_value = value;
	}

	public String getName() {
		return _name;
	}

	public int getColor() {
		return _color;
	}

	public int getValue() {
		return _value;
	}
}
