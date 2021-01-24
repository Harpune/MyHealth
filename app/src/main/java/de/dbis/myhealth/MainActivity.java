package de.dbis.myhealth;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
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
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.dialogs.DownloadSpotifyDialog;
import de.dbis.myhealth.ui.dialogs.SpotifyLoginDialog;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.ui.settings.SettingsViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;
import kaaes.spotify.webapi.android.SpotifyApi;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Google Fit
    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    // Spotify
    private final static int MENU_ITEM_ITEM_SPOTIFY = 2;
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    // View Models
    private GoogleFitConnector mGoogleFitConnector;
    public SettingsViewModel mSettingsViewModel;
    private QuestionnairesViewModel mQuestionnairesViewModel;

    // Views
    public FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    public CoordinatorLayout mCoordinatorLayout;

    // Android
    public SharedPreferences mSharedPreferences;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController mNavController;

    private Menu mMenu;
    private MenuItem mSpotifyMenuItem;

    private LiveData<PlayerState> mPlayerStateLiveData;
    private LiveData<SpotifyAppRemote> mSpotifyAppRemoteLiveData;
    private LiveData<SpotifyTrack> mSpotifyTrackLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.d(TAG, Build.ID);
//        Log.d(TAG, Build.DEVICE);
//        Log.d(TAG, Build.HARDWARE);
//        Log.d(TAG, Build.ID);
//        Log.d(TAG, Build.USER);
//        Log.d(TAG, Build.MANUFACTURER);
//        Log.d(TAG, Build.MODEL);

        // Set device id!
        this.mSharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
//        this.mSharedPreferences.edit().clear().apply();
//        this.deleteDatabase("app_database");
        if (!this.mSharedPreferences.contains(getString(R.string.device_id))) {
            this.mSharedPreferences.edit().putString(getString(R.string.device_id), UUID.randomUUID().toString()).apply();
        }

        this.mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        this.mQuestionnairesViewModel = new ViewModelProvider(this).get(QuestionnairesViewModel.class);
        this.mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);


        // navigation
        this.mCoordinatorLayout = findViewById(R.id.coordinator);
        this.mFab = findViewById(R.id.fab);
        this.initDrawerLayout();

        this.mNavController.addOnDestinationChangedListener((NavController.OnDestinationChangedListener) (controller, destination, arguments) -> {

        });

        // Google Fit
        this.mGoogleFitConnector = new GoogleFitConnector(this);
        if (this.mGoogleFitConnector.isEnabled()) {
            this.mGoogleFitConnector.connect();
            this.mGoogleFitConnector.getSleepingData();
        }

        this.mGoogleFitConnector.getSessionClient().observe(this, sessionsClient -> {
            Log.d("MainActivity", "SessionClient changed");
        });

        Intent intent = getIntent();
    }

    private String handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // get track Id
            String[] lines = sharedText.split("\\r?\\n");
            Uri uri = Uri.parse(lines[1]);
            return uri.getLastPathSegment();
        } else {
            Toast.makeText(this, "Wrong format.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void setFabClickListener(View.OnClickListener mFabClickListener) {
        this.mFab.setOnClickListener(mFabClickListener);
    }

    private void initDrawerLayout() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        this.mBottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(this.mBottomAppBar);

        this.mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_questionnaires, R.id.nav_settings)
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
            } else if (destination.getId() == R.id.nav_spotify_share_receiver) {
                this.mFab.show();
                this.mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_save_24));
            } else {
                this.mFab.hide();
            }

            // setup bottomAppBar
            if (destination.getId() == R.id.nav_questionnaire) {
                this.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            } else if (destination.getId() == R.id.nav_spotify_share_receiver) {
                this.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            } else {
                this.mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            }
            behavior.slideUp(this.mBottomAppBar);
        });
    }

    public void startSetupSpotify() {
        // Enabled
        boolean enabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
        this.setupSpotify(enabled);
    }

    public void setupSpotify(boolean enabled) {
        this.mSpotifyTrackLiveData.observe(this, spotifyTrack -> {
            this.mSettingsViewModel.play(spotifyTrack);
        });

        this.mSpotifyAppRemoteLiveData.observe(this, spotifyAppRemote -> {
            if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                // show menu icon
                this.toggleMusicMenuItem(true);

                // setup current selected track
                String currentTrackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
                if (currentTrackId != null) {
                    // get track id from shared preferences
                    this.mSettingsViewModel.getSpotifyTrackById(currentTrackId).observe(this, spotifyTrack -> {
                        // set spotifyTrack id from shared preferences
                        if (spotifyTrack != null) {
                            this.mSettingsViewModel.setCurrentSpotifyTrack(spotifyTrack);
                        }
                    });
                }
            } else {
                // this. to do if not connected
                this.toggleMusicMenuItem(false);
            }
        });

        if (enabled) {
            this.connectToApiOrAuth();
            this.connectToSpotifyApp();
        } else {
            this.mSettingsViewModel.disconnect();
        }
    }

    private void connectToSpotifyApp() {
        this.mSettingsViewModel.disconnect();
        SpotifyAppRemote.connect(this, this.mConnectionParams, this.mConnectionListener);
    }

    public void connectToApiOrAuth() {
        String accessToken = this.mSharedPreferences.getString(getString(R.string.access_token), null);
        if (accessToken != null) {
            SpotifyApi spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(accessToken);
            this.mSettingsViewModel.setSpotifyApi(spotifyApi);
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
            builder.setScopes(new String[]{"streaming"});
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
            if (throwable instanceof NotLoggedInException) {
                new SpotifyLoginDialog().show(getSupportFragmentManager(), "TAG");
            } else if (throwable instanceof UserNotAuthorizedException) {
                // TODO handle not authorize
            } else if (throwable instanceof CouldNotFindSpotifyApp) {
                new DownloadSpotifyDialog().show(getSupportFragmentManager(), "TAG");
            }
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) -> {
        if (s.equalsIgnoreCase(getString(R.string.spotify_key))) {
            this.setupSpotify(sharedPreferences.getBoolean(s, false));
        }
    };

    private void toggleMusicMenuItem(boolean enabled) {
        if (enabled) {
            this.mPlayerStateLiveData.observe(this, playerState -> {
                if (this.mMenu != null) {
                    // Clear menu
                    this.mMenu.clear();

                    // add menu item
                    this.mSpotifyMenuItem = this.mMenu.add(Menu.NONE, MENU_ITEM_ITEM_SPOTIFY, Menu.NONE, getString(R.string.spotify));
                    this.mSpotifyMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

                    // use icon depending on playerstate
                    int icon = playerState.isPaused ? R.drawable.ic_baseline_play_arrow_24 : R.drawable.ic_baseline_pause_24;
                    this.mSpotifyMenuItem.setIcon(ContextCompat.getDrawable(this, icon));
                }
            });
        } else {
            if (this.mMenu != null) {
                this.mMenu.clear();
            }
            if (this.mPlayerStateLiveData != null) {
                this.mPlayerStateLiveData.removeObservers(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ITEM_SPOTIFY) {
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
            // Google Fit
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                this.mGoogleFitConnector.connect();
            }

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
        this.mSpotifyAppRemoteLiveData = this.mSettingsViewModel.getSpotifyRemoteApp();
        this.mPlayerStateLiveData = this.mSettingsViewModel.getPlayerState();
        this.mSpotifyTrackLiveData = this.mSettingsViewModel.getCurrentSpotifyTrack();
        this.startSetupSpotify();

        // Check if from other app
        this.handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Check if from other app
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                final String trackId = this.handleSendText(intent);
                if (trackId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ApplicationConstants.TRACK_ID, trackId);
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.nav_spotify_share_receiver, bundle);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mSettingsViewModel.disconnect();
        if (this.mPlayerStateLiveData != null) {
            this.mPlayerStateLiveData.removeObservers(this);
        }

        if (this.mSpotifyAppRemoteLiveData != null) {
            this.mSpotifyAppRemoteLiveData.removeObservers(this);
        }

        if (this.mSpotifyTrackLiveData != null) {
            this.mSpotifyTrackLiveData.removeObservers(this);
        }
        this.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }
}