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

import de.dbis.myhealth.models.SpotifyTrack;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Deprecated
public class SpotifyRepository {
    private final static String TAG = "SpotifyRepository";

    private final MutableLiveData<SpotifyApi> mSpotifyApi;
    private final MutableLiveData<SpotifyAppRemote> mSpotifyAppRemote;
    private final MutableLiveData<PlayerState> mPlayerState;
    private final MutableLiveData<PlayerContext> mPlayerContext;

    private final MutableLiveData<SpotifyTrack> mSpotifyTrack;

    private final Application application;

    public SpotifyRepository(Application application) {
        this.application = application;

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



    public LiveData<SpotifyTrack> getCurrentSpotifyTrack() {
        return this.mSpotifyTrack;
    }

    public void setCurrentSpotifyTrack(SpotifyTrack track) {
        this.mSpotifyTrack.setValue(track);
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
