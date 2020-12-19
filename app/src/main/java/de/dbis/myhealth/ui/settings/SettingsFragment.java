package de.dbis.myhealth.ui.settings;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;
import de.dbis.myhealth.util.SpotifyConnector;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;

    // Actitivy
    private MainActivity mMainActivity;

    // View Model
    private QuestionnairesViewModel mQuestionnairesViewModel;

    // Services
    private SpotifyConnector mSpotifyConnector;
    private GoogleFitConnector mGoogleFitConnector;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.mMainActivity = (MainActivity) getActivity();
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);


        this.mSpotifyConnector = new SpotifyConnector(this.mMainActivity);
        this.mGoogleFitConnector = new GoogleFitConnector(this.mMainActivity);

        this.setupDarkMode();
        this.setupQuestionnaire();
        this.setupTheme();
        this.setupGoogleFit();
        this.setupSpotify();
    }

    private void setupDarkMode() {
        // Preferences
        SwitchPreference darkModePreference = findPreference(getString(R.string.dark_mode_key));
        if (darkModePreference != null) {
            darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            });
        }
    }

    private void setupQuestionnaire() {
        ListPreference fastStartQuestionnaire = findPreference(getString(R.string.questionnaire_fast_start_key));
        this.mQuestionnairesViewModel.getQuestionnaires().observe(this.mMainActivity, questionnaires -> {

            // setup entries
            String[] entries = questionnaires.stream().map(Questionnaire::getTitle).toArray(String[]::new);
            fastStartQuestionnaire.setEntries(entries);

            // setup entry values
            String[] entryValue = questionnaires.stream().map(Questionnaire::getId).toArray(String[]::new);
            fastStartQuestionnaire.setEntryValues(entryValue);
        });
    }

    private void setupTheme() {
        ListPreference themePreference = findPreference(getString(R.string.theme_key));
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                this.mMainActivity.recreate();
                return true;
            });
        }
    }

    private void setupGoogleFit() {
        CheckBoxPreference googleFitPreference = findPreference(getString(R.string.google_fit_key));
        if (googleFitPreference != null) {
            googleFitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    this.mGoogleFitConnector.connect();
                } else {
                    this.mGoogleFitConnector.disconnect();
                }
                return true;
            });
        }
    }

    private void setupSpotify() {
        // Connect on listener
        CheckBoxPreference spotifyPreference = findPreference(getString(R.string.spotify_key));
        if (spotifyPreference != null) {
            spotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    this.mSpotifyConnector.connect();
                } else {
                    this.mSpotifyConnector.disconnect();
                    this.mMainActivity.showMusicIcon(false);
                }
                return true;
            });
        }
    }
}