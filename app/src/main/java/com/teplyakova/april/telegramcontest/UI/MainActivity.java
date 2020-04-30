package com.teplyakova.april.telegramcontest.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
public class MainActivity extends Activity {

    private static final String STATE_ADAPTER = "adapter";
    PageAdapter adapter;
    RecyclerView recyclerView;
    ThemeHelper _themeHelper;
    MenuItem _menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<ChartData> chartData = ChartsManager.getCharts(getApplicationContext());

        recyclerView = findViewById(R.id.pager);
        recyclerView.setHasFixedSize(true);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }

        adapter = new PageAdapter(chartData, getLayoutInflater(),  this);
        recyclerView.setAdapter(adapter);

        _themeHelper = ThemeHelper.getInstance(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(STATE_ADAPTER, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(state.getParcelable(STATE_ADAPTER));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.switch_theme):
                if (_themeHelper.getBaseTheme() == Theme.DAY) {
                    setBaseTheme(Theme.NIGHT);
                    updateUiElements();
                }
                else {
                    setBaseTheme(Theme.DAY);
                    updateUiElements();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_mode_menu, menu);
        _menuItem = menu.findItem(R.id.switch_theme);
        return true;
    }

    public void setBaseTheme(Theme baseTheme) {
        _themeHelper.setBaseTheme(baseTheme);
    }

    public void updateUiElements() {
        for (View view : ViewUtil.getAllChildren(findViewById(android.R.id.content))) {
            if (view instanceof Themed) ((Themed) view).refreshTheme(_themeHelper);
        }
        setActionBarColor(_themeHelper.getPrimaryBgColor());
        setMenuButtonIcon(_themeHelper.getMenuButtonIcon());
    }

    private void setActionBarColor(int color) {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setBackgroundDrawable(new ColorDrawable(color));
            ab.setTitle(Html.fromHtml("<font color=\"#9E9E9E\">" + getString(R.string.app_name) + "</font>"));
            //TODO: fix colors
        }
    }

    private void setMenuButtonIcon(Drawable icon) {
        if (_menuItem != null)
            _menuItem.setIcon(icon);
        //TODO: fix problem with discrepancy between menu item creation time and adapter views creation time
    }
}
