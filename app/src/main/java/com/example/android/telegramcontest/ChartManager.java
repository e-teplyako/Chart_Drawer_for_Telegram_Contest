package com.example.android.telegramcontest;

import android.util.Log;

public class ChartManager implements Observer{
    private static ChartManager instance;
    private ChartView mChartView;
    private ScrollChartView mScrollChartView;

    private ChartManager(ChartView chartView, ScrollChartView scrollChartView){
        mChartView = chartView;
        mScrollChartView = scrollChartView;
        mScrollChartView.registerObserver(this);
    }

    public static ChartManager getInstance(ChartView chartView, ScrollChartView scrollChartView) {
        if (instance == null) {
            instance = new ChartManager(chartView, scrollChartView);
        }
        return instance;
    }


    @Override
    public void update(float start, float percentage) {
        mChartView.setChartParams(start, percentage);
    }
}
