package com.example.android.telegramcontest.Utils;

import android.graphics.Color;
import android.util.Log;

import com.example.android.telegramcontest.Chart;
import com.example.android.telegramcontest.ChartData;
import com.example.android.telegramcontest.LineData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONUtils {

    public static ArrayList<ChartData> parseJSON (String jsonString) {
        ArrayList<ChartData> charts = new ArrayList<>();
        try {
            JSONArray jsonRootArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonRootArray.length(); i++) {
                JSONObject jsonChart = jsonRootArray.optJSONObject(i);

                ChartData chartData = new ChartData();
                ArrayList<LineData> lines = new ArrayList<>();

                JSONArray columns = jsonChart.optJSONArray("columns");
                if (columns == null)
                    return null;

                for (int j = 0; j < columns.length(); j++) {
                    JSONArray points = columns.optJSONArray(j);

                    if (points.optString(0).equals("x")) {
                        long[] x = new long[points.length() - 1];
                        for (int m = 1; m < points.length(); m++) {
                            x[m-1] = points.optLong(m);
                        }
                        chartData.posX = x;
                    }
                    else {
                        LineData line  = new LineData();
                        line.id = points.optString(0);
                        line.posY = new long[points.length() - 1];
                        for (int m = 1; m < points.length(); m++) {
                            line.posY[m-1] = points.optLong(m);
                        }
                        lines.add(line);
                    }
                }

                JSONObject jsonNames = jsonChart.optJSONObject("names");
                JSONObject jsonColors = jsonChart.getJSONObject("colors");
                for (LineData line : lines) {
                    line.name = jsonNames.optString(line.id);
                    line.color = Color.parseColor(jsonColors.optString(line.id));
                }

                chartData.lines = lines.toArray(new LineData[lines.size()]);
                charts.add(chartData);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return charts;
    }
}
