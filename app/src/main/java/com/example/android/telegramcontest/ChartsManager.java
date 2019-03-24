package com.example.android.telegramcontest;

import android.content.Context;

import com.example.android.telegramcontest.Utils.FileIOUtils;
import com.example.android.telegramcontest.Utils.JSONUtils;

import java.util.ArrayList;

public class ChartsManager {

    private static ArrayList<ChartData> mCharts;

    public static ArrayList<ChartData> getCharts(Context context){
        if (mCharts == null)
        {
            String data = FileIOUtils.readFileToString(context);
            mCharts = JSONUtils.parseJSON(data);
        }

        return mCharts;
    }
}
