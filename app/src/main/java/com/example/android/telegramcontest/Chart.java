package com.example.android.telegramcontest;

import com.example.android.telegramcontest.Utils.DateTimeUtils;

import java.util.ArrayList;

public class Chart {
    private long[] mXPoints;
    private ArrayList<int[]> mYPoints;
    private String[] mTypes;
    private String[] mNames;
    private String[] mColors;

    public Chart(){}

    public long[] getXPoints() {return mXPoints;}
    public ArrayList<int[]> getYPoints() {return mYPoints;}
    public String[] getTypes() {return mTypes;}
    public String[] getNames() {return mNames;}
    public String[] getColors() {return mColors;}

    public void setXPoints(long[] xPoints) {mXPoints = xPoints;}
    public void setYPoints (ArrayList<int[]> yPoints) {mYPoints = yPoints;}
    public void setTypes(String[] types) {mTypes = types;}
    public void setNames(String[] names) {mNames = names;}
    public void setColors(String[] colors) {mColors = colors;}


}
