package com.teplyakova.april.telegramcontest;

import android.content.Context;

import com.teplyakova.april.telegramcontest.Utils.FileIOUtils;
import com.teplyakova.april.telegramcontest.Utils.JSONUtils;

import java.util.ArrayList;

public class ChartsManager {

    private static ArrayList<ChartData> _charts;

    public static ArrayList<ChartData> getCharts(Context context){
        if (_charts == null)
        {
            _charts = new ArrayList<>();
            for (String fileName : FileIOUtils.FILE_NAMES) {
                String data = FileIOUtils.readFileToString(context, fileName);
                _charts.add(JSONUtils.parseJSON(data));
            }
        }

        return _charts;
    }
}
