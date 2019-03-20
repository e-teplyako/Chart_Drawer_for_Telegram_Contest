package com.example.android.telegramcontest.Interfaces;

public interface WidthObservable {
    void registerObserver (WidthObserver widthObserver);
    void removeObserver (WidthObserver widthObserver);
    void notifyObservers();
}
