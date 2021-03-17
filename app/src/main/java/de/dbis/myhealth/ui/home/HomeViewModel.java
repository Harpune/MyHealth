package de.dbis.myhealth.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.spotify.protocol.types.PlayerState;

import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;

public class HomeViewModel extends AndroidViewModel {

    private final MutableLiveData<Track> mTrack;
    private final MutableLiveData<AudioFeaturesTrack> mAudioFeaturesTrack;
    private final MutableLiveData<PlayerState> mPlayerState;


    public HomeViewModel(Application application) {
        super(application);

        // Spotify
        this.mTrack = new MutableLiveData<>();
        this.mAudioFeaturesTrack = new MutableLiveData<>();
        this.mPlayerState = new MutableLiveData<>();
    }

    public void setTrack(Track track) {
        this.mTrack.setValue(track);
    }

    public LiveData<Track> getTrack() {
        return this.mTrack;
    }

    public void setAudioFeaturesTrack(AudioFeaturesTrack audioFeaturesTrack) {
        this.mAudioFeaturesTrack.setValue(audioFeaturesTrack);
    }

    public LiveData<AudioFeaturesTrack> getAudioFeaturesTrack() {
        return this.mAudioFeaturesTrack;
    }

    public void setPlayerState(PlayerState playerState) {
        this.mPlayerState.setValue(playerState);
    }

    public LiveData<PlayerState> getPlayerState() {
        return this.mPlayerState;
    }

}