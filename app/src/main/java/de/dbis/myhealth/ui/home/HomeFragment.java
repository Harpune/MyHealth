package de.dbis.myhealth.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Calendar;
import java.util.Optional;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentHomeBinding;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class HomeFragment extends Fragment {
    private final static String TAG = "HomeFragment";

    private HomeViewModel mHomeViewModel;
    private SettingsViewModel mSettingsViewModel;
    private QuestionnairesViewModel mQuestionnairesViewModel;
    public SharedPreferences mSharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.mHomeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        this.mSettingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        FragmentHomeBinding fragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        fragmentHomeBinding.setLifecycleOwner(this);
        fragmentHomeBinding.setHomeViewModel(this.mHomeViewModel);
        fragmentHomeBinding.setSettingsViewModel(this.mSettingsViewModel);

        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        View root = fragmentHomeBinding.getRoot();

        return root;
    }

    private final View.OnClickListener mFabClickListener = view -> {
        this.mQuestionnairesViewModel.getQuestionnaires().observe(this, questionnaires -> {
            String questionnairePref = this.mSharedPreferences.getString(getString(R.string.questionnaire_fast_start_key), null);
            if (questionnairePref == null) {
                Toast.makeText(getContext(), "Set Questionnaire for fast access in Settings.", Toast.LENGTH_LONG).show();
            } else if (questionnaires == null) {
                Toast.makeText(getContext(), "No questionnaires are available.", Toast.LENGTH_LONG).show();
            } else {
                Optional<Questionnaire> questionnaire = questionnaires.stream().filter(tmp -> tmp.getId().equalsIgnoreCase(questionnairePref)).findFirst();
                if (questionnaire.isPresent()) {
                    this.mQuestionnairesViewModel.select(questionnaire.get());
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_questionnaire);
                } else {
                    Toast.makeText(getContext(), "Couldn't find selected questionnaire.", Toast.LENGTH_LONG).show();
                }
            }
        });
    };

    private void setupPieChart() {
//        this.pieChart.getDescription().setEnabled(false);
//        this.pieChart.getLegend().setEnabled(false);
//        this.pieChart.setCenterTextTypeface(Typeface.SANS_SERIF);
//        this.pieChart.setRotationEnabled(true);
//        this.pieChart.setCenterTextSize(20f);
//        this.pieChart.setDrawEntryLabels(false);
//        this.pieChart.setCenterText(this.getWelcomeMessage());
//        this.pieChart.setCenterTextColor(this.getTextColor());
//        this.pieChart.setHoleRadius(90f);
//        this.pieChart.setElevation(0f);
//        this.pieChart.setDrawMarkers(false);
//        this.pieChart.setHoleColor(Color.TRANSPARENT);
//        this.pieChart.setExtraOffsets(32, 32, 32, 32);
//        this.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
//            @Override
//            public void onValueSelected(Entry e, Highlight h) {
//                if (((PieEntry) e).getLabel().equals(getString(R.string.questionnaires_label))) {
//                    Toast.makeText(getContext(), "You already answered " + (int) e.getY() + " questionnaires.", Toast.LENGTH_SHORT).show();
//                }
//
//                if (((PieEntry) e).getLabel().equals(getString(R.string.music))) {
//                    Toast.makeText(getContext(), "You already listened to " + e.getY() + " minutes of music.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected() {
//
//            }
//        });

    }

    private void setupPieChartData() {
//        List<PieEntry> entries = new ArrayList<>();
//        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_home_24);
//
//        PieDataSet dataSet = new PieDataSet(entries, "");
//        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
//        dataSet.setDrawIcons(true);
//        dataSet.setIconsOffset(MPPointF.getInstance(0, 20f));
//        dataSet.setSliceSpace(5f);
//
//        PieData pieData = new PieData(dataSet);
//        this.pieChart.setData(pieData);
//        this.pieChart.invalidate();
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