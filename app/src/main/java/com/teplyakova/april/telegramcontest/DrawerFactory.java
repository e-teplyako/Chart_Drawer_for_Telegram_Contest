package com.teplyakova.april.telegramcontest;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChart2YAxisDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StackedAreaChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StandardLineChartDrawer;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;

public class DrawerFactory {
    public static ChartDrawer getChartDrawer(Context context, ChartData chartData) {
        switch (chartData.type) {
            case "LineChartStandard":
                return new StandardLineChartDrawer(context, chartData);
            case "LineChart2OrdAxis":
                return new LineChart2YAxisDrawer(context, chartData);
            case "BarChart":
                return new BarChartDrawer(context, chartData);
            case "StackedBarChart":
                return new BarChartDrawer(context, chartData);
            case "StackedAreaChart":
                return new StackedAreaChartDrawer(context, chartData);
            default:
                return new StandardLineChartDrawer(context, chartData);
        }
    }
}
