package com.example.android.telegramcontest;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.telegramcontest.Utils.DateTimeUtils;
import com.example.android.telegramcontest.Utils.FileIOUtils;
import com.example.android.telegramcontest.Utils.JSONUtils;

import java.io.FileInputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ChartView chartView;
    ScrollChartView scrollChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartView = findViewById(R.id.chartview);
        scrollChartView = findViewById(R.id.scrollchartview);

        long[] x = {1542412800000L,
                1542499200000L,
                1542585600000L,
                1542672000000L,
                1542758400000L,
                1542844800000L,
                1542931200000L,
                1543017600000L,
                1543104000000L,
                1543190400000L};
        int[][] y = {{37, 20, 32, 39, 32, 35, 19, 65, 36, 62},
                {22, 12, 30, 40, 33, 23, 18, 41, 45, 69
                }};
        long[] a = {2, 5, 8, 9, 11, 12, 15, 16, 17, 18};
        int[][] b = {{0, 4, 2, 5, 1, 10, 15, 3, 5, 13}, {12, 15, 9, 9, 9, 12, 15, 9, 9, 9}};
        String[] colors = {"#3DC23F", "#F34C44"};
        String[] names = {"Joined", "Left"};
        //chartView.setChartParams(x, y, colors, names);
        //chartView.setChartParams(a, b, colors, names);
        //chartView.setChartParams(0.2f, 0.4f);
        //chartView.setChartParams(0f, 1f);
        //chartView.setChartParams(0f, 0.5f);
        //chartView.setChartParams(0.2f, 0.8f);
        //scrollChartView.setChartParams(x, y, colors);
        //ChartManager chartManager = ChartManager.getInstance(chartView, scrollChartView);


        String test = FileIOUtils.readFileToString(this);
        if (test == "") Log.e(MainActivity.class.getSimpleName(), "STRING IS NULL");
        Log.e(MainActivity.class.getSimpleName(), test);
        ArrayList<Chart> charts = JSONUtils.parseJSON(test);
        Chart chart = charts.get(2);
        long[] xx = chart.getXPoints();
        ArrayList<int[]> ints = chart.getYPoints();
        int[][] yy = new int[ints.size()][xx.length];
        for (int i = 0; i < yy.length; i++) {
            yy[i] = ints.get(i);
        }
        String[] colorscolors = chart.getColors();
        String[] namesnames = chart.getNames();
        chartView.setChartParams(xx, yy, colorscolors, namesnames);
        scrollChartView.setChartParams(xx, yy, colorscolors);
        ChartManager chartManager = ChartManager.getInstance(chartView, scrollChartView);


    }


}
