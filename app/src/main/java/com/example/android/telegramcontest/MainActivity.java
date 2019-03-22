package com.example.android.telegramcontest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private final String NIGHT_MODE_ENABLED_KEY = "night_mode";
    private boolean mNightModeIsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChartsManager.init(this);
        int amountOfButtons = ChartsManager.getAmountOfCharts();
        LinearLayout layout = (LinearLayout) findViewById(R.id.main_linear_layout);
        for (int i = 0; i < amountOfButtons; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            createButton(i, row);
            layout.addView(row);
        }

    }

    @Override
    protected void onResume() {
        if (mNightModeIsEnabled != isNightModeEnabled())
            recreate();
        super.onResume();
    }

    private void changeTheme() {
        if (isNightModeEnabled()) {
            setTheme(R.style.NightMode);
        }
        else {
            setTheme(R.style.DayMode);
        }
    }

    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mNightModeIsEnabled = sharedPreferences.getBoolean(NIGHT_MODE_ENABLED_KEY, false);
        return mNightModeIsEnabled;
    }


    private void createButton (final int index, LinearLayout row) {
                Button btnTag = new Button(this);
                btnTag.setTag(index);
                btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                btnTag.setText("Chart " + String.valueOf(index));
                btnTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, index);
                        startActivity(intent);
                    }
                });
                row.addView(btnTag);

    }


}
