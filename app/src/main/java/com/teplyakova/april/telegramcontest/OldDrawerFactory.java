package com.teplyakova.april.telegramcontest;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.LineChart2YAxisDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StackedAreaChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StackedBarChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.StandardLineChartDrawer;
import com.teplyakova.april.telegramcontest.Drawing.ChartDrawer;

public class OldDrawerFactory {
    public static ChartDrawer getChartDrawer(Context context, ChartData chartData) {
        switch (chartData.type) {
            case "LineChartStandard":
                return new StandardLineChartDrawer(context, chartData);
            case "LineChart2OrdAxis":
                return new LineChart2YAxisDrawer(context, chartData);
            case "BarChart":
                return new BarChartDrawer(context, chartData);
            case "StackedBarChart":
                return new StackedBarChartDrawer(context, chartData);
            case "StackedAreaChart":
                return new StackedAreaChartDrawer(context, chartData);
            default:
                return new StandardLineChartDrawer(context, chartData);
        }
    }
}
