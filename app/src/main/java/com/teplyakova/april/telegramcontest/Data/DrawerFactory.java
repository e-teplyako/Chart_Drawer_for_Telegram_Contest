package com.teplyakova.april.telegramcontest.Data;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.IndependentLineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StackedAreaChartDrawer;

public class DrawerFactory {
    public static ChartDrawer getChartDrawer(Context context, ChartData chartData) {
        switch (chartData.type) {
            case "LineChartStandard":
                return new LineChartDrawer(chartData);
            case "LineChart2OrdAxis":
                return new IndependentLineChartDrawer(chartData);
            case "BarChart":
            case "StackedBarChart":
                return new BarChartDrawer(chartData);
            case "StackedAreaChart":
                return new StackedAreaChartDrawer(chartData);
            default:
                return new LineChartDrawer(chartData);
        }
    }
}
