package com.example.android.telegramcontest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.android.telegramcontest.Interfaces.WidthObserver;
import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity implements WidthObserver {

    private ScrollChartView2 mScrollChartView2;
    private ChartView2 mChartView2;
    private ArrayList<LineData> mLines = new ArrayList<>();
    private ChartView mChartView;
    private ScrollChartView mScrollChartView;
    private Chart mChart;
    private int[] mIncludedLines;
    private Resources.Theme mAppTheme;
    private boolean mNightModeIsEnabled = false;
    private final String NIGHT_MODE_ENABLED_KEY = "night_mode";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        mAppTheme = getTheme();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);


        mChartView = findViewById(R.id.chartview);
        mScrollChartView = findViewById(R.id.scrollchartview);



        mScrollChartView.registerObserver(this);

        Intent intent = getIntent();
        int index = intent.getIntExtra(Intent.EXTRA_TEXT, 0);
        mChart = ChartsManager.getChart(index);
        mIncludedLines = mChart.getIndexesOfFullYArray();
        mScrollChartView.setChartParams(mChart, mIncludedLines);

        //TEST
        ChartData chartData = ChartsManager.loadData2(this).get(index);

        mScrollChartView2 = findViewById(R.id.scrollchartview2);
        mScrollChartView2.init(chartData);

        mChartView2 = findViewById(R.id.chartview2);
        mChartView2.init(chartData, mScrollChartView2);
        //TEST

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.charts_linear_layout);
        for (int i = 0; i < chartData.lines.length; i++) {
            mLines.add(chartData.lines[i]);
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String name = mChart.getName(i);
            String color = mChart.getColor(i);
            createCheckbox(mIncludedLines[i], name, color, row);
            createCheckbox2(chartData.lines[i], row);
            linearLayout.addView(row);
        }
        setLines();

    }

    private void changeTheme() {
        if (isNightModeEnabled()) {
            setTheme(R.style.NightMode);
        } else {
            setTheme(R.style.DayMode);
        }
    }

    @SuppressLint("RestrictedApi")
    private void createCheckbox (final int index, String name, String color, LinearLayout row) {
        final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
        checkBox.setTag(index);
        TypedValue textColor = new TypedValue();
        if (mAppTheme.resolveAttribute(R.attr.labelTextColor, textColor, true)) {
            checkBox.setTextColor(textColor.data);
        }
        checkBox.setText(name);
        int uncheckedColor = Color.WHITE;
        int checkedColor = Color.parseColor(color);
        int[] colors = {uncheckedColor, checkedColor};
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[] { -android.R.attr.state_checked },
                        new int[] {  android.R.attr.state_checked }
                },
                new int[] {
                        uncheckedColor,
                        checkedColor
                });
        checkBox.setSupportButtonTintList(colorStateList);
        checkBox.setChecked(true);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateIncludedLines(index, checkBox.isChecked());
                setLines();
            }
        });
        row.addView(checkBox);

    }

    @SuppressLint("RestrictedApi")
    private void createCheckbox2 (final LineData line, LinearLayout row) {
        final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
        checkBox.setTag(line.id);
        TypedValue textColor = new TypedValue();
        if (mAppTheme.resolveAttribute(R.attr.labelTextColor, textColor, true)) {
            checkBox.setTextColor(textColor.data);
        }
        checkBox.setText(line.name);
        int uncheckedColor = Color.WHITE;
        int checkedColor = line.color;
        int[] colors = {uncheckedColor, checkedColor};
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {
                        new int[] { -android.R.attr.state_checked },
                        new int[] {  android.R.attr.state_checked }
                },
                new int[] {
                        uncheckedColor,
                        checkedColor
                });
        checkBox.setSupportButtonTintList(colorStateList);
        checkBox.setChecked(true);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked())
                    mLines.add(line);
                else
                    mLines.remove(line);
                setLines();
            }
        });
        row.addView(checkBox);

    }

    private void updateIncludedLines(int index, boolean include) {
        if (include) mIncludedLines = MathUtils.add(mIncludedLines, index);
        else mIncludedLines = MathUtils.remove(mIncludedLines, index);

        mScrollChartView.setChartParams(mIncludedLines);
    }

    private void setLines() {
        mChartView2.setLines(mLines.toArray(new LineData[mLines.size()]));
        mScrollChartView2.setLines(mLines.toArray(new LineData[mLines.size()]));
    }

    @Override
    public void update(Chart chart, float start, float percentage, int[] indexesToInclude) {
        mChartView.setChartParams(chart, start, percentage, indexesToInclude);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (android.R.id.home):
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case (R.id.switch_theme):
                setIsNightModeEnabled(!isNightModeEnabled());
                recreate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_mode_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_theme);
        if (isNightModeEnabled()) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day_mode));
        }
        else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night_mode));
        }
        return true;
    }

    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mNightModeIsEnabled = sharedPreferences.getBoolean(NIGHT_MODE_ENABLED_KEY, false);
        return mNightModeIsEnabled;
    }

    private void setIsNightModeEnabled (boolean isEnabled) {
        mNightModeIsEnabled = isEnabled;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NIGHT_MODE_ENABLED_KEY, mNightModeIsEnabled);
        editor.apply();
    }
}
