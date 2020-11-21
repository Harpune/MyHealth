package de.dbis.myhealth.util;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class SpotifyConnector {

    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    private final MainActivity mMainActivity;
    private final SettingsViewModel mSettingsViewModel;

    public SpotifyConnector(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
        this.mSettingsViewModel = new ViewModelProvider(mainActivity).get(SettingsViewModel.class);
    }

    public void play() {
        SpotifyAppRemote spotifyAppRemote = this.getSpotify().getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().setShuffle(false);
            spotifyAppRemote.getPlayerApi().play("spotify:album:1cLEpY3zVvw0zV6WwVoAEB");
        }
    }

    public void pause() {
        SpotifyAppRemote spotifyAppRemote = this.getSpotify().getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public boolean isEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this.mMainActivity).getBoolean(mMainActivity.getString(R.string.spotify_key), false);
    }

    public void connect() {
        SpotifyAppRemote.connect(mMainActivity, this.mConnectionParams, this.mConnectionListener);
    }

    public void disconnect() {
        if (this.getSpotify().getValue() != null) {
            this.getSpotify().getValue().getPlayerApi().pause();
        }
        SpotifyAppRemote.disconnect(this.mSettingsViewModel.getSpotify().getValue());
    }

    public LiveData<SpotifyAppRemote> getSpotify() {
        return this.mSettingsViewModel.getSpotify();
    }

    private final ConnectionParams mConnectionParams = new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .showAuthView(true)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .build();

    private final Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            Log.d("SettingsFragment", "Connected to Spotify!");
            mSettingsViewModel.setSpotify(spotifyAppRemote);
            mMainActivity.showMusicIcon(true);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.e("MainActivity", throwable.getMessage(), throwable);
            if (throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException) {
                openSpotifyLoginDialog();
            } else if (throwable instanceof CouldNotFindSpotifyApp) {
                openDownloadSpotifyDialog();
            }
            mMainActivity.showMusicIcon(false);
        }
    };

    private void openSpotifyLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mMainActivity)
                .setTitle("Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialog, i) -> {
                    dialog.dismiss();
                    Intent launchIntent = this.mMainActivity.getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        this.mMainActivity.startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(this.mMainActivity)
                            .edit()
                            .putBoolean(this.mMainActivity.getString(R.string.spotify_key), false)
                            .apply();
                });

        builder.show();
    }

    private void openDownloadSpotifyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mMainActivity)
                .setTitle("Spotify")
                .setMessage("To include a some music you can download Spotify")
                .setPositiveButton("Download", (dialog, i) -> {
                    dialog.dismiss();
                    try {
                        this.mMainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));
                    } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                        this.mMainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(this.mMainActivity)
                            .edit()
                            .putBoolean(this.mMainActivity.getString(R.string.spotify_key), false)
                            .apply();
                });
        builder.show();
    }
}
