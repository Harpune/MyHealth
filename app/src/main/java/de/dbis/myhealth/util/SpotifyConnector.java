package de.dbis.myhealth.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import de.dbis.myhealth.R;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class SpotifyConnector {

    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    private final Activity mActivity;
    private final SettingsViewModel mSettingsViewModel;

    public SpotifyConnector(Activity activity) {
        this.mActivity = activity;
        this.mSettingsViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(SettingsViewModel.class);
    }

    public void play() {
        SpotifyAppRemote spotifyAppRemote = this.getSpotify().getValue();
        spotifyAppRemote.getPlayerApi().setShuffle(false);
        spotifyAppRemote.getPlayerApi().play("spotify:album:1cLEpY3zVvw0zV6WwVoAEB");
    }

    public void pause() {
        this.getSpotify().getValue().getPlayerApi().pause();
    }

    public boolean isEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(mActivity.getString(R.string.spotify_key), false);
    }

    public void connect() {
        SpotifyAppRemote.connect(mActivity, this.mConnectionParams, this.mConnectionListener);
    }

    public void disconnect() {
        if(this.getSpotify().getValue() != null){
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

    private void openSpotifyLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mActivity)
                .setTitle("Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialog, i) -> {
                    dialog.dismiss();
                    Intent launchIntent = this.mActivity.getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        this.mActivity.startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(this.mActivity)
                            .edit()
                            .putBoolean(this.mActivity.getString(R.string.spotify_key), false)
                            .apply();
                });

        builder.show();
    }

    private void openDownloadSpotifyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mActivity)
                .setTitle("Spotify")
                .setMessage("To include a some music you can download Spotify")
                .setPositiveButton("Download", (dialog, i) -> {
                    dialog.dismiss();
                    try {
                        this.mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));
                    } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                        this.mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> {
                    PreferenceManager.getDefaultSharedPreferences(this.mActivity)
                            .edit()
                            .putBoolean(this.mActivity.getString(R.string.spotify_key), false)
                            .apply();
                });
        builder.show();
    }
}
