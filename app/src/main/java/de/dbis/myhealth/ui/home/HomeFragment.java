package de.dbis.myhealth.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.HomeAdapter;
import de.dbis.myhealth.databinding.FragmentHomeBinding;
import de.dbis.myhealth.models.Gamification;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.ui.settings.SpotifyViewModel;
import de.dbis.myhealth.ui.stats.StatsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;

public class HomeFragment extends Fragment {
    private final static String TAG = "HomeFragment";

    private FragmentHomeBinding mFragmentHomeBinding;
    private SharedPreferences mSharedPreferences;
    private HomeAdapter mHomeAdapter;

    // View Models
    private HomeViewModel mHomeViewModel;
    private SpotifyViewModel mSpotifyViewModel;
    private UserViewModel mUserViewModel;
    private StatsViewModel mStatsViewModel;
    private QuestionnairesViewModel mQuestionnairesViewModel;

    // LiveData
    private LiveData<List<HealthSession>> mHealthSessionsLiveData;
    private LiveData<List<Gamification>> mGamificationsLiveData;

    private final View.OnClickListener mFabClickListener = this::openQuestionnaire;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // shared preferences
        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // view models
        this.mHomeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        this.mSpotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mStatsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // bindings
        this.mFragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        this.mFragmentHomeBinding.setLifecycleOwner(getViewLifecycleOwner());
        this.mFragmentHomeBinding.setSpotifyViewModel(this.mSpotifyViewModel);
        this.mFragmentHomeBinding.setMessage(this.getWelcomeMessage());

        // live data
        this.mHealthSessionsLiveData = this.mStatsViewModel.getHealthSessions();
        this.mGamificationsLiveData = this.mStatsViewModel.getGamifications();

        // set fab action in activity
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        // views
        View root = this.mFragmentHomeBinding.getRoot();

        RecyclerView recyclerView = root.findViewById(R.id.home_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        this.mHomeAdapter = new HomeAdapter(requireActivity(), getViewLifecycleOwner());
        recyclerView.setAdapter(this.mHomeAdapter);

        return root;
    }


    /**
     * Click on Fab in HomeFragment
     *
     * @param view View of FAB
     */
    private void openQuestionnaire(View view) {
        this.mQuestionnairesViewModel.getAllQuestionnaires().observe(getViewLifecycleOwner(), questionnaires -> {
            String questionnairePref = this.mSharedPreferences.getString(getString(R.string.questionnaire_fast_start_key), null);
            if (questionnairePref == null) {
                // TODO create dialog/snackbar to ask user to go to settings
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
    }

    private String getWelcomeMessage() {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int coffee = 0x2615;
        int sun = 0x1F31E;
        int greeting = 0x1F64B;
        int moon = 0x1F31C;

        if (timeOfDay < 12) {
            return "Good Morning " + getEmojiByUnicode(coffee);
        } else if (timeOfDay < 16) {
            return "Good Afternoon " + getEmojiByUnicode(sun);
        } else if (timeOfDay < 21) {
            return "Good Evening " + getEmojiByUnicode(greeting);
        } else {
            return "Good Night " + getEmojiByUnicode(moon);
        }
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onStart() {
        super.onStart();

        this.mHealthSessionsLiveData.observe(getViewLifecycleOwner(), healthSessions -> {
            Log.d(TAG, String.valueOf(healthSessions));
            this.mHomeAdapter.updateSessions();
        });

        this.mGamificationsLiveData.observe(getViewLifecycleOwner(), gamifications -> this.mHomeAdapter.setData(gamifications));

    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mHealthSessionsLiveData != null) {
            this.mHealthSessionsLiveData.removeObservers(getViewLifecycleOwner());
        }

        if (this.mGamificationsLiveData != null) {
            this.mGamificationsLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}