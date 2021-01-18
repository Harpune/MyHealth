package de.dbis.myhealth.ui.settings;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.fitness.SessionsClient;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import java.util.List;

import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.repository.SpotifyRepository;

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
    public void connect(String accessToken) {
        this.mSpotifyRepository.connect(accessToken);
    }

    public void disconnect() {
        this.mSpotifyRepository.disconnect();
    }

    public void togglePlay() {
        PlayerState playerState = this.mSpotifyRepository.getPlayerState().getValue();
        if (playerState != null) {
            if (playerState.isPaused) {
                this.mSpotifyRepository.playSpotifyTrack();
            } else {
                this.mSpotifyRepository.pause();
            }
        }
    }

    public void play(SpotifyTrack spotifyTrack) {
        this.mSpotifyRepository.play(spotifyTrack);
    }

    public void playSpotifyTrack() {
        this.mSpotifyRepository.playSpotifyTrack();
    }

    public void pause() {
        this.mSpotifyRepository.pause();
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

    public LiveData<Boolean> isConnected() {
        return this.mSpotifyRepository.isConnected();
    }
}