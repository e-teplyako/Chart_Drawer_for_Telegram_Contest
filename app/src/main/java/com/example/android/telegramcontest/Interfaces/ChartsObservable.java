package com.example.android.telegramcontest.Interfaces;

public interface ChartsObservable {
    void registerObserver (ChartsObserver chartsObserver);
    void removeObserver (ChartsObserver chartsObserver);
    void notifyObservers();
}
