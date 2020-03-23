package com.teplyakova.april.telegramcontest.Utils;

import android.graphics.Color;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.LineData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONUtils {

    public static ChartData parseJSON (String jsonString) {
        if (jsonString == null || jsonString.equals("")) {
            return null;
        }

       ChartData chartData = new ChartData();
        try {
            JSONObject jsonRootObject = new JSONObject(jsonString);

                ArrayList<LineData> lines = new ArrayList<>();
                JSONArray columns = jsonRootObject.optJSONArray("columns");
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

                JSONObject jsonNames = jsonRootObject.optJSONObject("names");
                JSONObject jsonColors = jsonRootObject.getJSONObject("colors");
                for (LineData line : lines) {
                    line.name = jsonNames.optString(line.id);
                    line.color = Color.parseColor(jsonColors.optString(line.id));
                }

                chartData.init(lines.toArray(new LineData[lines.size()]));

                JSONObject jsonTypes = jsonRootObject.optJSONObject("types");
                String type = jsonTypes.optString(lines.get(0).id);
                switch (type){
                    case "line":
                        if (jsonRootObject.optBoolean("y_scaled")) {
                            chartData.type = "LineChart2OrdAxis";
                        }
                        else {
                            chartData.type = "LineChartStandard";
                        }
                        break;
                    case "bar":
                        if (jsonRootObject.optBoolean("stacked")) {
                            chartData.type = "StackedBarChart";
                        }
                        else {
                            chartData.type = "BarChart";
                        }
                        break;
                    case "area":
                        chartData.type = "StackedAreaChart";
                        break;
                    default:
                        chartData.type = "LineChartStandard";
                }

            }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return chartData;
    }
}
