package com.teplyakova.april.telegramcontest.UI;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.DrawerFactory;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private List<ChartData> _chartData;
    private ChartView _chartView;

    PageAdapter(@NonNull List<ChartData> data, LayoutInflater inflater) {
        _inflater = inflater;
        _chartData = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = _inflater.inflate(R.layout.chart_page, viewGroup, false);
        _chartView =  view.findViewById(R.id.chartview);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        _chartView.init(DrawerFactory.getChartDrawer(_chartView.getContext(), _chartData.get(i)));
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
    }

    private class ChartViewHolder extends RecyclerView.ViewHolder {

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
