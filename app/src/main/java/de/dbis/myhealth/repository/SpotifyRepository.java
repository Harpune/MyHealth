package de.dbis.myhealth.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.util.List;

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
                    Toast.makeText(application, error.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(application, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        return mediatorLiveData;
    }

    public void insert(SpotifyTrack spotifyTrack) {
        AppDatabase.databaseWriteExecutor.execute(() -> this.mSpotifyTrackDao.insert(spotifyTrack));
    }

    public LiveData<SpotifyTrack> getCurrentSpotifyTrack() {
        return this.mSpotifyTrack;
    }

    public void setCurrentSpotifyTrack(SpotifyTrack track) {
        this.mSpotifyTrack.setValue(track);
    }

    public LiveData<SpotifyTrack> getSpotifyTrack(String id) {
        return this.mSpotifyTrackDao.getSpotifyTrackById(id);
    }

    public LiveData<List<SpotifyTrack>> getAllSpotifyTracks() {
        return this.mSpotifyTrackDao.getAll();
    }

    public void play(SpotifyTrack spotifyTrack) {
        SpotifyAppRemote spotifyAppRemote = this.mSpotifyAppRemote.getValue();
        if (spotifyAppRemote != null) {
            spotifyAppRemote.getPlayerApi().setShuffle(false);
            spotifyAppRemote.getPlayerApi().setRepeat(Repeat.ALL);

            spotifyAppRemote.getPlayerApi().play(spotifyTrack.getTrack().uri);
        } else {
            Log.d(TAG, "!!!!! Couldn't play: " + spotifyTrack.getTrack().name);
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

    public void setSpotifyAppRemote(SpotifyAppRemote spotifyAppRemote) {
        this.mSpotifyAppRemote.setValue(spotifyAppRemote);
    }

    public LiveData<SpotifyApi> getSpotifyApi() {
        return this.mSpotifyApi;
    }

    public void setSpotifyApi(SpotifyApi spotifyApi) {
        this.mSpotifyApi.setValue(spotifyApi);
    }

    public LiveData<PlayerState> getPlayerState() {
        return mPlayerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.mPlayerState.setValue(playerState);
    }

    public LiveData<PlayerContext> getPlayerContext() {
        return mPlayerContext;
    }

    public void setPlayerContext(PlayerContext playerContext) {
        this.mPlayerContext.setValue(playerContext);
    }
}
