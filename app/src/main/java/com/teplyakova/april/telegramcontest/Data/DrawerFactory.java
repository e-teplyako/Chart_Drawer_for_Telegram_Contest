package com.teplyakova.april.telegramcontest.Data;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.IndependentLineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChartDrawer;

public class OldDrawerFactory {
    public static ChartDrawer getChartDrawer(Context context, ChartData chartData) {
        switch (chartData.type) {
            case "LineChartStandard":
                return new LineChartDrawer(chartData);
            case "LineChart2OrdAxis":
                return new IndependentLineChartDrawer(chartData);
            case "BarChart":
                return new LineChartDrawer(chartData);
            case "StackedBarChart":
                return new LineChartDrawer(chartData);
            case "StackedAreaChart":
                return new LineChartDrawer(chartData);
            default:
                return new LineChartDrawer(chartData);
        }
    }
}
