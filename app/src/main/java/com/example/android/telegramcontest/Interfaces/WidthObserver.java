package com.example.android.telegramcontest.Interfaces;

import com.example.android.telegramcontest.Chart;

public interface WidthObserver {
    void update (Chart chart, float start, float percentage, int[] indexesToInclude);
}
