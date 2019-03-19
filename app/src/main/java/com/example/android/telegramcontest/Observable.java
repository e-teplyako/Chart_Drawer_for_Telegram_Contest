package com.example.android.telegramcontest;

public interface Observable {
    void registerObserver (Observer observer);
    void removeObserver (Observer observer);
    void notifyObservers();
}
