package com.teplyakova.april.telegramcontest.UI;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Resources.Theme mAppTheme;
    private boolean mNightModeIsEnabled = false;
    private final String NIGHT_MODE_ENABLED_KEY = "night_mode";
    private ArrayList<ChartData> mChartData;

    ChartFragmentPagerAdapter mAdapter;
    ViewPager mViewPager;
    TabLayout mTabLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppTheme = getTheme();

        mChartData = ChartsManager.getCharts(this);

        mAdapter = new ChartFragmentPagerAdapter(getSupportFragmentManager(), this);
        addFragments();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void addFragments() {
        TypedValue textColor = new TypedValue();
        int color1 = Color.GRAY;
        if (mAppTheme.resolveAttribute(R.attr.labelTextColor, textColor, true)) {
             color1 = textColor.data;
        }
        TypedValue backgroundColor = new TypedValue();
        int color2 = Color.WHITE;
        if (mAppTheme.resolveAttribute(R.attr.primaryBackgroundColor, backgroundColor, true)) {
            color2 = backgroundColor.data;
        }
        for (int i = 0; i <mChartData.size(); i++) {
            mAdapter.addFragment(PageFragment.newInstance(i, color2, color1), "Chart #" + String.valueOf(i));
        }
    }

    private void changeTheme() {
        if (isNightModeEnabled()) {
            setTheme(R.style.NightMode);
            mAppTheme = getTheme();

        } else {
            setTheme(R.style.DayMode);
            mAppTheme = getTheme();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
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
