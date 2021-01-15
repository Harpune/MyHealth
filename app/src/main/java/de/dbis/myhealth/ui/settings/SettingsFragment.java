package de.dbis.myhealth.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static String TAG = "SettingsFragment";
    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;

    // Actitivy
    private MainActivity mMainActivity;

    // View Model
    private QuestionnairesViewModel mQuestionnairesViewModel;

    // Services
    private SettingsViewModel mSettingsViewModel;
    private GoogleFitConnector mGoogleFitConnector;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.mMainActivity = (MainActivity) getActivity();
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);

        // TODO do adding tracks, playing tracks over repository + viewmodel
        this.mSettingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
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
                    this.mSettingsViewModel.setupSpotifyData().observe(this, spotifyData -> {
                        if (spotifyData != null) {
                            this.mSettingsViewModel.connect(spotifyData);
                        }
                    });
                } else {
                    this.mSettingsViewModel.disconnect();
                }
                return true;
            });
        }


        // Get tracks
        ListPreference playlistPreference = findPreference(getString(R.string.spotify_playlist_key));
        if (playlistPreference != null) {
            playlistPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, newValue.toString());
                mSettingsViewModel.getSpotifyTrackById(newValue.toString()).observe(this, spotifyTrack -> {
                    mSettingsViewModel.setCurrentSpotifyTrack(spotifyTrack);
                });
                return true;
            });

            this.mSettingsViewModel.getAllSpotifyTracks().observe(requireActivity(), spotifyTracks -> {
                // setup entries
                String[] entries = spotifyTracks.stream()
                        .map(spotifyTrack -> spotifyTrack.getTrack().name)
                        .toArray(String[]::new);

                playlistPreference.setEntries(entries);

                // setup values
                String[] values = spotifyTracks.stream()
                        .map(SpotifyTrack::getTrackId)
                        .toArray(String[]::new);

                playlistPreference.setEntryValues(values);
            });
        }

        Preference addSpotifyPreference = findPreference(getString(R.string.spotify_add));
        if (addSpotifyPreference != null) {
            addSpotifyPreference.setOnPreferenceClickListener(preference -> {
                final EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setMaxLines(1);
                input.setText("2dxyTLxvO1xzHSD6fDafwZ");

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                        .setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_add_24))
                        .setTitle("Add id to add")
                        .setMessage("Instructions on how to get id")
                        .setView(input)
                        .setPositiveButton(getString(R.string.save), (dialogInterface, i) -> {
                            String text = input.getText().toString();
                            String[] splitted = text.split(":");
                            String[] reversed = this.reverse(splitted);
                            String id = reversed[0];


                            this.mSettingsViewModel.loadTrack(id);

                        });
                // spotify:track:2dxyTLxvO1xzHSD6fDafwZ
                // spotify:track:1raWfcURBd1Q3W3K0ojDCM
                // spotify:track:2kX0ubVLuWiFn1fAmPNB7V
                builder.show();
                return true;
            });
        }
    }

    private String[] reverse(String[] strings) {
        for (int i = 0; i < strings.length / 2; i++) {
            String temp = strings[i];
            strings[i] = strings[strings.length - i - 1];
            strings[strings.length - i - 1] = temp;
        }
        return strings;
    }
}