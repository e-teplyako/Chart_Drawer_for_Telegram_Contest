package com.teplyakova.april.telegramcontest;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Utils.FileIOUtils;
import com.teplyakova.april.telegramcontest.Utils.JSONUtils;

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
