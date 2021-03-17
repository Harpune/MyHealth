package de.dbis.myhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.PlayerState;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.dialogs.DownloadSpotifyDialog;
import de.dbis.myhealth.ui.dialogs.SpotifyLoginDialog;
import de.dbis.myhealth.ui.settings.SettingsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;
import kaaes.spotify.webapi.android.SpotifyApi;

import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_CLIENT_ID;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REDIRECT_URI;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // View Models
    public SettingsViewModel mSettingsViewModel;
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

    private LiveData<PlayerState> mPlayerStateLiveData;
    private LiveData<SpotifyAppRemote> mSpotifyAppRemoteLiveData;
    private LiveData<SpotifyTrack> mSpotifyTrackLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get settings and settings- db
        PowerPreference.init(this);
        this.mPreference = PowerPreference.getDefaultFile();
//        this.mPreference.clear();

        this.mSharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
        this.mSharedPreferences.edit().clear().apply();
        this.deleteDatabase("app_database");

        this.mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        this.mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // navigation
        this.mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        this.mCoordinatorLayout = findViewById(R.id.coordinator);
        this.mFab = findViewById(R.id.fab);
        this.initDrawerLayout();
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
                R.id.nav_user, R.id.nav_home, R.id.nav_questionnaires, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupWithNavController(navigationView, this.mNavController);
        NavigationUI.setupWithNavController(this.mBottomAppBar, this.mNavController, this.mAppBarConfiguration);

        // check for current fragment
        this.mNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            HideBottomViewOnScrollBehavior<BottomAppBar> behavior = this.mBottomAppBar.getBehavior();

            // setup fab
            if (destination.getId() == R.id.nav_home) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_send_24));
            } else if (destination.getId() == R.id.nav_questionnaire) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            } else if (destination.getId() == R.id.nav_user) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_save_24));
            } else {
                this.mFab.hide();
            }

            // setup bottomAppBar
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
            this.mSettingsViewModel.disconnect();
        }
    }

    /**
     * Connect to the SpotifyApp
     */
    private void connectToSpotifyApp() {
        this.mSettingsViewModel.disconnect();
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
            this.mSettingsViewModel.setSpotifyApi(spotifyApi);
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
            builder.setScopes(new String[]{"streaming", "app-remote-control"});
            AuthenticationRequest request = builder.build();
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
            mSettingsViewModel.setSpotifyAppRemote(spotifyAppRemote);
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

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) -> {
        if (s.equalsIgnoreCase(getString(R.string.spotify_key))) {
            this.setupSpotify(sharedPreferences.getBoolean(s, false));
        }

        if (s.equalsIgnoreCase(getString(R.string.current_spotify_track_key))) {
            this.setupSpotifyTrack(sharedPreferences.getString(s, null));
        }
    };

    private void setupSpotifyTrack(String trackId) {
        if (trackId != null) {
            SpotifyTrack spotifyTrack = this.mPreference.getObject(trackId, SpotifyTrack.class);
            if (spotifyTrack != null) {
                this.mSettingsViewModel.setCurrentSpotifyTrack(spotifyTrack);
            } else {
                Log.w(TAG, "Could not find SpotifyTrack with id in Preference: " + trackId);
            }
        } else {
            Log.w(TAG, "SpotifyTrack is null");
        }
    }

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
            PlayerState playerState = this.mSettingsViewModel.getPlayerState().getValue();
            if (playerState != null) {
                if (playerState.isPaused) {
                    SpotifyTrack spotifyTrack = this.mSettingsViewModel.getCurrentSpotifyTrack().getValue();
                    if (spotifyTrack != null) {
                        this.mSettingsViewModel.play(spotifyTrack);
                    } else {
                        Toast.makeText(this, "No Track set", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    this.mSettingsViewModel.pause();
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
        String currentTheme = sharedPreferences.getString(getString(R.string.theme_key), getString(R.string.green_theme_key));
        if (currentTheme.equalsIgnoreCase(getString(R.string.green_theme_key))) {
            theme.applyStyle(R.style.Theme_Green, true);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.blue_theme_key))) {
            theme.applyStyle(R.style.Theme_Blue, true);
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.red_theme_key))) {
            theme.applyStyle(R.style.Theme_Red, true);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Spotify
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

        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
        this.mSpotifyTrackLiveData = this.mSettingsViewModel.getCurrentSpotifyTrack();
        this.mSpotifyAppRemoteLiveData = this.mSettingsViewModel.getSpotifyRemoteApp();
        this.mPlayerStateLiveData = this.mSettingsViewModel.getPlayerState();

        // Observe current SpotifyTrack to Play on change.
        this.mSpotifyTrackLiveData.observe(this, spotifyTrack -> {
            Log.d(TAG, "!!!!!! Played: " + spotifyTrack.getTrack().name);
            this.mSettingsViewModel.play(spotifyTrack);
        });

        // Observe SpotifyRemoteApp to check if App is connected to Spotify.
        this.mSpotifyAppRemoteLiveData.observe(this, spotifyAppRemote -> {
            if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                // show menu icon
                this.toggleMusicMenuItem(true);

                // setup current selected track
                String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
                this.setupSpotifyTrack(trackId);
//                if (trackId != null) {
//                    // get track id from shared preferences
//                    this.mSettingsViewModel.getSpotifyTrackById(trackId).observe(this, spotifyTrack -> {
//                        // set spotifyTrack id from shared preferences
//                        if (spotifyTrack != null) {
//                            Log.d(TAG, "!!!!! Trying to set current Track");
//                            this.mSettingsViewModel.setCurrentSpotifyTrack(spotifyTrack);
//                        }
//                    });
//                } else {
//                    Log.d(TAG, "No track set to play");
//                }
            } else {
                // not connected
                this.toggleMusicMenuItem(false);
            }
        });

        // Setup playerstate
        this.mPlayerStateLiveData.observe(this, playerState -> {
            if (this.mSpotifyMenuItem != null) {
                // use icon depending on playerstate
                int icon = playerState.isPaused ? R.drawable.ic_baseline_play_arrow_24 : R.drawable.ic_baseline_pause_24;
                this.mSpotifyMenuItem.setIcon(ContextCompat.getDrawable(this, icon));
            }
        });

        this.startSetupSpotify();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mPlayerStateLiveData != null) {
            this.mPlayerStateLiveData.removeObservers(this);
        }

        if (this.mSpotifyAppRemoteLiveData != null) {
            this.mSpotifyAppRemoteLiveData.removeObservers(this);
        }

        if (this.mSpotifyTrackLiveData != null) {
            this.mSpotifyTrackLiveData.removeObservers(this);
        }

        this.mSettingsViewModel.disconnect();
        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }
}