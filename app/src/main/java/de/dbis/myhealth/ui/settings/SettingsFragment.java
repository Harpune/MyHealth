package de.dbis.myhealth.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.util.GoogleFitConnector;
import de.dbis.myhealth.util.SpotifyConnector;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;

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
        MainActivity mainActivity = (MainActivity) getActivity();

        this.mSpotifyConnector = new SpotifyConnector(mainActivity);
        this.mGoogleFitConnector = new GoogleFitConnector(mainActivity);

        this.setupDarkMode();
        this.setupTheme();
        this.setupGoogleFit();
        this.setupSpotify();
    }

    private void setupDarkMode() {
        this.darkModePreference = findPreference(getString(R.string.dark_mode_key));
        this.darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            return true;
        });
    }

    private void setupTheme() {
        this.themePreference = findPreference(getString(R.string.theme_key));
        this.themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.recreate();
            }
            return true;
        });
    }

    private void setupGoogleFit() {
        this.googleFitPreference = findPreference(getString(R.string.google_fit_key));
        this.googleFitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                this.mGoogleFitConnector.connect();
            } else {
                this.mGoogleFitConnector.disconnect();
            }
            return true;
        });
    }

    private void setupSpotify() {
        // Connect on listener
        this.spotifyPreference = findPreference(getString(R.string.spotify_key));
        this.spotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                this.mSpotifyConnector.connect();
            } else {
                this.mSpotifyConnector.disconnect();
            }
            return true;
        });
    }
}