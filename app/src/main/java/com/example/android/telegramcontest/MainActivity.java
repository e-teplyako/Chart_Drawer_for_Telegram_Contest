package com.example.android.telegramcontest;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    ChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chartView = findViewById(R.id.chartview);

        long[] x  = {1542412800000L,
                1542499200000L,
                1542585600000L,
                1542672000000L,
                1542758400000L,
                1542844800000L,
                1542931200000L,
                1543017600000L,
                1543104000000L,
                1543190400000L};
        long[][] y = {{37,
                20,
                32,
                39,
                32,
                35,
                19,
                65,
                36,
                62},
                {22,
                        12,
                        30,
                        40,
                        33,
                        23,
                        18,
                        41,
                        45,
                        69
                }};

        long[][] b = {{0, 4, 2, 5 ,1, 10 , 15, 3, 5, 13}, {12, 15, 9, 9, 9, 12, 15, 9, 9, 9}};
        String[] colors = {"#3DC23F", "#F34C44"};
        chartView.setChartParams(x, y, colors);
    }


}
