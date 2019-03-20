package com.example.android.telegramcontest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.android.telegramcontest.Interfaces.WidthObserver;
import com.example.android.telegramcontest.Utils.MathUtils;

public class ChartActivity extends AppCompatActivity implements WidthObserver {

    ChartView mChartView;
    ScrollChartView mScrollChartView;
    Chart mChart;
    int[] mIncludedLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mChartView = findViewById(R.id.chartview);
        mScrollChartView = findViewById(R.id.scrollchartview);

        mScrollChartView.registerObserver(this);

        Intent intent = getIntent();
        int index = intent.getIntExtra(Intent.EXTRA_TEXT, 0);

        mChart = ChartData.getChart(index);
        mIncludedLines = mChart.getIndexesOfFullYArray();
        mScrollChartView.setChartParams(mChart, mIncludedLines);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.charts_linear_layout);
        for (int i = 0; i < mIncludedLines.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            String name = mChart.getName(i);
            String color = mChart.getColor(i);
            createCheckbox(mIncludedLines[i], name, color, row);
            linearLayout.addView(row);
        }
    }

    @SuppressLint("RestrictedApi")
    private void createCheckbox (final int index, String name, String color, LinearLayout row) {
        final AppCompatCheckBox checkBox = new AppCompatCheckBox(this);
        checkBox.setTag(index);
        //checkBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
            }
        });
        row.addView(checkBox);

    }

    private void updateIncludedLines(int index, boolean include) {
        if (include) mIncludedLines = MathUtils.add(mIncludedLines, index);
        else mIncludedLines = MathUtils.remove(mIncludedLines, index);

        mScrollChartView.setChartParams(mIncludedLines);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
