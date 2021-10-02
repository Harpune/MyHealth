package de.dbis.myhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Set;

import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.dialogs.DownloadSpotifyDialog;
import de.dbis.myhealth.ui.dialogs.SpotifyLoginDialog;
import de.dbis.myhealth.ui.intro.IntroActivity;
import de.dbis.myhealth.ui.spotify.SpotifyViewModel;
import de.dbis.myhealth.ui.stats.StatsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;
import kaaes.spotify.webapi.android.SpotifyApi;

import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_CLIENT_ID;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REDIRECT_URI;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // View Models
    private StatsViewModel mStatsViewModel;
    public SpotifyViewModel mSpotifyViewModel;
    public UserViewModel mUserViewModel;

    // Views
    public FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    public CoordinatorLayout mCoordinatorLayout;

    // Android
    public SharedPreferences mSharedPreferences;
    private Preference mPreference;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;
    private MenuItem mSpotifyMenuItem;
    private AudioManager mAudioManager;

    // LiveData
    private LiveData<HealthSession> mHealthSessionLiveData;
    private LiveData<PlayerState> mPlayerStateLiveData;
    private LiveData<SpotifyAppRemote> mSpotifyAppRemoteLiveData;
    private LiveData<SpotifyTrack> mSpotifyTrackLiveData;
    private LiveData<FirebaseUser> mFirebaseUserLiveData;

    // Timer
    private StopWatch mStopWatch;
    private Handler mHandler;
    private final static long INTERVAL_UPDATE = 10000L;
    private final static long INTERVAL_DELAY = 2000L;

    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, INTERVAL_UPDATE);

            // increment app opened
            mStatsViewModel.incrementAppTime(INTERVAL_UPDATE);

            // increment spotify played
            boolean spotifyEnabled = mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
            if (spotifyEnabled) {
                String currentTrackId = mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
                if (currentTrackId != null) {
                    PlayerState playerState = mSpotifyViewModel.getPlayerState().getValue();
                    if (playerState != null && playerState.track != null && !playerState.isPaused) { // check player
                        Track currentTrack = playerState.track;
                        if (currentTrack != null && currentTrack.uri != null && currentTrack.uri.endsWith(currentTrackId)) {
                            mStatsViewModel.incrementMusicTime(currentTrackId, INTERVAL_UPDATE);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get settings and settings- db
        PowerPreference.init(this);
        this.mPreference = PowerPreference.getDefaultFile();
//        this.mPreference.clear();

        this.mSharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
//        this.mSharedPreferences.edit().clear().apply();

        if (!this.mSharedPreferences.getBoolean(getString(R.string.pref_on_boarding), false)) {
            startActivity(new Intent(this, IntroActivity.class));
        }


        // view models
        this.mSpotifyViewModel = new ViewModelProvider(this).get(SpotifyViewModel.class);
        this.mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        this.mStatsViewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        // navigation
        this.mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (this.mSharedPreferences.getBoolean(getString(R.string.general_start_questionnaire_key), false)) {
            NavGraph navGraph = this.mNavController.getGraph();
            navGraph.setStartDestination(R.id.nav_questionnaires_item);
            this.mNavController.setGraph(navGraph);
        }
        this.mCoordinatorLayout = findViewById(R.id.coordinator);
        this.mFab = findViewById(R.id.fab);
        this.initDrawerLayout();

        // timer
        this.mStopWatch = StopWatch.createStarted();
        this.mHandler = new Handler();

        // audio
        this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }


    /**
     * Gets update from each fragment to set individual clickListener
     *
     * @param mFabClickListener ClickListener of FAB
     */
    public void setFabClickListener(View.OnClickListener mFabClickListener) {
        this.mFab.setOnClickListener(mFabClickListener);
    }

    /**
     * Setup the DrawerLayout and handle the appearance of the fab.
     */
    private void initDrawerLayout() {
        // Get navigation views
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        this.mBottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(this.mBottomAppBar);

        this.mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_user_item, R.id.nav_home_item, R.id.nav_questionnaires_item, R.id.nav_stats_item, R.id.nav_intro_item, R.id.nav_settings_item, R.id.nav_information_item)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupWithNavController(navigationView, this.mNavController);
        NavigationUI.setupWithNavController(this.mBottomAppBar, this.mNavController, this.mAppBarConfiguration);

        // this.mNavController.getGraph().setStartDestination(R.id.nav_home_item);
        this.mNavController.getGraph().setStartDestination(R.id.nav_questionnaires_item);

        // check for current fragment
        this.mNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            HideBottomViewOnScrollBehavior<BottomAppBar> behavior = this.mBottomAppBar.getBehavior();

            // setup fab depending on destination
            if (destination.getId() == R.id.nav_home_item) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_send_24));
            } else if (destination.getId() == R.id.nav_questionnaire) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            } else if (destination.getId() == R.id.nav_user_item) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_save_24));
            } else {
                this.mFab.hide();
            }

            // setup bottomAppBar depending on destination
            if (destination.getId() == R.id.nav_questionnaire) {
                this.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            } else {
                this.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            }
            behavior.slideUp(this.mBottomAppBar);
        });
    }

    /**
     * Check if Spotify is enabled in sharedPreferences and setup is so.
     */
    public void startSetupSpotify() {
        boolean enabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
        this.setupSpotify(enabled);

        if (enabled) {
            int volume = this.getSpotifyVolume();
            this.setSpotifyVolume(volume);
        }

        String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
        this.setupSpotifyTrack(trackId);
    }

    /**
     * Setup Spotify.
     *
     * @param enabled boolean
     */
    public void setupSpotify(boolean enabled) {
        // Connect if enabled. Disconnect otherwise.
        if (enabled) {
            this.connectToApiOrAuth();
            this.connectToSpotifyApp();
        } else {
            this.mSharedPreferences.edit().remove(getString(R.string.access_token)).apply();
            this.mSpotifyViewModel.disconnect();
        }
    }

    /**
     * Connect to the SpotifyApp
     */
    private void connectToSpotifyApp() {
        this.mSpotifyViewModel.disconnect();
        SpotifyAppRemote.connect(this, this.mConnectionParams, this.mConnectionListener);
    }

    /**
     * Connect to the Spotify API or authenticate if no accesstoken is set.
     */
    public void connectToApiOrAuth() {
        String accessToken = this.mSharedPreferences.getString(getString(R.string.access_token), null);
        if (accessToken != null) {
            SpotifyApi spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(accessToken);
            this.mSpotifyViewModel.setSpotifyApi(spotifyApi);
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
            AuthenticationRequest request = builder
                    .setScopes(new String[]{"streaming", "app-remote-control"})
                    .build();
            AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
        }
    }

    private final ConnectionParams mConnectionParams = new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .showAuthView(true)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .build();

    private final Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            Log.d(TAG, "Connected to Spotify!");
            mSpotifyViewModel.setSpotifyAppRemote(spotifyAppRemote);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.d(TAG, throwable.getMessage(), throwable);
            if (throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException) {
                new SpotifyLoginDialog().show(getSupportFragmentManager(), "TAG");
            } else if (throwable instanceof CouldNotFindSpotifyApp) {
                new DownloadSpotifyDialog().show(getSupportFragmentManager(), "TAG");
            }
        }
    };

    /**
     * Listener for preference changes. Mostly done in SettingsFragement
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) -> {

        if (s.equalsIgnoreCase(getString(R.string.spotify_volume_key))) {
            int volume = this.getSpotifyVolume();
            this.setSpotifyVolume(volume);
        }

        if (s.equalsIgnoreCase(getString(R.string.spotify_key))) {
            this.setupSpotify(sharedPreferences.getBoolean(s, false));
        }

        if (s.equalsIgnoreCase(getString(R.string.current_spotify_track_key))) {
            this.setupSpotifyTrack(sharedPreferences.getString(s, null));
        }

    };

    /**
     * Gets called on preference change. Sets current track to given trackId
     *
     * @param trackId TrackId
     */
    private void setupSpotifyTrack(String trackId) {
        if (trackId != null) {
            SpotifyTrack spotifyTrack = this.mPreference.getObject(trackId, SpotifyTrack.class);
            if (spotifyTrack != null) {
                this.mSpotifyViewModel.setCurrentSpotifyTrack(spotifyTrack);
            } else {
                Log.w(TAG, "Could not find SpotifyTrack with id in Preference: " + trackId);
            }
        } else {
            Log.w(TAG, "SpotifyTrack is null");
        }
    }

    private void setSpotifyVolume(int volume) {
        this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private int getSpotifyVolume() {
        int savedVolume = this.mSharedPreferences.getInt(getString(R.string.spotify_volume_key), 25);
        int maxStreamVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return Math.round(((float) maxStreamVolume / 100) * savedVolume);
    }

    /**
     * Shows or hide the spotify control-icon depending on enabling spotify.
     *
     * @param enabled show if enabled
     */
    private void toggleMusicMenuItem(boolean enabled) {
        if (this.mSpotifyMenuItem != null) {
            this.mSpotifyMenuItem.setVisible(enabled);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        this.mSpotifyMenuItem = menu.add(Menu.NONE, ApplicationConstants.SPOTIFY_MENU_ITEM, Menu.NONE, getString(R.string.spotify));
        this.mSpotifyMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        this.mSpotifyMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_music_note_24));
        this.mSpotifyMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == ApplicationConstants.SPOTIFY_MENU_ITEM) {
            // play or stop
            PlayerState playerState = this.mSpotifyViewModel.getPlayerState().getValue();
            if (playerState != null) {
                if (playerState.isPaused) {
                    SpotifyTrack spotifyTrack = this.mSpotifyViewModel.getCurrentSpotifyTrack().getValue();
                    if (spotifyTrack != null) {
                        this.mSpotifyViewModel.play(spotifyTrack);
                    } else {
                        Toast.makeText(this, "No Track set", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    this.mSpotifyViewModel.pause();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, this.mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        SharedPreferences sharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, MODE_PRIVATE);

        // set current color theme
        String currentTheme = sharedPreferences.getString(getString(R.string.theme_key), getString(R.string.green_theme_key));
        if (currentTheme.equalsIgnoreCase(getString(R.string.green_theme_key))) {
            theme.applyStyle(R.style.Theme_Green, true);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.blue_theme_key))) {
            theme.applyStyle(R.style.Theme_Blue, true);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.red_theme_key))) {
            theme.applyStyle(R.style.Theme_Red, true);
        }

        // set dark theme if enabled
        boolean darkMode = sharedPreferences.getBoolean(getString(R.string.dark_mode_key), false);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO: // night mode NOT active
                if (darkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                break;
            case Configuration.UI_MODE_NIGHT_YES:// night mode active
                if (!darkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;
        }

        return theme;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Spotify result code when accessing accesstoken
            if (requestCode == SPOTIFY_REQUEST_CODE) {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
                switch (response.getType()) {
                    case TOKEN:
                        this.mSharedPreferences
                                .edit()
                                .putString(getString(R.string.access_token), response.getAccessToken())
                                .apply();

                        this.startSetupSpotify();
                        break;
                    case ERROR:
                        Log.d(TAG, "Spotify-onActivityResult: " + response.getError());
                        break;
                    default:
                        Log.d(TAG, "Spotify-onActivityResult: No TOKEN nor ERROR");
                }
            }
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");

        // Timer
        if (this.mStopWatch.isStopped()) {
            this.mStopWatch.start();
        }
        this.mHandler.postDelayed(updater, INTERVAL_DELAY);

        // User
        this.mFirebaseUserLiveData = this.mUserViewModel.getFirebaseUser();
        this.mFirebaseUserLiveData.observe(this, firebaseUser -> {
            // Get all sessions of user
            this.mStatsViewModel.loadHealthSessions(firebaseUser);
        });

        // Session
        this.mHealthSessionLiveData = this.mStatsViewModel.getCurrentHealthSession();
        this.mHealthSessionLiveData.observe(this, healthSession -> {
            Log.d(TAG, String.valueOf(healthSession.getTimeAppOpened()));
        });

        // Gamification
//        this.mStatsViewModel.loadGamifications();

        // spotify
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
        this.mSpotifyTrackLiveData = this.mSpotifyViewModel.getCurrentSpotifyTrack();
        this.mSpotifyAppRemoteLiveData = this.mSpotifyViewModel.getSpotifyRemoteApp();
        this.mPlayerStateLiveData = this.mSpotifyViewModel.getPlayerState();

        // Observe current SpotifyTrack to Play on change.
        this.mSpotifyTrackLiveData.observe(this, spotifyTrack -> this.mSpotifyViewModel.play(spotifyTrack));

        // Observe SpotifyRemoteApp to check if App is connected to Spotify.
        this.mSpotifyAppRemoteLiveData.observe(this, spotifyAppRemote -> {
            if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                // show menu icon
                this.toggleMusicMenuItem(true);

                // setup current selected track
                String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
                this.setupSpotifyTrack(trackId);
            } else {
                // not connected
                this.toggleMusicMenuItem(false);
            }
        });

        // Observe PlayerState and set icon accordingly
        this.mPlayerStateLiveData.observe(this, playerState -> {
            if (playerState != null) {
                if (this.mSpotifyMenuItem != null) {
                    // use icon depending on playerstate
                    int icon = playerState.isPaused ? R.drawable.ic_baseline_play_arrow_24 : R.drawable.ic_baseline_pause_24;
                    this.mSpotifyMenuItem.setIcon(ContextCompat.getDrawable(this, icon));
                }

                // get the image of current song
                SpotifyAppRemote spotifyAppRemote = this.mSpotifyViewModel.getSpotifyRemoteApp().getValue();
                if (spotifyAppRemote != null) {
                    if (playerState.track != null) {
                        ImageUri imageUri = playerState.track.imageUri;
                        if (imageUri != null) {
                            spotifyAppRemote.getImagesApi().getImage(imageUri).setResultCallback(bitmap -> {
                                if (bitmap != null) {
                                    this.mSpotifyViewModel.setCurrentTrackImage(bitmap);
                                }
                            });
                        }
                    }
                }
            }
        });

        this.startSetupSpotify();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mStopWatch.isSuspended()) {
            this.mStopWatch.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop called");

        if (this.mStopWatch.isStarted()) {
            this.mStopWatch.suspend();
        }

        if (this.mHealthSessionLiveData != null) {
            this.mHealthSessionLiveData.removeObservers(this);
        }

        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(updater);
        }

        this.removeSpotifyObservers();

        this.mSpotifyViewModel.disconnect();
        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }

    private void removeSpotifyObservers() {
        if (this.mPlayerStateLiveData != null) {
            this.mPlayerStateLiveData.removeObservers(this);
        }

        if (this.mSpotifyAppRemoteLiveData != null) {
            this.mSpotifyAppRemoteLiveData.removeObservers(this);
        }

        if (this.mSpotifyTrackLiveData != null) {
            this.mSpotifyTrackLiveData.removeObservers(this);
        }
    }
}