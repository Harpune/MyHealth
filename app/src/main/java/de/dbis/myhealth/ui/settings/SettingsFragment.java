package de.dbis.myhealth.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
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

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat {

    // finals
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";
    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;
    private final static String[] SLEEP_STAGE_NAMES = {"Unused", "Awake (during sleep)", "Sleep", "Out-of-bed", "Light sleep", "Deep sleep", "REM sleep"};


    // Preferences
    private SwitchPreference darkModePreference;
    private ListPreference themePreference;
    private CheckBoxPreference googleFitPreference;
    private CheckBoxPreference spotifyPreference;

    // Spotify
    private SpotifyAppRemote mSpotifyRemoteApp;
    private ConnectionParams mConnectionParams;
    private Connector.ConnectionListener mConnectionListener;

    // Google Fit
    private FitnessOptions mFitnessOptions;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        MainActivity mainActivity = (MainActivity) getActivity();

        this.setupDarkMode();
        this.setupTheme();
        this.setupGoogleFit();
        this.setupSpotify();
    }

    private void setupDarkMode() {
        this.darkModePreference = findPreference(getString(R.string.dark_mode_key));
        this.darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("Preference", "" + newValue);
            if ((Boolean) newValue) {
                Log.d("Preference", "1" + newValue);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                Log.d("Preference", "2" + newValue);
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
        this.mFitnessOptions = FitnessOptions.builder()
                .accessSleepSessions(FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();
        this.googleFitPreference = findPreference(getString(R.string.google_fit_key));
        this.googleFitPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    accessSleepingData();
                }
                return true;
            }
        });

    }

    private void setupSpotify() {
        this.mConnectionParams =
                new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
                        .showAuthView(true)
                        .setRedirectUri(SPOTIFY_REDIRECT_URI)
                        .build();

        this.mConnectionListener = new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                spotifyPreference.setChecked(true);
                mSpotifyRemoteApp = spotifyAppRemote;
                Log.d("SettingsFragment", "Connected to Spotify!");
            }

            @Override
            public void onFailure(Throwable throwable) {
                spotifyPreference.setChecked(false);
                if (throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException) {
                    openSpotifyLoginDialog();
                } else if (throwable instanceof CouldNotFindSpotifyApp) {
                    openDownloadSpotifyDialog();
                } else {
                    Log.e("MainActivity", throwable.getMessage(), throwable);
                }
            }
        };

        // Connect on listener
        this.spotifyPreference = findPreference(getString(R.string.spotify_key));
        this.spotifyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                SpotifyAppRemote.connect(getContext(), this.mConnectionParams, this.mConnectionListener);
            } else {
                SpotifyAppRemote.disconnect(this.mSpotifyRemoteApp);
            }
            return true;
        });
    }


    private void accessSleepingData() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(getContext(), this.mFitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, this.mFitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    account,
                    this.mFitnessOptions);
            return;
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        SessionsClient sessionsClient = Fitness.getSessionsClient(getContext(), account);

        SessionReadRequest request = new SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                .includeSleepSessions()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeInterval(startSeconds, endSeconds, TimeUnit.MILLISECONDS)
                .build();

        sessionsClient.readSession(request)
                .addOnSuccessListener(response -> {
                    Log.d("Google Fitness", "Success");
                    response.getSessions().forEach(session -> {
                        long sessionStart = session.getStartTime(TimeUnit.MILLISECONDS);
                        long sessionEnd = session.getEndTime(TimeUnit.MILLISECONDS);
                        Log.d("GoogleFit", "Sleep between " + sessionStart + "  and " + sessionEnd);
                        response.getDataSet(session).forEach(dataSet -> {
                            dataSet.getDataPoints().forEach(dataPoint -> {
                                int sleepStageVal = dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt();
                                String sleepStage = SLEEP_STAGE_NAMES[sleepStageVal];
                                long segmentStart = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                                long segmentEnd = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
                                Log.d("GoogleFit", "Type " + sleepStage + " between " + segmentStart + " and " + segmentEnd);
                            });
                        });
                    });
                })
                .addOnFailureListener(error -> Log.d("GoogleFit", "OnFailure()", error));
    }

    private void openSpotifyLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialog, i) -> {
                    dialog.dismiss();
                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .edit()
                            .putBoolean(getString(R.string.spotify_key), false)
                            .apply();
                    this.spotifyPreference.setChecked(false);
                });

        builder.show();
    }

    private void openDownloadSpotifyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Spotify")
                .setMessage("To include a some music you can download Spotify")
                .setPositiveButton("Download", (dialog, i) -> {
                    dialog.dismiss();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));
                    } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .edit()
                            .putBoolean(getString(R.string.spotify_key), false)
                            .apply();
                    this.spotifyPreference.setChecked(false);
                });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                this.accessSleepingData();
            }
        } else {
            Toast.makeText(getContext(), "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SpotifyAppRemote.disconnect(this.mSpotifyRemoteApp);
        if (this.spotifyPreference.isChecked()) {
            SpotifyAppRemote.connect(getContext(), this.mConnectionParams, this.mConnectionListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SpotifyAppRemote.disconnect(this.mSpotifyRemoteApp);
    }
}