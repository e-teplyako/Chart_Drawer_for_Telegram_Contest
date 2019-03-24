package com.example.android.telegramcontest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.example.android.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;

public class PageFragment extends Fragment {

    public static final String INDEX = "index";

    private ChartData mChartData;
    private ScrollChartView mScrollChartView;
    private ChartView mChartView;
    private ArrayList<LineData> mLines = new ArrayList<>();
    private ArrayList<CheckBox> mCheckboxes = new ArrayList<>();

    private static int mBackgroundColor;
    private static int mLabelColor;

    public static PageFragment newInstance (int index, @ColorInt int backgroundColor, @ColorInt int labelColor) {
        Bundle args = new Bundle();
        args.putInt(INDEX, index);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);

        mBackgroundColor = backgroundColor;
        mLabelColor = labelColor;

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChartData = ChartsManager.getCharts(getContext()).get(getArguments().getInt(INDEX));
        mLines.clear();
        for (int i = 0; i < mChartData.lines.length; i++) {
            mLines.add(mChartData.lines[i]);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        view.setBackgroundColor(mBackgroundColor);

        mScrollChartView = view.findViewById(R.id.scrollchartview2);
        mScrollChartView.init(mChartData);

        mChartView = view.findViewById(R.id.chartview2);
        mChartView.init(mChartData, mScrollChartView);

        mCheckboxes.clear();
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.checkboxes_layout);
        for (int i = 0; i < mChartData.lines.length; i++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            createCheckbox(mChartData.lines[i], row);
            linearLayout.addView(row);
        }
        setLines();
        return view;
    }

    @SuppressLint("RestrictedApi")
    private void createCheckbox(final LineData line, LinearLayout row) {
        final AppCompatCheckBox checkBox = new AppCompatCheckBox(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = (int) MathUtils.dpToPixels(8, getContext());
        params.leftMargin = (int) MathUtils.dpToPixels(8, getContext());
        checkBox.setTag(line.id);
        checkBox.setTextColor(mLabelColor);
        checkBox.setText(line.name);
        int uncheckedColor = line.color;
        int checkedColor = line.color;
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
                setLines();
            }
        });
        mCheckboxes.add(checkBox);
        row.addView(checkBox, params);
    }

    private void setLines() {
        ArrayList<LineData> lines = new ArrayList<>();
        for (int i = 0; i < mCheckboxes.size(); i++) {
            if (mCheckboxes.get(i).isChecked()) {
                lines.add(mLines.get(i));
            }
        }
        LineData[] linesArray = lines.toArray(new LineData[lines.size()]);
        mChartView.setLines(linesArray);
        mScrollChartView.setLines(linesArray);
    }
}
