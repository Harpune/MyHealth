package de.dbis.myhealth.ui.settings;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.util.GoogleFitConnector;
import de.dbis.myhealth.util.SpotifyConnector;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;

    // Actitivy
    private MainActivity mMainActivity;

    // View Model
    private SettingsViewModel settingsViewModel;

    // Preferences
    private SwitchPreference darkModePreference;
    private ListPreference themePreference;
    private CheckBoxPreference googleFitPreference;
    private CheckBoxPreference spotifyPreference;

    // Services
    private SpotifyConnector mSpotifyConnector;
    private GoogleFitConnector mGoogleFitConnector;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.mMainActivity = (MainActivity) getActivity();

        this.mSpotifyConnector = new SpotifyConnector(mMainActivity);
        this.mGoogleFitConnector = new GoogleFitConnector(mMainActivity);

        this.setupDarkMode();
        this.setupTheme();
        this.setupGoogleFit();
        this.setupSpotify();
    }

    private void setupDarkMode() {
        this.darkModePreference = findPreference(getString(R.string.dark_mode_key));
        if (this.darkModePreference != null) {
            this.darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            });
        }
    }

    private void setupTheme() {
        this.themePreference = findPreference(getString(R.string.theme_key));
        if (this.themePreference != null) {
            this.themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.recreate();
                }
                return true;
            });
        }
    }

    private void setupGoogleFit() {
        this.googleFitPreference = findPreference(getString(R.string.google_fit_key));
        if (this.googleFitPreference != null) {
            this.googleFitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
        this.spotifyPreference = findPreference(getString(R.string.spotify_key));
        if (this.spotifyPreference != null) {
            this.spotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
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