package com.teplyakova.april.telegramcontest.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileIOUtils {

    public static final String[] FILE_NAMES = {"chart_data_1.json", "chart_data_2.json", "chart_data_3.json", "chart_data_4.json", "chart_data_5.json"};

    public static String readFileToString(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String data;
            while ((data = reader.readLine()) != null) {
                sb.append(data);
            }
            reader.close();
            inputStream.close();
            return sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
