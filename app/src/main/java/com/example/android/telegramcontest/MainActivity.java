package com.example.android.telegramcontest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.telegramcontest.Utils.FileIOUtils;
import com.example.android.telegramcontest.Utils.JSONUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChartData.init(this);
        int amountOfButtons = ChartData.getAmountOfCharts();
        LinearLayout layout = (LinearLayout) findViewById(R.id.main_linear_layout);
        for (int i = 0; i < amountOfButtons; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            createButton(i, row);
            layout.addView(row);
        }



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
