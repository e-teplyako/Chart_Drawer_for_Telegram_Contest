package com.teplyakova.april.telegramcontest.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.MathUtils;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;

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
    ArrayList<PageFragment> mFragments = new ArrayList<>();
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
        Intent intent = getIntent();
        if (intent.hasExtra("fragment_saved_state")) {
            Bundle savedState = intent.getBundleExtra("fragment_saved_state");
            for (int i = 0; i < mFragments.size(); i++) {
                mFragments.get(i).onActivityCreated(savedState.getBundle(String .valueOf(i)));
            }
        }
        int position = intent.getIntExtra("tablayout_position", 0);
        mTabLayout.getTabAt(position).select();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("DeSTroYIng", "this dude");
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

        mFragments.clear();
        for (int i = 0; i < mChartData.size(); i++) {
            PageFragment fragment =  PageFragment.newInstance(i, color2, color1);
            mFragments.add(fragment);
            mAdapter.addFragment(fragment, "Chart #" + String.valueOf(i));
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
                Bundle fragmentSavedState = new Bundle();
                for (int i = 0; i < mFragments.size(); i++) {
                    Bundle fragmentState = new Bundle();
                    mFragments.get(i).onSaveInstanceState(fragmentState);
                    fragmentSavedState.putBundle(String.valueOf(i), fragmentState);
                }
                int tablayoutPosition = mTabLayout.getSelectedTabPosition();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment_saved_state", fragmentSavedState);
                intent.putExtra("tablayout_position", tablayoutPosition);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_mode_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_theme);
        if (isNightModeEnabled()) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night_mode));
        }
        else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day_mode));
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
