package com.teplyakova.april.telegramcontest.UI;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.DrawerFactory;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater inflater;
    private List<ChartData> chartData;
    private ChartView chartView;

    PageAdapter(@NonNull List<ChartData> data, LayoutInflater inflater) {
        this.inflater = inflater;
        Log.e(getClass().getSimpleName(), (data == null) ? "NULL" : "OK");
        this.chartData = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.fragment_page, viewGroup, false);
        chartView =  view.findViewById(R.id.chartview);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        chartView.init(DrawerFactory.getChartDrawer(chartView.getContext(), chartData.get(i)));
    }

    @Override
    public int getItemCount() {
        return chartData.size();
    }

    private class ChartViewHolder extends RecyclerView.ViewHolder {

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
