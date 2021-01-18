package de.dbis.myhealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import de.dbis.myhealth.models.Result;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.ui.settings.SettingsViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;

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

    // Views
    private FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    public CoordinatorLayout mCoordinatorLayout;

    // Android
    private SharedPreferences mSharedPreferences;
    private AppBarConfiguration mAppBarConfiguration;

    private Menu mMenu;
    private MenuItem mSpotifyMenuItem;

    private LiveData<PlayerState> mPlayerStateLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        this.deleteDatabase("app_database");

//        Log.d(TAG, Build.ID);
//        Log.d(TAG, Build.DEVICE);
//        Log.d(TAG, Build.HARDWARE);
//        Log.d(TAG, Build.ID);
//        Log.d(TAG, Build.USER);
//        Log.d(TAG, Build.MANUFACTURER);
//        Log.d(TAG, Build.MODEL);

        // Set device id!
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!this.mSharedPreferences.contains(getString(R.string.device_id))) {
            this.mSharedPreferences.edit().putString(getString(R.string.device_id), UUID.randomUUID().toString()).apply();
        }

        this.mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // navigation
        this.mCoordinatorLayout = findViewById(R.id.coordinator);
        this.mFab = findViewById(R.id.fab);
        this.initDrawerLayout();
        this.mFab.setOnClickListener(view -> {

            QuestionnairesViewModel viewHolder = new ViewModelProvider(this).get(QuestionnairesViewModel.class);

            // Check Fragment
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination navDestination = navController.getCurrentDestination();
            if (navDestination != null) {
                if (navDestination.getId() == R.id.nav_home) {
                    viewHolder.getQuestionnaires().observe(this, questionnaires -> {
                        String questionnairePref = this.mSharedPreferences.getString(getString(R.string.questionnaire_fast_start_key), null);
                        Log.d(TAG, "questionnairePref: " + questionnairePref);
                        Log.d(TAG, "questionnaires: " + questionnaires);
                        if (questionnairePref != null && questionnaires != null) {
                            Optional<Questionnaire> questionnaire = questionnaires.stream().filter(tmp -> tmp.getId().equalsIgnoreCase(questionnairePref)).findFirst();
                            if (questionnaire.isPresent()) {
                                viewHolder.select(questionnaire.get());
                                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.nav_questionnaire);
                            } else {
                                Toast.makeText(this, "Set Questionnaire for fast access in Settings.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                if (navDestination.getId() == R.id.nav_questionnaire) {
                    Questionnaire questionnaire = viewHolder.getSelected().getValue();
                    Log.d(TAG, "WTF" + questionnaire);

                    if (questionnaire != null) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Done")
                                .setMessage("Are you done with this questionnaire?")
                                .setPositiveButton("Yes", (dialogInterface, i) -> {

                                    // IDs
                                    String userId = this.mSharedPreferences.getString(getString(R.string.device_id), UUID.randomUUID().toString());
                                    String resultId = UUID.randomUUID().toString();

                                    // get result data
                                    List<Integer> resultEntries = Optional.ofNullable(questionnaire.getQuestions())
                                            .map(Collection::stream)
                                            .orElseGet(Stream::empty)
                                            .map(Question::getResult)
                                            .collect(Collectors.toList());

                                    // build result
                                    Result result = new Result(
                                            resultId,
                                            userId,
                                            new Date(),
                                            100000L,
                                            questionnaire.getId(),
                                            resultEntries,
                                            new ArrayList<>()
                                    );

                                    // save result
                                    QuestionnairesViewModel viewModel = new ViewModelProvider(this).get(QuestionnairesViewModel.class);
                                    viewModel.sendResult(result);

                                    navController.popBackStack();
                                })
                                .setNegativeButton("No", null).show();
                    }
                }
            }
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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(this.mBottomAppBar, navController, this.mAppBarConfiguration);

        // check for current fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            HideBottomViewOnScrollBehavior<BottomAppBar> behavior = this.mBottomAppBar.getBehavior();

            // setup fab
            if (destination.getId() == R.id.nav_home) {
                this.mFab.show();
            } else if (destination.getId() == R.id.nav_questionnaire) {
                this.mFab.show();
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

    public void setupSpotifyConnection() {
        boolean enabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
        if (enabled) {
            this.tryConnectToSpotify();
        }
    }

    public void tryConnectToSpotify() {
        String accessToken = this.mSharedPreferences.getString(getString(R.string.access_token), null);
        if (accessToken != null) {
            this.connectToSpotify(accessToken);
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
        }

    }

    private void connectToSpotify(String accessToken) {
        this.mSettingsViewModel.isConnected().observe(this, connected -> {
            this.setupMusicMenuIcon();

            boolean autoPlayEnabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_autoplay_key), false);
            if (autoPlayEnabled) {
                this.mSettingsViewModel.playSpotifyTrack();
            }
        });

        this.mSettingsViewModel.connect(accessToken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        return true;
    }

    public void setupMusicMenuIcon() {
        this.toggleMusicMenuItem(this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false));
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) -> {
        if (s.equalsIgnoreCase(getString(R.string.spotify_key))) {
            this.toggleMusicMenuItem(sharedPreferences.getBoolean(getString(R.string.spotify_key), false));
        }
    };

    private void toggleMusicMenuItem(boolean enabled) {
        if (enabled) {
            this.mPlayerStateLiveData = this.mSettingsViewModel.getPlayerState();
            this.mPlayerStateLiveData.observe(this, playerState -> {
                if (this.mMenu != null) {
                    this.mMenu.clear();
                    this.mSpotifyMenuItem = this.mMenu.add(Menu.NONE, MENU_ITEM_ITEM_SPOTIFY, Menu.NONE, getString(R.string.spotify));
                    this.mSpotifyMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
                        PreferenceManager.getDefaultSharedPreferences(this)
                                .edit()
                                .putString(getString(R.string.access_token), response.getAccessToken())
                                .apply();

                        this.setupSpotifyConnection();
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
    protected void onPause() {
        super.onPause();
        this.mSettingsViewModel.disconnect();
        if (this.mPlayerStateLiveData != null) {
            this.mPlayerStateLiveData.removeObservers(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.setupSpotifyConnection();
//        QuestionnairesViewModel viewModel = new ViewModelProvider(this).get(QuestionnairesViewModel.class);
//        viewModel.generateTFI(this);
//        viewModel.generateTHI(this);
    }
}