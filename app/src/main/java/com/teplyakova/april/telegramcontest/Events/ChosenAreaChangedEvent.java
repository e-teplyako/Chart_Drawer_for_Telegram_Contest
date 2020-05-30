package com.teplyakova.april.telegramcontest.Events;

public class ChosenAreaChangedEvent {
	private float _chosenAreaStart = 0f;
	private float _chosenAreaEnd = 1f;

	public ChosenAreaChangedEvent(float start, float end) {
		_chosenAreaStart = start;
		_chosenAreaEnd = end;
	}

	public float getStart() {
		return _chosenAreaStart;
	}

	public float getEnd() {
		return _chosenAreaEnd;
	}
}
