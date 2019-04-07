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

    private static final String FILE_NAME = "chart_data.json";

    public static String readFileToString(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(FILE_NAME);
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
