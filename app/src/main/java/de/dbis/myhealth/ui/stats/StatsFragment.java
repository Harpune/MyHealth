package de.dbis.myhealth.ui.stats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.HealthSession;

public class StatsFragment extends Fragment {

    private SharedPreferences mSharedPreferences;

    // View models
    private StatsViewModel mStatsViewModel;

    // Live Data
    private LiveData<List<HealthSession>> mHealthSessionLiveData;

    // Views
    private View mRoot;
    private BarChart mAppOpenedBarChart;
    private BarChart mAppUsedBarChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mStatsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        // Binding
        de.dbis.myhealth.databinding.FragmentStatsBinding mFragmentStatsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stats, container, false);
        mFragmentStatsBinding.setStatsViewModel(this.mStatsViewModel);
        mFragmentStatsBinding.setLifecycleOwner(getViewLifecycleOwner());

        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        this.mRoot = mFragmentStatsBinding.getRoot();
        this.mAppOpenedBarChart = this.mRoot.findViewById(R.id.app_opened_bar_chart);
        this.mAppUsedBarChart = this.mRoot.findViewById(R.id.app_used_bar_chart);

        // setup charts
        this.setupAppOpenedChart();
        this.setupAppUsedChart();

        return this.mRoot;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.mHealthSessionLiveData = this.mStatsViewModel.getHealthSessions();
        this.mHealthSessionLiveData.observe(getViewLifecycleOwner(), this::handleSessions);
    }

    private void handleSessions(List<HealthSession> healthSessions) {
        // App opened
        BarDataSet appOpenedDataSet = this.getAppOpenedBarDataSet(healthSessions);
        BarData appOpenedData = new BarData();
        appOpenedData.addDataSet(appOpenedDataSet);
        this.mAppOpenedBarChart.setData(appOpenedData);

        // App used & music
        BarDataSet appUsedDataSet = this.getAppUsedBarDataSet(healthSessions);
        BarData appUsedData = new BarData();
        appUsedData.addDataSet(appUsedDataSet);
        this.mAppUsedBarChart.setData(appUsedData);
    }

    private BarDataSet getAppUsedBarDataSet(List<HealthSession> healthSessions) {
        // Usage chart
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> barLabels = new ArrayList<>();

        // Get session of each day
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(1L).plusDays(1L);

        // check last weeks dates if sessions were created
        int count = 0;
        for (LocalDate date = start; date.isBefore(end) || date.isEqual(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;

            // find session of each day in the last week
            List<HealthSession> list = healthSessions.stream()
                    .filter(healthSession -> convertToLocalDate(healthSession.getDate()).isEqual(finalDate))
                    .collect(Collectors.toList());

            long totalTime = list.stream().mapToLong(HealthSession::getTimeAppOpened).sum();
            long musicTime = list
                    .stream()
                    .map(HealthSession::getTimeMusic)
                    .collect(Collectors.toList())
                    .stream()
                    .map(m -> m.values()
                            .stream()
                            .mapToLong(Long::longValue)
                            .sum())
                    .collect(Collectors.toList())
                    .stream()
                    .mapToLong(Long::longValue)
                    .sum();

            // order by day and get labels
            long calcTime = totalTime - musicTime;
            barEntries.add(new BarEntry(count, new float[]{musicTime, calcTime}, list));
            barLabels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));

            count++;
        }

        // create dataset with colors
        BarDataSet barDataSet = new BarDataSet(barEntries, "App used");
        barDataSet.setColors(getThemeSecondaryColor(requireContext()), getThemePrimaryColor(requireContext()));
        barDataSet.setValueTextColor(getThemeOnPrimaryTextColor(requireContext()));
        barDataSet.setStackLabels(new String[]{"Music", "Usage"});
//        barDataSet.setValueTextColors(Arrays.asList(getThemeOnPrimaryTextColor(requireContext()), getThemeTextColor(requireContext())));
        barDataSet.setHighlightEnabled(true);
        barDataSet.setLabel("");
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarStackedLabel(float value, BarEntry stackedEntry) {
                if (value == 0) return "";
                return getDurationFormat((long) value);
            }
        });

        // setup axis description
        XAxis xAxis = this.mAppUsedBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(barLabels.size());
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(barLabels));

        YAxis yAxis = this.mAppUsedBarChart.getAxisLeft();
        yAxis.setLabelCount(5);
        yAxis.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String getFormattedValue(float value) {
                return getDurationFormat((long) value);
            }
        });

        // add data
        return barDataSet;
    }

    private BarDataSet getAppOpenedBarDataSet(List<HealthSession> healthSessions) {
        // Usage chart
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> barLabels = new ArrayList<>();

        // Get session of each day
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(1L).plusDays(1L);

        // check last weeks dates if sessions were created
        int count = 0;
        for (LocalDate date = start; date.isBefore(end) || date.isEqual(end); date = date.plusDays(1)) {
            LocalDate finalDate = date;

            // find session of each day in the last week
            List<HealthSession> list = healthSessions.stream()
                    .filter(healthSession -> convertToLocalDate(healthSession.getDate()).isEqual(finalDate))
                    .collect(Collectors.toList());

            // order by day and get labels
            barEntries.add(new BarEntry(count, list.size(), list));
            barLabels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));

            count++;
        }

        // create dataset with colors
        BarDataSet barDataSet = new BarDataSet(barEntries, "App opened");
        barDataSet.setColor(getThemePrimaryColor(requireContext()));
        barDataSet.setValueTextColor(getThemeOnPrimaryTextColor(requireContext()));
//        barDataSet.setHighLightColor(getThemeAccentColor(requireContext()));
        barDataSet.setHighlightEnabled(true);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                if (barEntry.getY() == 0) return "";
                return String.valueOf(Math.round(barEntry.getY()));
            }
        });

        // setup axis description
        XAxis xAxis = this.mAppOpenedBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(barLabels.size());
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(barLabels));

        // add data
        return barDataSet;
    }

    private void setupAppUsedChart() {
        this.mAppUsedBarChart.getDescription().setEnabled(false);
        this.mAppUsedBarChart.getAxisRight().setEnabled(false);
        this.mAppUsedBarChart.getAxisLeft().setAxisMinimum(0);
        this.mAppUsedBarChart.setDrawBarShadow(false);
        this.mAppUsedBarChart.setDrawValueAboveBar(false);
//        this.mAppUsedBarChart.getLegend().setEnabled(false);
        this.mAppUsedBarChart.setScaleEnabled(false);
        int textColor = getThemeTextColor(requireContext());
        this.mAppUsedBarChart.getAxisLeft().setTextColor(textColor);
        this.mAppUsedBarChart.getXAxis().setTextColor(textColor);
        this.mAppUsedBarChart.getLegend().setTextColor(textColor);
        this.mAppUsedBarChart.getDescription().setTextColor(textColor);

        this.mAppUsedBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                BarEntry barEntry = (BarEntry) e;
                String duration = getDurationFormat((long) barEntry.getY());
                Toast.makeText(requireContext(), "The app was open for " + duration, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void setupAppOpenedChart() {
        this.mAppOpenedBarChart.getDescription().setEnabled(false);
        this.mAppOpenedBarChart.getAxisRight().setEnabled(false);
        this.mAppOpenedBarChart.getAxisLeft().setAxisMinimum(0);
        this.mAppOpenedBarChart.setDrawBarShadow(false);
        this.mAppOpenedBarChart.setDrawValueAboveBar(false);
        this.mAppOpenedBarChart.getLegend().setEnabled(false);
        this.mAppOpenedBarChart.setScaleEnabled(false);
        int textColor = getThemeTextColor(requireContext());
        this.mAppOpenedBarChart.getAxisLeft().setTextColor(textColor);
        this.mAppOpenedBarChart.getXAxis().setTextColor(textColor);
        this.mAppOpenedBarChart.getLegend().setTextColor(textColor);
        this.mAppOpenedBarChart.getDescription().setTextColor(textColor);

        this.mAppOpenedBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                BarEntry barEntry = (BarEntry) e;
                int amount = Math.round(barEntry.getY());
                if (amount == 1) {
                    Toast.makeText(requireContext(), "You have opened the app only once.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "You have opened the app " + Math.round(barEntry.getY()) + " times.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    public static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

    public static int getThemePrimaryColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        return value.data;
    }

    public static int getThemeSecondaryColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorSecondary, value, true);
        return value.data;
    }

    public int getThemeTextColor(final Context context) {
        boolean darkMode = context.getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.dark_mode_key), false);
        if (darkMode) {
            return context.getColor(R.color.white);
        }
        return context.getColor(R.color.black);
    }

    public int getThemeOnPrimaryTextColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorOnPrimary, value, true);
        return value.data;
    }

    public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return LocalDateTime.ofInstant(
                dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @SuppressLint("DefaultLocale")
    private String getDurationFormat(long value) {
        int seconds = (int) (value / 1000) % 60;
        int minutes = (int) ((value / (1000 * 60)) % 60);
        int hours = (int) ((value / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mHealthSessionLiveData != null) {
            this.mHealthSessionLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}