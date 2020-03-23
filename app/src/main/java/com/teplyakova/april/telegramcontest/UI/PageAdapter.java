package com.teplyakova.april.telegramcontest.UI;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.DrawerFactory;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO: fix checkboxes text color
public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private Context _context;
    private List<ChartData> _chartData;
    private ChartView _chartView;

    PageAdapter(@NonNull List<ChartData> chartData, LayoutInflater inflater, Context context) {
        _inflater = inflater;
        _chartData = new ArrayList<>(chartData);
        _context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = _inflater.inflate(R.layout.chart_page, viewGroup, false);
        _chartView =  view.findViewById(R.id.chartview);
        GridLayout checkboxesLayout = view.findViewById(R.id.checkboxes_layout);
        return new ChartViewHolder(view, _chartView, checkboxesLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        _chartView.init(DrawerFactory.getChartDrawer(_context, _chartData.get(i)));
        _chartView.setLines(_chartData.get(i).getActiveLines());
        ChartViewHolder vh = (ChartViewHolder) viewHolder;
        vh.bind(i, _chartData.get(i));
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
    }

    class ChartViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private ChartView _chartView;
        private ChartData _chart;
        private GridLayout _checkboxesLayout;
        private CustomCheckbox[] _checkboxes;
        private HashMap<Integer, LineData> _lineByCheckboxId = new HashMap<>();

        ChartViewHolder(@NonNull View pageView, @NonNull ChartView chartView, @NonNull GridLayout checkboxesLayout) {
            super(pageView);
            _chartView = chartView;
            _checkboxesLayout = checkboxesLayout;
        }

        void bind(int position, ChartData chart) {
            _chart = chart;
            createAndAttachCheckboxes(position);
        }

        private void createAndAttachCheckboxes(int position) {
            _checkboxesLayout.removeAllViews();
            int linesCount = _chart.getLines().length;
            _checkboxes = new CustomCheckbox[linesCount];
            for (int k = 0; k < linesCount; k++) {
                int color = _chart.getLines()[k].color;
                CustomCheckbox cb = CustomCheckbox.getCheckbox(_chartView.getContext(), color);
                cb.setText(_chart.getLines()[k].name);
                cb.setChecked(_chart.isLineActive(k));
                Log.e(getClass().getSimpleName(), _chart.isLineActive(k) + "");
                int id = cb.getUniqueId();
                _checkboxes[k] = cb;
                _lineByCheckboxId.put(id, _chart.getLines()[k]);
                _checkboxesLayout.addView(cb);
                cb.setOnCheckedChangeListener(this);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CustomCheckbox cb = (CustomCheckbox) buttonView;
            _chart.setLineState(_lineByCheckboxId.get(cb.getUniqueId()), isChecked);
            _chartView.setLines(_chart.getActiveLines());
        }

    }
}
