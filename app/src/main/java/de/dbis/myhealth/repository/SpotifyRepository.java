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

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.dao.SpotifyTrackDao;
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
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

    private final MutableLiveData<SpotifyApi> mSpotifyApi;
    private final MutableLiveData<SpotifyAppRemote> mSpotifyAppRemote;
    private final MutableLiveData<PlayerState> mPlayerState;
    private final MutableLiveData<PlayerContext> mPlayerContext;

    private final MutableLiveData<SpotifyTrack> mSpotifyTrack;

    private final SpotifyTrackDao mSpotifyTrackDao;
    private final Application application;

    public SpotifyRepository(Application application) {
        this.application = application;

        // db
        AppDatabase db = AppDatabase.getInstance(application);
        this.mSpotifyTrackDao = db.spotifyTrackDao();

        // Live Data
        this.mSpotifyApi = new MutableLiveData<>();
        this.mSpotifyAppRemote = new MutableLiveData<>();
        this.mPlayerState = new MutableLiveData<>();
        this.mPlayerContext = new MutableLiveData<>();
        this.mSpotifyTrack = new MutableLiveData<>();
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
            this.mSpotifyAppRemote.setValue(null);
        }
    }

    public LiveData<SpotifyTrack> loadSpotifyTrack(String id) {
        // LiveData updated by SpotifyAPI-Callbacks
        MutableLiveData<Track> trackLiveData = new MutableLiveData<>();
        MutableLiveData<AudioFeaturesTrack> audioFeaturesTrackLiveData = new MutableLiveData<>();

        // MediatorLiveData returns all callback data if present
        MediatorLiveData<SpotifyTrack> mediatorLiveData = new MediatorLiveData<>();

        // Check if audioFeaturesTrackLiveData is finished on Track-Callback
        mediatorLiveData.addSource(trackLiveData, value -> {
            Track track = trackLiveData.getValue();
            AudioFeaturesTrack audioFeaturesTrack = audioFeaturesTrackLiveData.getValue();
            if (track != null && audioFeaturesTrack != null) {
                SpotifyTrack spotifyTrack = new SpotifyTrack(track.id);
                spotifyTrack.setTrack(track);
                spotifyTrack.setAudioFeaturesTrack(audioFeaturesTrack);
                AppDatabase.databaseWriteExecutor.execute(() -> this.mSpotifyTrackDao.insert(spotifyTrack));
                mediatorLiveData.setValue(spotifyTrack);
            }
        });

        // Check if trackLiveData is finished on AudioFeaturesTrack-Callback
        mediatorLiveData.addSource(audioFeaturesTrackLiveData, value -> {
            Track track = trackLiveData.getValue();
            AudioFeaturesTrack audioFeaturesTrack = audioFeaturesTrackLiveData.getValue();
            if (track != null && audioFeaturesTrack != null) {
                SpotifyTrack spotifyTrack = new SpotifyTrack(track.id);
                spotifyTrack.setTrack(track);
                spotifyTrack.setAudioFeaturesTrack(audioFeaturesTrack);
                AppDatabase.databaseWriteExecutor.execute(() -> this.mSpotifyTrackDao.insert(spotifyTrack));
                mediatorLiveData.setValue(spotifyTrack);
            }
        });

        // Access data
        SpotifyApi spotifyApi = this.mSpotifyApi.getValue();
        if (spotifyApi != null) {
            // Get
            spotifyApi.getService().getTrack(id, new Callback<Track>() {
                @Override
                public void success(Track track, Response response) {
                    trackLiveData.setValue(track);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Get Track", error);
                }
            });

            spotifyApi.getService().getTrackAudioFeatures(id, new Callback<AudioFeaturesTrack>() {
                @Override
                public void success(AudioFeaturesTrack audioFeaturesTrack, Response response) {
                    audioFeaturesTrackLiveData.setValue(audioFeaturesTrack);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Get Track", error);
                }
            });
        }

        return mediatorLiveData;
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

    public void playSpotifyTrack() {
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

    public void play(SpotifyTrack spotifyTrack) {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().setShuffle(false);
            spotifyAppRemote.getPlayerApi().setRepeat(Repeat.ALL);

            spotifyAppRemote.getPlayerApi().play(spotifyTrack.getTrack().uri);
        }
    }

    public void pause() {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public LiveData<SpotifyAppRemote> getSpotifyAppRemote() {
        return this.mSpotifyAppRemote;
    }

    public LiveData<SpotifyApi> getSpotifyApi() {
        return this.mSpotifyApi;
    }

    public LiveData<PlayerState> getPlayerState() {
        return mPlayerState;
    }

    public LiveData<PlayerContext> getPlayerContext() {
        return mPlayerContext;
    }

    private final ConnectionParams mConnectionParams = new ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
            .showAuthView(true)
            .setRedirectUri(SPOTIFY_REDIRECT_URI)
            .build();

    private final Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            Log.d(TAG, "Connected to Spotify!");
            spotifyAppRemote.getPlayerApi().subscribeToPlayerState()
                    .setEventCallback(mPlayerState::setValue)
                    .setErrorCallback(error -> Log.d(TAG, "subscribeToPlayerState 123", error));
            spotifyAppRemote.getPlayerApi().subscribeToPlayerContext().setEventCallback(mPlayerContext::setValue)
                    .setErrorCallback(error -> Log.d(TAG, "subscribeToPlayerContext", error));

            mSpotifyAppRemote.setValue(spotifyAppRemote);
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
