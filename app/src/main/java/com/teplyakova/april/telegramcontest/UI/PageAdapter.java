package com.teplyakova.april.telegramcontest.UI;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.DrawerFactory;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.List;
//TODO: fix checkboxes text color
public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private Context _context;
    private List<ChartData> _chartData;
    private ChartView _chartView;
    private GridLayout _checkboxesLayout;

    PageAdapter(@NonNull List<ChartData> data, LayoutInflater inflater, Context context) {
        _inflater = inflater;
        _chartData = new ArrayList<>(data);
        _context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = _inflater.inflate(R.layout.chart_page, viewGroup, false);
        _chartView =  view.findViewById(R.id.chartview);
        _checkboxesLayout = view.findViewById(R.id.checkboxes_layout);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        _chartView.init(DrawerFactory.getChartDrawer(_context, _chartData.get(i)));
        createAndAttachCheckboxes(i);
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
    }

    private void createAndAttachCheckboxes(int i) {
        int linesCount = _chartData.get(i).lines.length;
        for (int k = 0; k < linesCount; k++) {
            int color = _chartData.get(i).lines[k].color;
            CheckBox cb = new CheckBox(_context);
            cb.setText(_chartData.get(i).lines[k].name);
            cb.setButtonDrawable(null);
            cb.setBackground(getCheckboxDrawable(color));
            cb.setPadding(20,20,20,20);
            _checkboxesLayout.addView(cb);
        }
    }

    private StateListDrawable getCheckboxDrawable(int color) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_checked}, getCheckedShape(color));
        drawable.addState(new int[]{}, getUncheckedShape(color));
        return drawable;
    }

    private GradientDrawable getUncheckedShape(int color) {
        GradientDrawable shape = getBaseShape(color);
        shape.setStroke(8, color);
        return shape;
    }

    private GradientDrawable getCheckedShape(int color) {
       GradientDrawable shape = getBaseShape(color);
       shape.setColor(color);
       return shape;
    }

    private GradientDrawable getBaseShape(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(100);
        return shape;
    }

    private class ChartViewHolder extends RecyclerView.ViewHolder {

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
