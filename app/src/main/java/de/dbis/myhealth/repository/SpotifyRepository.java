package de.dbis.myhealth.repository;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dbis.myhealth.R;
import de.dbis.myhealth.dao.SpotifyDataDao;
import de.dbis.myhealth.dao.SpotifyTrackDao;
import de.dbis.myhealth.models.SpotifyData;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.util.AppDatabase;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyRepository {
    private final static String TAG = "SpotifyRepository";

    private final static String CLIENT_ID = "da07627d8dba46a88700c9ee8acb1832";
    private final static String CLIENT_SECRET = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String AUTH = "ODBiYzk3Y2RkZjlhNGEwZmExZmE1ZGYzMGM2ZjFjZDg6ZGEwNzYyN2Q4ZGJhNDZhODg3MDBjOWVlOGFjYjE4MzI=";

    private static final int REQUEST_CODE = 1337;
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    private final MutableLiveData<String> mAccessToken;
    private final MutableLiveData<SpotifyApi> mSpotifyApi;
    private final MutableLiveData<SpotifyAppRemote> mSpotifyAppRemote;
    private final MutableLiveData<PlayerState> mPlayerState;
    private final MutableLiveData<PlayerContext> mPlayerContext;
    private final MutableLiveData<Boolean> mIsConnected;
    private final MutableLiveData<Boolean> mIsPlaying;

    private final MutableLiveData<SpotifyTrack> mSpotifyTrack;
    private final MutableLiveData<Track> mTrack;
    private final MutableLiveData<AudioFeaturesTrack> mAudioFeaturesTrack;

    private final String mDeviceId;
    private final SpotifyDataDao mSpotifyDataDao;
    private final SpotifyTrackDao mSpotifyTrackDao;
    private final Application application;

    public SpotifyRepository(Application application) {
        this.application = application;

        this.mDeviceId = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext())
                .getString(this.application.getString(R.string.device_id), this.application.getString(R.string.device_id));

        // db
        AppDatabase db = AppDatabase.getInstance(application);
        this.mSpotifyDataDao = db.spotifyDataDao();
        this.mSpotifyTrackDao = db.spotifyTrackDao();

        // Live Data
        this.mAccessToken = new MutableLiveData<>();
        this.mSpotifyApi = new MutableLiveData<>();
        this.mSpotifyAppRemote = new MutableLiveData<>();
        this.mPlayerState = new MutableLiveData<>();
        this.mPlayerContext = new MutableLiveData<>();

        this.mIsConnected = new MutableLiveData<>();
        this.mIsPlaying = new MutableLiveData<>();

        this.mSpotifyTrack = new MutableLiveData<>();
        this.mTrack = new MutableLiveData<>();
        this.mAudioFeaturesTrack = new MutableLiveData<>();
    }

    public LiveData<SpotifyData> setupSpotifyData() {
        this.requestAccessToken();

//        // Authenticate
//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
//        builder.setScopes(new String[]{"streaming"});
//        AuthenticationRequest request = builder.build();
//        AuthenticationClient.openLoginActivity(this.mMainActivity, REQUEST_CODE, request);

        return this.mSpotifyDataDao.getByDeviceId(this.mDeviceId);
    }

    public void connect(String accessToken) {
        // Connect to spotify api
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(accessToken);
        this.mSpotifyApi.setValue(spotifyApi);

        // Connect to spotify app
        SpotifyAppRemote.connect(this.application, this.mConnectionParams, this.mConnectionListener);
    }

    public void disconnect() {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
        }
        this.mIsConnected.setValue(false);
    }

    public LiveData<Boolean> isConnected() {
        return this.mIsConnected;
    }

    public void loadTrack(String id) {
        SpotifyApi spotifyApi = this.mSpotifyApi.getValue();
        if (spotifyApi != null) {
            // Get
            spotifyApi.getService().getTrack(id, new Callback<Track>() {
                @Override
                public void success(Track track, Response response) {
                    mTrack.setValue(track);

                    SpotifyTrack spotifyTrack = new SpotifyTrack(track.id);
                    spotifyTrack.setTrack(track);
                    saveSpotifyTrack(spotifyTrack);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Get Track", error);
                }
            });
        }
    }

    public LiveData<Track> getCurrentTrack() {
        return this.mTrack;
    }

    public void loadAudioFeaturesTrack(String id) {
        SpotifyApi spotifyApi = this.mSpotifyApi.getValue();
        if (spotifyApi != null) {
            // Get
            spotifyApi.getService().getTrackAudioFeatures(id, new Callback<AudioFeaturesTrack>() {
                @Override
                public void success(AudioFeaturesTrack audioFeaturesTrack, Response response) {
                    mAudioFeaturesTrack.setValue(audioFeaturesTrack);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Get Track", error);
                }
            });
        }
    }

    public LiveData<AudioFeaturesTrack> getCurrentAudioFeaturesTrack() {
        return this.mAudioFeaturesTrack;
    }

    public LiveData<SpotifyTrack> createdSpotifyTrack() {
        MediatorLiveData<SpotifyTrack> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(this.mTrack, value -> {
            if (value != null) {
                mediatorLiveData.setValue(this.waitForSpotifyTrack());
            }
        });
        mediatorLiveData.addSource(this.mAudioFeaturesTrack, value -> {
            if (value != null) {
                mediatorLiveData.setValue(this.waitForSpotifyTrack());
            }
        });

        return mediatorLiveData;
    }

    private SpotifyTrack waitForSpotifyTrack() {
        Track track = mTrack.getValue();
        AudioFeaturesTrack audioFeaturesTrack = mAudioFeaturesTrack.getValue();
        if (track == null && audioFeaturesTrack == null) {
            return null;
        }

        SpotifyTrack spotifyTrack = new SpotifyTrack(track.id);
        spotifyTrack.setTrack(track);
        spotifyTrack.setAudioFeaturesTrack(audioFeaturesTrack);

        this.mTrack.setValue(null);
        this.mAudioFeaturesTrack.setValue(null);

        return spotifyTrack;
    }


    public void saveSpotifyTrack(SpotifyTrack spotifyTrack) {
        AppDatabase.databaseWriteExecutor.execute(() -> mSpotifyTrackDao.insert(spotifyTrack));
    }

    public LiveData<SpotifyTrack> getCurrentSpotifyTrack() {
        return this.mSpotifyTrack;
    }

    public void setCurrentSpotifyTrack(SpotifyTrack track) {
        this.mSpotifyTrack.setValue(track);
    }

    public LiveData<SpotifyTrack> getSpotifyTrack(String id) {
        return this.mSpotifyTrackDao.getByTrackId(id);
    }

    public LiveData<List<SpotifyTrack>> getAllSpotifyTracks() {
        return this.mSpotifyTrackDao.getAll();
    }

    public void playTrack() {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().setShuffle(false);
            spotifyAppRemote.getPlayerApi().setRepeat(Repeat.ALL);

            SpotifyTrack spotifyTrack = this.mSpotifyTrack.getValue();
            if (spotifyTrack != null) {
                spotifyAppRemote.getPlayerApi().play(spotifyTrack.getTrack().uri);
            }
        }
    }

    public void pause() {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public LiveData<PlayerState> getPlayerState() {
        return mPlayerState;
    }

    public LiveData<PlayerContext> getPlayerContext() {
        return mPlayerContext;
    }

    private void requestAccessToken() {
        RequestQueue queue = Volley.newRequestQueue(this.application);
        String url = "https://accounts.spotify.com/api/token";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        String accessToken = new JSONObject(response).get("access_token").toString();
                        Log.d(TAG, "AccessToken: " + accessToken);

                        // insert data in room
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            if (this.mSpotifyDataDao.exists(this.mDeviceId)) {
                                this.mSpotifyDataDao.updateAccessToken(this.mDeviceId, accessToken);
                            } else {
                                this.mSpotifyDataDao.insert(new SpotifyData(this.mDeviceId, accessToken));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d(TAG, "AccessToken", error)) {
            @Override
            public byte[] getBody() {
                String body = "grant_type=client_credentials";
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

    private final ConnectionParams mConnectionParams = new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .showAuthView(true)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .build();

    private final Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            Log.d(TAG, "Connected to Spotify!");
            spotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(mPlayerState::setValue);
            spotifyAppRemote.getPlayerApi().subscribeToPlayerContext().setEventCallback(mPlayerContext::setValue);

            mSpotifyAppRemote.setValue(spotifyAppRemote);
            mIsConnected.setValue(true);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.e(TAG, throwable.getMessage(), throwable);
            if (throwable instanceof NotLoggedInException) {
                openSpotifyLoginDialog();
            } else if (throwable instanceof UserNotAuthorizedException) {
                // TODO handle not authorize
            } else if (throwable instanceof CouldNotFindSpotifyApp) {
                openDownloadSpotifyDialog();
            }
            mIsConnected.setValue(false);
        }
    };

    private void openSpotifyLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.application)
                .setTitle("Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialog, i) -> {
                    dialog.dismiss();
                    Intent launchIntent = this.application.getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        this.application.startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> PreferenceManager.getDefaultSharedPreferences(this.application)
                        .edit()
                        .putBoolean(this.application.getString(R.string.spotify_key), false)
                        .apply());

        builder.show();
    }

    private void openDownloadSpotifyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.application)
                .setTitle("Spotify")
                .setMessage("To include a some music you can download Spotify")
                .setPositiveButton("Download", (dialog, i) -> {
                    dialog.dismiss();
                    try {
                        this.application.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")));
                    } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                        this.application.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) -> PreferenceManager.getDefaultSharedPreferences(this.application)
                        .edit()
                        .putBoolean(this.application.getString(R.string.spotify_key), false)
                        .apply());
        builder.show();
    }
}
