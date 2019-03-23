package com.example.android.telegramcontest.Interfaces;

public interface SliderObservable {
    void registerObserver (SliderObserver observer);
    void removeObserver (SliderObserver observer);
    void notifyObservers();
}
