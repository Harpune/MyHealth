package de.dbis.myhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final static String[] SLEEP_STAGE_NAMES = {"Unused", "Awake (during sleep)", "Sleep", "Out-of-bed", "Light sleep", "Deep sleep", "REM sleep"};
    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    // Android
    private AppBarConfiguration mAppBarConfiguration;

    // Spotify
    private SpotifyAppRemote mSpotifyRemoteApp;
    private ConnectionParams mConnectionParams;
    private Connector.ConnectionListener mConnectionListener;

    // Google Fit
    private final FitnessOptions mFitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build();

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initDrawerLayout();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Snackbar.make(view, "hello", Snackbar.LENGTH_SHORT).show();
        });

        this.context = this;
        this.connectGoogleFit();
        this.connectSpotify();
    }

    private void connectSpotify() {
        this.mConnectionParams =
                new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
                        .showAuthView(true)
                        .setRedirectUri(SPOTIFY_REDIRECT_URI)
                        .build();

        this.mConnectionListener = new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyRemoteApp = spotifyAppRemote;
                Log.d("MainActivity", "Connected! Yay!");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("MainActivity", throwable.getMessage(), throwable);
                if (throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException) {
                    openSpotifyLoginDialog();
                } else if (throwable instanceof CouldNotFindSpotifyApp) {
                    openDownloadSpotifyDialog();
                }
            }
        };
        SpotifyAppRemote.connect(this, this.mConnectionParams, this.mConnectionListener);
    }

    private void openSpotifyLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialog, i) -> {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean(getString(R.string.showSpotifyLoginDialog), true)) {
            builder.show();
            sharedPreferences.edit()
                    .putBoolean(getString(R.string.showSpotifyLoginDialog), false)
                    .apply();
        }
    }

    private void openDownloadSpotifyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Spotify")
                .setMessage("To include a some music you can download Spotify")
                .setPositiveButton("Download", (dialog, i) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean(getString(R.string.showSpotifyDownloadDialog), true)) {
            builder.show();
            sharedPreferences.edit()
                    .putBoolean(getString(R.string.showSpotifyDownloadDialog), false)
                    .apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                this.accessGoogleFit();
            }
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void accessGoogleFit() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, this.mFitnessOptions);
        SessionsClient sessionsClient = Fitness.getSessionsClient(this, account);

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

    private void connectGoogleFit() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, this.mFitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, this.mFitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    account,
                    this.mFitnessOptions);
        } else {
            this.accessGoogleFit();
        }
    }

    private void initDrawerLayout() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_questionnaires, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomAppBar, navController, mAppBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String currentTheme = sharedPreferences.getString(getString(R.string.theme_key), getString(R.string.green_theme_key));
        if (currentTheme.equalsIgnoreCase(getString(R.string.green_theme_key))) {
            theme.applyStyle(R.style.Theme_Green, true);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.blue_theme_key))) {
            theme.applyStyle(R.style.Theme_Blue, true);
        }

        boolean darkMode = sharedPreferences.getBoolean(getString(R.string.dark_mode_key), false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        return theme;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SpotifyAppRemote.disconnect(mSpotifyRemoteApp);
        SpotifyAppRemote.connect(this, this.mConnectionParams, this.mConnectionListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyRemoteApp);
    }
}