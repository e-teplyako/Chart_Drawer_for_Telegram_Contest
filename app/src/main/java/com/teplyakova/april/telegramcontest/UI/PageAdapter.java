package com.teplyakova.april.telegramcontest.UI;

import android.graphics.Color;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.teplyakova.april.telegramcontest.Data.ChartData;
import com.teplyakova.april.telegramcontest.Data.LineData;
import com.teplyakova.april.telegramcontest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageAdapter extends ChartRecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private List<ChartData> _chartData;
    private MainActivity _context;

    PageAdapter(@NonNull List<ChartData> chartData, LayoutInflater inflater, MainActivity context) {
        _inflater = inflater;
        _chartData = new ArrayList<>(chartData);
        _context = context;
    }

    @NonNull
    @Override
    public ChartRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = _inflater.inflate(R.layout.chart_page, viewGroup, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartRecyclerView.ViewHolder viewHolder, int i) {
        ChartViewHolder vh = (ChartViewHolder) viewHolder;
        vh.bind(i, _chartData.get(i));
        //Log.e(getClass().getSimpleName(), "VH binded for position " + i);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ChartRecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        _context.updateUiElements();
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
    }

    class ChartViewHolder extends ChartRecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {
        private ChartView _chartView;
        private SliderView _sliderView;
        private RangeTextView _rangeTextView;
        private TextView _nameTextView;
        private ChartData _chart;
        private GridLayout _checkboxesLayout;
        private CustomCheckbox[] _checkboxes;
        private HashMap<Integer, LineData> _lineByCheckboxId = new HashMap<>();

        ChartViewHolder(@NonNull View view) {
            super(view);

            _chartView =  view.findViewById(R.id.chartview);
            _sliderView = view.findViewById(R.id.slider);
            _rangeTextView = view.findViewById(R.id.rangeTextView);
            _nameTextView = view.findViewById(R.id.nameTextView);
            _checkboxesLayout = view.findViewById(R.id.checkboxes_layout);

            if (_chartView != null && _sliderView != null && _rangeTextView != null && _nameTextView != null && _checkboxesLayout != null)
                Log.e(getClass().getSimpleName(), "All views found by id");
        }

        void bind(int position, ChartData chart) {
            _chart = chart;
            if (_chartData.get(position) == null) {
                Log.e(getClass().getSimpleName(), "Chart # " + position + "doesn't exist");
            }
            _nameTextView.setText("Chart #" + position);
            _sliderView.init(_chartData.get(position));
            _rangeTextView.init(_chartData.get(position), _sliderView);
            _chartView.init(_chartData.get(position), _sliderView);
            _chartView.setLines();
            _sliderView.setLines();
            createAndAttachCheckboxes(position);
        }

        private void createAndAttachCheckboxes(int position) {
            _checkboxesLayout.removeAllViews();
            int linesCount = _chart.getLines().length;
            _checkboxes = new CustomCheckbox[linesCount];
            for (int k = 0; k < linesCount; k++) {
                int color = _chart.getLines()[k].getColor();
                CustomCheckbox cb = CustomCheckbox.getCheckbox(_context, color);
                cb.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                cb.setChecked(_chart.isLineActive(k));
				if (cb.isChecked()) {
					cb.setTextColor(Color.WHITE);
					cb.setText("\u2713" + _chart.getLines()[k].getName());
				}
				else {
					cb.setTextColor(_chart.getLines()[k].getColor());
					cb.setText(_chart.getLines()[k].getName());
				}
                int id = cb.getUniqueId();
                _checkboxes[k] = cb;
                _lineByCheckboxId.put(id, _chart.getLines()[k]);
                _checkboxesLayout.addView(cb);
                cb.setOnCheckedChangeListener(this);
                cb.setOnLongClickListener(this);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CustomCheckbox cb = (CustomCheckbox) buttonView;
            LineData line = _lineByCheckboxId.get(cb.getUniqueId());
            _chart.setLineState(line, isChecked);
            _chartView.setLines();
            _sliderView.setLines();
            if (isChecked) {
				cb.setTextColor(Color.WHITE);
				cb.setText("\u2713" + line.getName());
			}
            else {
				cb.setTextColor(line.getColor());
				cb.setText(line.getName());
			}
        }

        @Override
        public boolean onLongClick(View v) {
            _chart.setAllLinesState(true);
            _chartView.setLines();
            _sliderView.setLines();
            for (CompoundButton cb : _checkboxes) {
                cb.setChecked(true);
            }
            return true;
        }
    }
}
