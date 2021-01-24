package de.dbis.myhealth.ui.settings;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.fitness.SessionsClient;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import java.util.List;

import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.repository.SpotifyRepository;
import kaaes.spotify.webapi.android.SpotifyApi;

public class SettingsViewModel extends AndroidViewModel {

    private final static String TAG = "SettingsViewModel";

    // Google Fit
    private final MutableLiveData<SessionsClient> mSessionsClient;

    // Spotify
    private final SpotifyRepository mSpotifyRepository;

    public SettingsViewModel(Application application) {
        super(application);

        // Google Fit
        this.mSessionsClient = new MutableLiveData<>();

        // Spotify
        this.mSpotifyRepository = new SpotifyRepository(application);
    }

    public LiveData<SessionsClient> getSessionClient() {
        return this.mSessionsClient;
    }

    public void setSessionsClient(SessionsClient sessionsClient) {
        this.mSessionsClient.setValue(sessionsClient);
    }

    // SPOTIFY
    public void disconnect() {
        this.mSpotifyRepository.disconnect();
    }

    public void play(SpotifyTrack spotifyTrack) {
        this.mSpotifyRepository.play(spotifyTrack);
    }

    public void pause() {
        this.mSpotifyRepository.pause();
    }

    public LiveData<SpotifyAppRemote> getSpotifyRemoteApp() {
        return this.mSpotifyRepository.getSpotifyAppRemote();
    }

    public void setSpotifyAppRemote(SpotifyAppRemote spotifyAppRemote) {
        this.mSpotifyRepository.setSpotifyAppRemote(spotifyAppRemote);

        spotifyAppRemote.getPlayerApi().subscribeToPlayerState()
                .setEventCallback(this.mSpotifyRepository::setPlayerState)
                .setErrorCallback(error -> Log.d(TAG, "subscribeToPlayerState", error));

        spotifyAppRemote.getPlayerApi().subscribeToPlayerContext()
                .setEventCallback(this.mSpotifyRepository::setPlayerContext)
                .setErrorCallback(error -> Log.d(TAG, "subscribeToPlayerContext", error));
    }

    public LiveData<SpotifyApi> getSpotifyApi() {
        return this.mSpotifyRepository.getSpotifyApi();
    }

    public void setSpotifyApi(SpotifyApi spotifyApi) {
        this.mSpotifyRepository.setSpotifyApi(spotifyApi);
    }

    public LiveData<PlayerState> getPlayerState() {
        return this.mSpotifyRepository.getPlayerState();
    }

    public LiveData<PlayerContext> getPlayerContext() {
        return this.mSpotifyRepository.getPlayerContext();
    }

    public LiveData<SpotifyTrack> loadSpotifyTrack(String id) {
        return this.mSpotifyRepository.loadSpotifyTrack(id);
    }

    public void save(SpotifyTrack spotifyTrack) {
        this.mSpotifyRepository.insert(spotifyTrack);
    }

    public LiveData<List<SpotifyTrack>> getAllSpotifyTracks() {
        return this.mSpotifyRepository.getAllSpotifyTracks();
    }

    public LiveData<SpotifyTrack> getCurrentSpotifyTrack() {
        return this.mSpotifyRepository.getCurrentSpotifyTrack();
    }

    public void setCurrentSpotifyTrack(SpotifyTrack track) {
        this.mSpotifyRepository.setCurrentSpotifyTrack(track);
    }

    public LiveData<SpotifyTrack> getSpotifyTrackById(String id) {
        return this.mSpotifyRepository.getSpotifyTrack(id);
    }
}