package com.teplyakova.april.telegramcontest.UI;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.DrawerFactory;
import com.teplyakova.april.telegramcontest.Interfaces.ChartDrawer;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;
import com.teplyakova.april.telegramcontest.Utils.MathUtils;

import java.util.ArrayList;

public class PageFragment extends Fragment {

    public static final String INDEX = "index";
    public static final String CHECKBOXES_KEY = "checkboxes";

    private ChartData mChartData;
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

        mChartView = view.findViewById(R.id.chartview);
        mChartDrawer = DrawerFactory.getChartDrawer(getContext(), mChartData);
        mChartView.init(mChartDrawer);

        mCheckboxesState = null;
        if (savedInstanceState != null) {
            mCheckboxesState = savedInstanceState.getBooleanArray(CHECKBOXES_KEY);
        }
        mCheckboxes.clear();

        manageCheckboxes(view, mChartData);

        return view;
    }

    private void manageCheckboxes(View view, ChartData chartData) {
        if (chartData.type == "BarChart")
            return;

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.checkboxes_layout);
        for (int i = 0; i < chartData.lines.length; i += 3) {
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            if (mCheckboxesState == null || mCheckboxesState.length == 0) {
                createCheckbox(chartData.lines[i], row, true);
                if ((i + 1) <= chartData.lines.length - 1)
                    createCheckbox(chartData.lines[i + 1], row, true);
                if ((i + 2) <= chartData.lines.length - 1)
                    createCheckbox(chartData.lines[i + 2], row, true);
            } else {
                createCheckbox(chartData.lines[i], row, mCheckboxesState[i]);
                if ((i + 1) <= chartData.lines.length - 1)
                    createCheckbox(chartData.lines[i + 1], row, mCheckboxesState[i + 1]);
                if ((i + 2) <= chartData.lines.length - 1)
                    createCheckbox(chartData.lines[i + 2], row, mCheckboxesState[i + 2]);
            }
            linearLayout.addView(row);
        }

        setLines();
    }

    private void removeFilters() {
        for (CheckBox box : mCheckboxes) {
            box.setChecked(true);
        }
        setLines();
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
        checkBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeFilters();
                return true;
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
