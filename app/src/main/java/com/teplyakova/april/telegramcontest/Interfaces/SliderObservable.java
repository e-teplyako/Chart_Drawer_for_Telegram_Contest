package com.teplyakova.april.telegramcontest.Interfaces;

public interface SliderObservable {
    void registerObserver (SliderObserver observer);
    void removeObserver (SliderObserver observer);
    void notifyObservers();
}
