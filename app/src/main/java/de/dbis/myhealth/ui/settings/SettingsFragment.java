package de.dbis.myhealth.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static String TAG = "SettingsFragment";

    // View Model
    private QuestionnairesViewModel mQuestionnairesViewModel;

    // Services
    private SettingsViewModel mSettingsViewModel;
    private GoogleFitConnector mGoogleFitConnector;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(ApplicationConstants.PREFERENCES);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        setPreferencesFromResource(R.xml.preferences, rootKey);


        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mSettingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        this.mGoogleFitConnector = new GoogleFitConnector(requireActivity());

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
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Restart")
                        .setCancelable(false)
                        .setMessage("To apply the new design, the app has to restart.")
                        .setPositiveButton("Restart", (dialogInterface, i) -> {
                            // get result
                            Boolean enabled = (Boolean) newValue;

                            // update preference manually
                            requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean(getString(R.string.dark_mode_key), enabled)
                                    .apply();

                            // reload
                            if (enabled) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return false;
            });
        }
    }

    private void setupTheme() {
        ListPreference themePreference = findPreference(getString(R.string.theme_key));
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Restart")
                        .setCancelable(false)
                        .setMessage("To apply the new design, the app has to restart.")
                        .setPositiveButton("Restart", (dialogInterface, i) -> {
                            // update preference manually
                            requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                                    .edit()
                                    .putString(getString(R.string.theme_key), (String) newValue)
                                    .apply();
                            themePreference.setDefaultValue(newValue);

                            // recreate
                            requireActivity().recreate();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return false;
            });
        }
    }

    private void setupQuestionnaire() {
        ListPreference fastStartQuestionnaire = findPreference(getString(R.string.questionnaire_fast_start_key));
        this.mQuestionnairesViewModel.getQuestionnaires().observe(requireActivity(), questionnaires -> {

            if (fastStartQuestionnaire != null) {
                // setup entries
                String[] entries = questionnaires.stream().map(Questionnaire::getTitle).toArray(String[]::new);
                fastStartQuestionnaire.setEntries(entries);

                // setup entry values
                String[] entryValue = questionnaires.stream().map(Questionnaire::getId).toArray(String[]::new);
                fastStartQuestionnaire.setEntryValues(entryValue);
                fastStartQuestionnaire.notifyDependencyChange(true);

            }
        });
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
        // Get tracks
        ListPreference trackListPreference = findPreference(getString(R.string.current_spotify_track_key));
        if (trackListPreference != null) {
            this.mSettingsViewModel.getAllSpotifyTracks().observe(requireActivity(), spotifyTracks -> {
                // setup entries
                String[] entries = spotifyTracks.stream()
                        .map(spotifyTrack -> spotifyTrack.getTrack().name)
                        .toArray(String[]::new);

                trackListPreference.setEntries(entries);

                // setup values
                String[] values = spotifyTracks.stream()
                        .map(SpotifyTrack::getTrackId)
                        .toArray(String[]::new);

                trackListPreference.setEntryValues(values);

            });
        }

        EditTextPreference addSpotifyPreference = findPreference(getString(R.string.spotify_add));
        if (addSpotifyPreference != null) {
            addSpotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String trackId = (String) newValue;
                if (trackId != null && trackId.length() > 0) {
                    // uri and id possible
                    String[] split = trackId.trim().split(":");
                    String[] reversed = this.reverse(split);
                    String id = reversed[0];

                    // load track from web-api
                    this.mSettingsViewModel.loadSpotifyTrack(id).observe(this, spotifyTrack -> {
                        if (spotifyTrack != null) {
                            this.mSettingsViewModel.save(spotifyTrack);
                            Toast.makeText(
                                    requireContext(),
                                    "New track " + spotifyTrack.getTrack().name + " of " + spotifyTrack.getTrack().artists.get(0) + " added.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
                    return true;
                } else {
                    Toast.makeText(
                            requireContext(),
                            "Please enter a ID or URI of a Spotify-Track.",
                            Toast.LENGTH_LONG
                    ).show();
                    return false;
                }
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