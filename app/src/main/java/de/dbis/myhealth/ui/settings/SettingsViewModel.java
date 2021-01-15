package de.dbis.myhealth.ui.settings;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.fitness.SessionsClient;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.dbis.myhealth.models.SpotifyData;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.repository.SpotifyRepository;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;

public class SettingsViewModel extends AndroidViewModel {

    private final static String TAG = "SettingsViewModel";
    private final static String CLIENT_ID = "da07627d8dba46a88700c9ee8acb1832";
    private final static String CLIENT_SECRET = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String AUTH = "ODBiYzk3Y2RkZjlhNGEwZmExZmE1ZGYzMGM2ZjFjZDg6ZGEwNzYyN2Q4ZGJhNDZhODg3MDBjOWVlOGFjYjE4MzI=";

    private static final int REQUEST_CODE = 1337;
    private final static String SPOTIFY_CLIENT_ID = "80bc97cddf9a4a0fa1fa5df30c6f1cd8";
    private final static String SPOTIFY_REDIRECT_URI = "https://de.dbis.myhealth/callback";

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
    public LiveData<SpotifyData> setupSpotifyData() {
        return this.mSpotifyRepository.setupSpotifyData();
    }

    public void connect(@NotNull SpotifyData spotifyData) {
        this.mSpotifyRepository.connect(spotifyData.getAccessToken());
    }

    public void disconnect() {
        this.mSpotifyRepository.disconnect();
    }

    public void togglePlay() {
        PlayerState playerState = this.mSpotifyRepository.getPlayerState().getValue();
        if (playerState != null) {
            if (playerState.isPaused) {
                this.mSpotifyRepository.playTrack();
            } else {
                this.mSpotifyRepository.pause();
            }
        }
    }

    public void playTrack() {
        this.mSpotifyRepository.playTrack();
    }

    public LiveData<PlayerState> getPlayerState() {
        return this.mSpotifyRepository.getPlayerState();
    }

    public LiveData<PlayerContext> getPlayerContext() {
        return this.mSpotifyRepository.getPlayerContext();
    }

    public void loadTrack(String id) {
        this.mSpotifyRepository.loadTrack(id);
//        this.mSpotifyRepository.loadAudioFeaturesTrack(id);
    }

    public LiveData<SpotifyTrack> createdSpotifyTrack() {
        return this.mSpotifyRepository.createdSpotifyTrack();
    }

    public void saveSpotifyTrack(SpotifyTrack spotifyTrack) {
        this.mSpotifyRepository.saveSpotifyTrack(spotifyTrack);
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

    public LiveData<Track> getCurrentTrack() {
        return this.mSpotifyRepository.getCurrentTrack();
    }

    public LiveData<Boolean> isConnected() {
        return this.mSpotifyRepository.isConnected();
    }

    public LiveData<AudioFeaturesTrack> getAudioFeaturesTrack(String trackId) {
        this.mSpotifyRepository.loadAudioFeaturesTrack(trackId);
        return this.mSpotifyRepository.getCurrentAudioFeaturesTrack();
    }
}