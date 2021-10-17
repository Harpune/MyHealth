package de.dbis.myhealth.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.HomeAdapter;
import de.dbis.myhealth.databinding.FragmentHomeBinding;
import de.dbis.myhealth.models.Gamification;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.ui.spotify.SpotifyViewModel;
import de.dbis.myhealth.ui.stats.GamificationViewModel;
import de.dbis.myhealth.ui.stats.StatsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;

public class HomeFragment extends Fragment {
    private final static String TAG = "HomeFragment";

    private SharedPreferences mSharedPreferences;
    private Preference mPreference;
    private HomeAdapter mHomeAdapter;

    // View Models
    private HomeViewModel mHomeViewModel;
    private SpotifyViewModel mSpotifyViewModel;
    private UserViewModel mUserViewModel;
    private StatsViewModel mStatsViewModel;
    private QuestionnairesViewModel mQuestionnairesViewModel;
    private GamificationViewModel mGamificationViewModel;

    // LiveData
    private LiveData<Bitmap> mCurrentTrackImageLiveData;
    private LiveData<List<HealthSession>> mAllHealthSessions;
    private LiveData<HealthSession> mCurrentHealthSession;
    private LiveData<List<Gamification>> mGamifications;

    // Views
    private ShapeableImageView mTrackImageView;

    private final View.OnClickListener mFabClickListener = this::openQuestionnaire;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // shared preferences
        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
        this.mPreference = PowerPreference.getDefaultFile();

        // view models
        this.mHomeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        this.mSpotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mStatsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        this.mGamificationViewModel = new ViewModelProvider(requireActivity()).get(GamificationViewModel.class);

        // bindings
        FragmentHomeBinding mFragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        mFragmentHomeBinding.setLifecycleOwner(getViewLifecycleOwner());
        mFragmentHomeBinding.setSpotifyViewModel(this.mSpotifyViewModel);
        mFragmentHomeBinding.setHomeFragment(this);
        mFragmentHomeBinding.setSpotifyEnabled(this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false));
        mFragmentHomeBinding.setGreetingEnabled(this.mSharedPreferences.getBoolean(getString(R.string.general_greeting_key), false));
        mFragmentHomeBinding.setMessage(this.getWelcomeMessage());

        // live data
        this.mCurrentTrackImageLiveData = this.mSpotifyViewModel.getCurrentTrackImage();
        this.mCurrentHealthSession = this.mStatsViewModel.getCurrentHealthSession();
        this.mAllHealthSessions = this.mStatsViewModel.getAllHealthSessions();
        this.mGamifications = this.mGamificationViewModel.getGamifications();

        // set fab action in activity
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        // views
        View root = mFragmentHomeBinding.getRoot();
        this.mTrackImageView = root.findViewById(R.id.spotifyTrackIcon);

        // Recyclerview
        RecyclerView recyclerView = root.findViewById(R.id.home_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mHomeAdapter = new HomeAdapter(requireActivity());
        recyclerView.setAdapter(this.mHomeAdapter);

        return root;
    }

    /**
     * Click on Fab in HomeFragment: FastStart Questionnaiere.
     *
     * @param view View of FAB
     */
    private void openQuestionnaire(View view) {
        this.mQuestionnairesViewModel.getAllQuestionnaires().observe(getViewLifecycleOwner(), questionnaires -> {
            String questionnairePref = this.mSharedPreferences.getString(getString(R.string.questionnaire_fast_start_key), null);
            if (questionnairePref == null) {
                Toast.makeText(getContext(), getString(R.string.set_fast_start_in_settings), Toast.LENGTH_LONG).show();
            } else if (questionnaires == null) {
                Toast.makeText(getContext(), getString(R.string.no_questionnaire_available), Toast.LENGTH_LONG).show();
            } else {
                Optional<Questionnaire> optionalQuestionnaire = questionnaires.stream().filter(tmp -> tmp.getId().equalsIgnoreCase(questionnairePref)).findFirst();
                if (optionalQuestionnaire.isPresent()) {
                    Questionnaire questionnaire = optionalQuestionnaire.get();
                    QuestionnaireSetting questionnaireSetting = this.mPreference.getObject(
                            questionnaire.getId(),
                            QuestionnaireSetting.class,
                            new QuestionnaireSetting(questionnaire.getId(), new ArrayList<>()));

                    this.mQuestionnairesViewModel.setQuestionnaireSetting(questionnaireSetting);
                    this.mQuestionnairesViewModel.select(optionalQuestionnaire.get());

                    if (this.mSharedPreferences.getBoolean(getString(R.string.questionnaire_chat_key), false)) {
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_chat_item);
                    } else {
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.nav_questionnaire);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.could_not_find_questionnaire), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Generates Welcome message depending on time of the day.
     *
     * @return message
     */
    private String getWelcomeMessage() {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int coffee = 0x2615;
        int sun = 0x1F31E;
        int greeting = 0x1F64B;
        int moon = 0x1F31C;

        if (timeOfDay < 12) {
            return getString(R.string.good_morning) + " " + getEmojiByUnicode(coffee);
        } else if (timeOfDay < 16) {
            return getString(R.string.good_afternoon) + " " + getEmojiByUnicode(sun);
        } else if (timeOfDay < 21) {
            return getString(R.string.good_evening) + " " + getEmojiByUnicode(greeting);
        } else {
            return getString(R.string.good_night) + " " + getEmojiByUnicode(moon);
        }
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onStart() {
        super.onStart();

        // observe current spotify track image
        this.mCurrentTrackImageLiveData.observe(getViewLifecycleOwner(), image -> {
            if (image != null) {
                this.mTrackImageView.setImageBitmap(image);
            }
        });

        this.mAllHealthSessions.observe(getViewLifecycleOwner(), this::handleHealthSessions);
        this.mGamifications.observe(getViewLifecycleOwner(), gamifications -> this.mHomeAdapter.setData(gamifications));

    }


    @Override
    public void onStop() {
        super.onStop();

        if (this.mCurrentTrackImageLiveData != null && this.mCurrentTrackImageLiveData.hasObservers()) {
            this.mCurrentTrackImageLiveData.removeObservers(getViewLifecycleOwner());
        }

        if (this.mAllHealthSessions != null && this.mAllHealthSessions.hasObservers()) {
            this.mAllHealthSessions.removeObservers(getViewLifecycleOwner());
        }

        if (this.mCurrentHealthSession != null && this.mCurrentHealthSession.hasObservers()) {
            this.mCurrentHealthSession.removeObservers(getViewLifecycleOwner());
        }

        if (this.mGamifications != null && this.mGamifications.hasObservers()) {
            this.mGamifications.removeObservers(getViewLifecycleOwner());
        }
    }

    private void handleHealthSessions(List<HealthSession> healthSessions) {
        this.mGamificationViewModel.matchWithHealthSessions(healthSessions);
    }

    public void openSpotifyTrackInfoFragment(View view) {
        // Nav to questionnaire
        Navigation.findNavController(view).navigate(R.id.action_nav_home_item_to_spotifyFragment);
    }
}