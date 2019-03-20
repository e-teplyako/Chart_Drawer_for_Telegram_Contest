package com.example.android.telegramcontest;

import android.content.Context;

import com.example.android.telegramcontest.Utils.FileIOUtils;
import com.example.android.telegramcontest.Utils.JSONUtils;

import java.util.ArrayList;

public class ChartData {
    private static ArrayList<Chart> mCharts;
    private static Context mContext;
    public static void setCharts(ArrayList<Chart> charts){mCharts = charts;}
    public static Chart getChart (int index) {if (mCharts != null) return mCharts.get(index); return null;}
    public static int getAmountOfCharts() {if (mCharts != null) return mCharts.size(); return 0;}
    public static void init(Context context) {
        mContext = context;
        if (mCharts == null) loadData();
    }
    private static void loadData(){
        String data = FileIOUtils.readFileToString(mContext);
        mCharts = JSONUtils.parseJSON(data);
    }

}
