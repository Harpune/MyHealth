package de.dbis.myhealth.ui.home;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.dbis.myhealth.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private PieChart pieChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new HomeViewModel();
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup pie chart
        this.pieChart = root.findViewById(R.id.pie_chart_home);
        this.pieChart.getDescription().setEnabled(false);
        this.pieChart.getLegend().setEnabled(false);
        this.pieChart.setCenterTextTypeface(Typeface.SANS_SERIF);
        this.pieChart.setRotationEnabled(true);
        this.pieChart.setCenterTextSize(20f);
        this.pieChart.setDrawEntryLabels(false);
        this.pieChart.setCenterText(this.getWelcomeMessage());
        this.pieChart.setCenterTextColor(this.getTextColor());
        this.pieChart.setHoleRadius(90f);
        this.pieChart.setElevation(0f);
        this.pieChart.setDrawMarkers(false);
        this.pieChart.setHoleColor(Color.TRANSPARENT);
        this.pieChart.setExtraOffsets(32,32,32,32);
        this.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (((PieEntry) e).getLabel().equals(getString(R.string.questionnaires_label))) {
                    Toast.makeText(getContext(), "You already answered " + (int) e.getY() + " questionnaires.", Toast.LENGTH_SHORT).show();
                }

                if (((PieEntry) e).getLabel().equals(getString(R.string.music))) {
                    Toast.makeText(getContext(), "You already listened to " + e.getY() + " minutes of music.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // update with data
        this.setPieChartData();

        root.findViewById(R.id.one_button).setOnClickListener(view -> homeViewModel.incrementOne());
        root.findViewById(R.id.two_button).setOnClickListener(view -> homeViewModel.incrementTwo());
        homeViewModel.getNumberOne().observe(getViewLifecycleOwner(), number -> setPieChartData());
        homeViewModel.getNumberTwo().observe(getViewLifecycleOwner(), number -> setPieChartData());
        return root;
    }

    private void setPieChartData() {
        List<PieEntry> entries = new ArrayList<>();
        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_home_24);

        entries.add(new PieEntry(homeViewModel.getNumberOne().getValue(), getString(R.string.questionnaires_label), icon));
        entries.add(new PieEntry(homeViewModel.getNumberTwo().getValue(), getString(R.string.music), icon));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setDrawIcons(true);
        dataSet.setIconsOffset(MPPointF.getInstance(0,20f));
        dataSet.setSliceSpace(5f);

        PieData pieData = new PieData(dataSet);
        this.pieChart.setData(pieData);
        this.pieChart.invalidate();
    }

    private int getTextColor() {
        final TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorOnBackground, value, true);
        return value.data;

    }

    private String getWelcomeMessage() {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int coffee = 0x2615;
        int sun = 0x1F31E;
        int greeting = 0x1F64B;
        int moon = 0x1F31C;

        if (timeOfDay >= 0 && timeOfDay < 12) {
            return "Good Morning\n" + getEmojiByUnicode(coffee);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            return "Good Afternoon\n" + getEmojiByUnicode(sun);
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            return "Good Evening\n" + getEmojiByUnicode(greeting);
        } else {
            return "Good Night\n" + getEmojiByUnicode(moon);
        }
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}