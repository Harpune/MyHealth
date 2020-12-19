package de.dbis.myhealth;

import android.content.DialogInterface;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.List;
import java.util.Optional;

import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;
import de.dbis.myhealth.util.GoogleFitConnector;
import de.dbis.myhealth.util.SpotifyConnector;

public class MainActivity extends AppCompatActivity {
    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private final static int MENU_ITEM_ITEM_SPOTIFY = 2;
    private static final String TAG = "MainActivity";

    // View Models
    private GoogleFitConnector mGoogleFitConnector;
    private SpotifyConnector mSpotifyConnector;

    // Views
    private FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    public CoordinatorLayout mCoordinatorLayout;

    // Android
    private AppBarConfiguration mAppBarConfiguration;

    private MenuItem spotifyMenuItem;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // navigation
        this.mCoordinatorLayout = findViewById(R.id.coordinator);
        this.mFab = findViewById(R.id.fab);
        this.initDrawerLayout();
        this.mFab.setOnClickListener(view -> {
            // View Holder
            QuestionnairesViewModel viewHolder = new ViewModelProvider(this).get(QuestionnairesViewModel.class);
            List<Questionnaire> questionnaires = viewHolder.getQuestionnaires().getValue();

            // Check Fragment
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            if (navController.getCurrentDestination().getId() == R.id.nav_questionnaire) {
                Questionnaire questionnaire = viewHolder.getSelected().getValue();
                Log.d(TAG, "WTF");

                new MaterialAlertDialogBuilder(this)
                        .setTitle("Done")
                        .setMessage("Are you done qith this questionnaire?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            Toast.makeText(this, "DONE!!", Toast.LENGTH_SHORT).show();
                            navController.popBackStack();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            Toast.makeText(this, "Ok then finish", Toast.LENGTH_SHORT).show();
                        }).show();
            } else {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String questionnairePref = sharedPreferences.getString(getString(R.string.questionnaire_fast_start_key), null);

                Optional<Questionnaire> questionnaire = questionnaires.stream().filter(tmp -> tmp.getId().equalsIgnoreCase(questionnairePref)).findFirst();
                if (questionnaire.isPresent()) {
                    viewHolder.select(questionnaire.get());
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.nav_questionnaire);
                } else {
                    Toast.makeText(this, "Set Questionnaire for fast access in Settings.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Google Fit
        this.mGoogleFitConnector = new GoogleFitConnector(this);
        if (this.mGoogleFitConnector.isEnabled()) {
            this.mGoogleFitConnector.connect();
        }

        this.mGoogleFitConnector.getSessionClient().observe(this, sessionsClient -> {
            Log.d("MainActivity", "SessionClient changed");
        });

        // Spotify
        this.mSpotifyConnector = new SpotifyConnector(this);
        if (this.mSpotifyConnector.isEnabled()) {
            this.mSpotifyConnector.connect();
        }

        this.mSpotifyConnector.getSpotify().observe(this, spotifyAppRemote -> {
            // play on autoplay
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.spotify_autoplay_key), false)) {
                this.mSpotifyConnector.play();
            }

            // set bottom app bar icon
            spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                if (playerState.isPaused) {
                    spotifyMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_play_arrow_24));
                } else {
                    spotifyMenuItem.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_pause_24));
                }
            });

            Log.d("MainActivity", "Spotify changed");
        });
    }

    private void initDrawerLayout() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        this.mBottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(this.mBottomAppBar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_questionnaires, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(this.mBottomAppBar, navController, mAppBarConfiguration);

        // check for current fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            HideBottomViewOnScrollBehavior behavior = mBottomAppBar.getBehavior();

            // setup fab
            if (destination.getId() == R.id.nav_home) {
                mFab.show();
            } else if (destination.getId() == R.id.nav_questionnaire) {
                mFab.show();
            } else {
                mFab.hide();
            }

            // setup bottomAppBar
            if (destination.getId() == R.id.nav_questionnaire) {
                mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            } else {
                mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            }
            behavior.slideUp(mBottomAppBar);
        });
    }

    public void showMusicIcon(boolean visible) {
        if (this.mMenu != null) {
            this.mMenu.clear();

            if (visible) {
                this.spotifyMenuItem = this.mMenu.add(Menu.NONE, MENU_ITEM_ITEM_SPOTIFY, Menu.NONE, getString(R.string.spotify));
                this.spotifyMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                this.spotifyMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24));
            } else {
                this.spotifyMenuItem = null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        this.showMusicIcon(this.mSpotifyConnector.isEnabled());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ITEM_SPOTIFY) {
            // play or stop
            SpotifyAppRemote spotifyAppRemote = this.mSpotifyConnector.getSpotify().getValue();
            if (spotifyAppRemote != null) {
                spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                    if (playerState.isPaused) {
                        mSpotifyConnector.play();
                    } else {
                        mSpotifyConnector.pause();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
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
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                this.mGoogleFitConnector.connect();
            }
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mSpotifyConnector.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mSpotifyConnector.disconnect();
        if (this.mSpotifyConnector.isEnabled()) {
            this.mSpotifyConnector.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        QuestionnairesViewModel viewModel = new ViewModelProvider(this).get(QuestionnairesViewModel.class);
        viewModel.generateTFI(this);
        viewModel.generateTHI(this);
    }
}