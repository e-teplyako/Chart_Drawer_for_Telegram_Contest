package com.teplyakova.april.telegramcontest.UI;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.Drawing.BarChartDrawer;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.ScrollChartView;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;

public class PageFragment extends Fragment {

    public static final String INDEX = "index";
    public static final String CHECKBOXES_KEY = "checkboxes";

    private ChartData mChartData;
    private ScrollChartView mScrollChartView;
    private ChartView mChartView;
    private ArrayList<LineData> mLines = new ArrayList<>();
    private ArrayList<CheckBox> mCheckboxes = new ArrayList<>();
    private boolean[] mCheckboxesState = null;
    private ChartDrawer mChartDrawer;

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
        //DELETE THIS TEST
        /*if (getArguments().getInt(INDEX) == 4) {
            mChartData = ChartsManager.getCharts(getContext()).get(3);
        }
        else{*/
            mChartData = ChartsManager.getCharts(getContext()).get(getArguments().getInt(INDEX));
        //}

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

        mScrollChartView = view.findViewById(R.id.scrollchartview);
        mScrollChartView.init(mChartData);
        mChartView = view.findViewById(R.id.chartview);
        mChartDrawer = new BarChartDrawer(getContext(), mChartData);
        mChartView.init(mChartDrawer, mScrollChartView);

        mCheckboxesState = null;
        if (savedInstanceState != null) {
            mCheckboxesState = savedInstanceState.getBooleanArray(CHECKBOXES_KEY);
        }

        mCheckboxes.clear();
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.checkboxes_layout);
        for (int i = 0; i < mChartData.lines.length; i++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            if (mCheckboxesState == null || mCheckboxesState.length == 0) {
                createCheckbox(mChartData.lines[i], row, true);
            }
            else {
                createCheckbox(mChartData.lines[i], row, mCheckboxesState[i]);
            }
            linearLayout.addView(row);
        }

        setLines();
        return view;
    }

    @SuppressLint("RestrictedApi")
    private void createCheckbox(final LineData line, LinearLayout row, boolean isChecked) {
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
        checkBox.setChecked(isChecked);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean[] checkboxesState = new boolean[ mCheckboxes.size()];
        for (int i = 0; i < mCheckboxes.size(); i++) {
            checkboxesState[i] = mCheckboxes.get(i).isChecked();
        }
        outState.putBooleanArray(CHECKBOXES_KEY, checkboxesState);
    }
}
