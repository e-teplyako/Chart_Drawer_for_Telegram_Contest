package com.teplyakova.april.telegramcontest.UI;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;

import com.teplyakova.april.telegramcontest.ChartData;
import com.teplyakova.april.telegramcontest.ChartView;
import com.teplyakova.april.telegramcontest.OldDrawerFactory;
import com.teplyakova.april.telegramcontest.LineData;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private List<ChartData> _chartData;
    private ChartView _chartView;
    private MainActivity _context;

    PageAdapter(@NonNull List<ChartData> chartData, LayoutInflater inflater, MainActivity context) {
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
        _chartView.init(OldDrawerFactory.getChartDrawer(_context, _chartData.get(i)));
        _chartView.setLines(_chartData.get(i).getActiveLines());
        ChartViewHolder vh = (ChartViewHolder) viewHolder;
        vh.bind(i, _chartData.get(i));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        _context.updateUiElements();
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
    }

    class ChartViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {
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
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    cb.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                cb.setText(_chart.getLines()[k].name);
                cb.setChecked(_chart.isLineActive(k));
                int id = cb.getUniqueId();
                _checkboxes[k] = cb;
                _lineByCheckboxId.put(id, _chart.getLines()[k]);
                _checkboxesLayout.addView(cb);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setGravity(Gravity.FILL_HORIZONTAL);
                cb.setLayoutParams(params);
                cb.setOnCheckedChangeListener(this);
                cb.setOnLongClickListener(this);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CustomCheckbox cb = (CustomCheckbox) buttonView;
            _chart.setLineState(_lineByCheckboxId.get(cb.getUniqueId()), isChecked);
            _chartView.setLines(_chart.getActiveLines());
        }

        @Override
        public boolean onLongClick(View v) {
            _chart.setAllLinesState(true);
            _chartView.setLines(_chart.getActiveLines());
            for (CheckBox cb : _checkboxes) {
                cb.setChecked(true);
            }
            return true;
        }
    }
}
