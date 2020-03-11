package com.teplyakova.april.telegramcontest.UI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private Resources.Theme mAppTheme;
    private boolean mNightModeIsEnabled = false;
    private final String NIGHT_MODE_ENABLED_KEY = "night_mode";
    private ArrayList<ChartData> mChartData;

    private static final String STATE_ADAPTER = "adapter";

    private final SnapHelper snapHelper = new PagerSnapHelper();
    PageAdapter adapter;
    RecyclerView pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppTheme = getTheme();

        StrictMode.ThreadPolicy.Builder builder =
                new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen();
        StrictMode.setThreadPolicy(builder.build());

        mChartData = ChartsManager.getCharts(getApplicationContext());

        pager = findViewById(R.id.pager);
        pager.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        snapHelper.attachToRecyclerView(pager);
        adapter = new PageAdapter(mChartData, getLayoutInflater());
        pager.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Bundle adapterState=new Bundle();
        state.putBundle(STATE_ADAPTER, adapterState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("DeSTroYIng", "this dude");
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
                Intent intent = new Intent(this, MainActivity.class);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mNightModeIsEnabled = sharedPreferences.getBoolean(NIGHT_MODE_ENABLED_KEY, false);
        return mNightModeIsEnabled;
    }

    private void setIsNightModeEnabled (boolean isEnabled) {
        mNightModeIsEnabled = isEnabled;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NIGHT_MODE_ENABLED_KEY, mNightModeIsEnabled);
        editor.apply();
    }
}
