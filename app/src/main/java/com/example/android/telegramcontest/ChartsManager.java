package com.example.android.telegramcontest;

import android.content.Context;

import com.example.android.telegramcontest.Utils.FileIOUtils;
import com.example.android.telegramcontest.Utils.JSONUtils;

import java.util.ArrayList;

public class ChartsManager {

    private static ArrayList<Chart> mCharts;

    public static Chart getChart (int index) {
        return mCharts != null ? mCharts.get(index) : null;
    }

    public static int getAmountOfCharts() {
        return mCharts != null ? mCharts.size() : 0;
    }

    public static void init(Context context) {
        if (mCharts == null)
            mCharts = loadData(context);
    }

    private static ArrayList<Chart> loadData(Context context){
        String data = FileIOUtils.readFileToString(context);
        return JSONUtils.parseJSON(data);
    }

    public static ArrayList<ChartData> loadData2(Context context){
        String data = FileIOUtils.readFileToString(context);
        return JSONUtils.parseJSON2(data);
    }


}
