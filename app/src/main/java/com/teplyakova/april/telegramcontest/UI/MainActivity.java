package com.teplyakova.april.telegramcontest.UI;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartsManager;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
public class MainActivity extends Activity {

    private static final String STATE_ADAPTER = "adapter";
    PageAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<ChartData> chartData = ChartsManager.getCharts(getApplicationContext());

        recyclerView = findViewById(R.id.pager);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        }

        adapter = new PageAdapter(chartData, getLayoutInflater(), this);
        recyclerView.setAdapter(adapter);
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
    
    private void changeTheme() {
        if (Preferences.getInstance(getApplicationContext()).isInNightMode()) {
            setTheme(R.style.NightMode);
        } else {
            setTheme(R.style.DayMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.switch_theme):
                Preferences.getInstance(getApplicationContext()).setNightMode(!isInNightMode());
                recreate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_mode_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_theme);
        if (isInNightMode()) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_night_mode));
        }
        else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_day_mode));
        }
        return true;
    }

    private boolean isInNightMode() {
        return Preferences.getInstance(getApplicationContext()).isInNightMode();
    }
}
