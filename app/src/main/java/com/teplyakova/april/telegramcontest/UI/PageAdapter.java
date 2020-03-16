package com.teplyakova.april.telegramcontest.UI;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import java.util.Map;

//TODO: fix checkboxes text color
public class PageAdapter extends RecyclerView.Adapter {
    private final LayoutInflater _inflater;
    private Context _context;
    private List<ChartData> _chartData;
    private ChartView _chartView;

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
        GridLayout checkboxesLayout = view.findViewById(R.id.checkboxes_layout);
        return new ChartViewHolder(view, _chartView, checkboxesLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        _chartView.init(DrawerFactory.getChartDrawer(_context, _chartData.get(i)));
        _chartView.setLines(_chartData.get(i).lines);
        ChartViewHolder vh = (ChartViewHolder) viewHolder;
        vh.bind(i);
    }

    @Override
    public int getItemCount() {
        return _chartData.size();
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

    private class ChartViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        private LineData[] _lines;
        private ChartView _chartView;
        private GridLayout _checkboxesLayout;
        private SparseBooleanArray _checkboxesState = new SparseBooleanArray();
        private CheckBox[] _checkboxes;
        private Map<Integer, LineData> _lineByCheckboxId = new HashMap<>();

        ChartViewHolder(@NonNull View pageView, @NonNull ChartView chartView, @NonNull GridLayout checkboxesLayout) {
            super(pageView);
            _chartView = chartView;
            _checkboxesLayout = checkboxesLayout;
        }

        void bind(int position) {
            _lines = _chartData.get(position).lines.clone();
            createAndAttachCheckboxes(position);
        }

        private void createAndAttachCheckboxes(int position) {
            int linesCount = _lines.length;
            _checkboxes = new CheckBox[linesCount];
            for (int k = 0; k < linesCount; k++) {
                int color = _chartData.get(position).lines[k].color;
                CheckBox cb = new CheckBox(_context);
                cb.setId(k);
                cb.setChecked(true);
                cb.setText(_chartData.get(position).lines[k].name);
                cb.setButtonDrawable(null);
                cb.setBackground(getCheckboxDrawable(color));
                cb.setPadding(20,20,20,20);
                cb.setOnCheckedChangeListener(this);
                _checkboxes[k] = cb;
                _checkboxesState.put(k, cb.isChecked());
                _lineByCheckboxId.put(k, _chartData.get(position).lines[k]);
                _checkboxesLayout.addView(cb);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _checkboxesState.put(buttonView.getId(), isChecked);
            Log.e(getClass().getSimpleName(), _checkboxesState.toString());
            setLinesForView();
        }

        private void setLinesForView() {
            List<LineData> lines = new ArrayList<>();
            for (CheckBox cb : _checkboxes) {
                if (_checkboxesState.get(cb.getId())){
                    lines.add(_lineByCheckboxId.get(cb.getId()));
                }
            }
            _chartView.setLines(lines.toArray(new LineData[lines.size()]));
        }
    }
}
