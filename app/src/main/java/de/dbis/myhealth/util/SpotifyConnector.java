package de.dbis.myhealth.util;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;
import com.spotify.sdk.android.auth.app.SpotifyAuthHandler;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.ui.settings.SettingsViewModel;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyConnector {

    private final static String TAG = "SpotifyConnector";
    private final static String CLIENT_ID = "da07627d8dba46a88700c9ee8acb1832";
    private final static String CLIENT_SECRET = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String AUTH = "ODBiYzk3Y2RkZjlhNGEwZmExZmE1ZGYzMGM2ZjFjZDg6ZGEwNzYyN2Q4ZGJhNDZhODg3MDBjOWVlOGFjYjE4MzI=";

    private static final int REQUEST_CODE = 1337;
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    private final MainActivity mMainActivity;
    private final SettingsViewModel mSettingsViewModel;
    private final SpotifyApi api;

    private String accessToken = null;

    public SpotifyConnector(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
        this.mSettingsViewModel = new ViewModelProvider(mainActivity).get(SettingsViewModel.class);
        this.api = new SpotifyApi();

        this.getAccessToken(new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    accessToken = new JSONObject(result).get("access_token").toString();
                    api.setAccessToken(accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestError(VolleyError errorMessage) {
                accessToken = null;
            }
        });
    }

    private final ConnectionParams mConnectionParams = new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .showAuthView(true)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .build();

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
//        // Authenticate
//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
//        builder.setScopes(new String[]{"streaming"});
//        AuthenticationRequest request = builder.build();
//        AuthenticationClient.openLoginActivity(this.mMainActivity, REQUEST_CODE, request);

        // Connect to spotify app
        SpotifyAppRemote.connect(this.mMainActivity, this.mConnectionParams, this.mConnectionListener);
    }

    public void disconnect() {
        this.getSpotify().observe(this.mMainActivity, spotifyAppRemote -> {
            SpotifyAppRemote.disconnect(this.mSettingsViewModel.getSpotify().getValue());
        });
    }

    public LiveData<SpotifyAppRemote> getSpotify() {
        return this.mSettingsViewModel.getSpotify();
    }

    public void getTracks(List<String> ids) {
        this.api.getService().getTrack("1mMEEkdjJKPRrcGiIkHpao", new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                Log.d("TRACK", track.name);
                Log.d("Response", response.toString());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    private void getAccessToken(final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(mMainActivity);

        String url = "https://accounts.spotify.com/api/token";
        String body = "grant_type=client_credentials";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                callback::onSuccess,
                callback::onRequestError
        ) {
            @Override
            public byte[] getBody() {
                return body.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                params.put("Authorization", "Basic " + AUTH);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public interface VolleyCallback {
        void onSuccess(String result);

        void onRequestError(VolleyError errorMessage);
    }

    private final Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            Log.d("SpotifyConnector", "Connected to Spotify!");
            mSettingsViewModel.setSpotify(spotifyAppRemote);
            mMainActivity.setupMusicMenuIcon(true);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.e("MainActivity", throwable.getMessage(), throwable);
            if (throwable instanceof NotLoggedInException) {
                openSpotifyLoginDialog();
            } else if (throwable instanceof UserNotAuthorizedException) {
                // TODO handle not authorize
            } else if (throwable instanceof CouldNotFindSpotifyApp) {
                openDownloadSpotifyDialog();
            }
            mMainActivity.setupMusicMenuIcon(false);
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
                .setNegativeButton("Cancel", (dialog, i) -> PreferenceManager.getDefaultSharedPreferences(this.mMainActivity)
                        .edit()
                        .putBoolean(this.mMainActivity.getString(R.string.spotify_key), false)
                        .apply());

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
                .setNegativeButton("Cancel", (dialog, i) -> PreferenceManager.getDefaultSharedPreferences(this.mMainActivity)
                        .edit()
                        .putBoolean(this.mMainActivity.getString(R.string.spotify_key), false)
                        .apply());
        builder.show();
    }
}
