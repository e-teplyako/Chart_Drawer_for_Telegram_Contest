package com.teplyakova.april.telegramcontest.UI;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.R;

public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater inflater;
    private ChartData mChartData;

    PageAdapter(RecyclerView pager, LayoutInflater inflater) {
        this.pager = pager;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.fragment_page, viewGroup, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    private class ChartViewHolder extends RecyclerView.ViewHolder {

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
