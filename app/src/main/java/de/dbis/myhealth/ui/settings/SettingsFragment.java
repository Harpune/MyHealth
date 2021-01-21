package de.dbis.myhealth.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

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
        this.mMainActivity = (MainActivity) requireActivity();
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
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

            if (fastStartQuestionnaire != null) {
                // setup entries
                String[] entries = questionnaires.stream().map(Questionnaire::getTitle).toArray(String[]::new);
                fastStartQuestionnaire.setEntries(entries);

                // setup entry values
                String[] entryValue = questionnaires.stream().map(Questionnaire::getId).toArray(String[]::new);
                fastStartQuestionnaire.setEntryValues(entryValue);

            }
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
//        // Connect on listener
//        CheckBoxPreference spotifyPreference = findPreference(getString(R.string.spotify_key));
//        if (spotifyPreference != null) {
//            spotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
//                if ((Boolean) newValue) {
//                    this.mMainActivity.tryConnectToSpotify();
//                } else {
//                    this.mSettingsViewModel.disconnect();
//                }
//                return true;
//            });
//        }


        // Get tracks
        ListPreference playlistPreference = findPreference(getString(R.string.current_spotify_track_key));
        if (playlistPreference != null) {
            playlistPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, newValue.toString());
                mSettingsViewModel.getSpotifyTrackById(newValue.toString()).observe(this.mMainActivity, spotifyTrack -> {
                    mSettingsViewModel.setCurrentSpotifyTrack(spotifyTrack);
                });
                return true;
            });

            this.mSettingsViewModel.getAllSpotifyTracks().observe(this.mMainActivity, spotifyTracks -> {
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this.mMainActivity)
                        .setIcon(ContextCompat.getDrawable(this.mMainActivity, R.drawable.ic_baseline_add_24))
                        .setTitle("Add new song")
                        .setMessage("Paste the song URI or ID to add")
                        .setView(input)
                        .setPositiveButton(getString(R.string.save), (dialogInterface, i) -> {
                            String text = input.getText().toString();

                            // uri and id possible
                            String[] split = text.split(":");
                            String[] reversed = this.reverse(split);
                            String id = reversed[0];

                            // load track from web-api
                            this.mSettingsViewModel.loadSpotifyTrack(id).observe(this, spotifyTrack -> {
                                Log.d(TAG, spotifyTrack.toString());
//                                this.mSettingsViewModel.play(spotifyTrack);
                            });

                        });
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