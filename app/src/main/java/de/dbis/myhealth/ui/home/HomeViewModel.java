package de.dbis.myhealth.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.spotify.protocol.types.PlayerState;

import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Track> mTrack;
    private final MutableLiveData<AudioFeaturesTrack> mAudioFeaturesTrack;
    private final MutableLiveData<PlayerState> mPlayerState;


    public HomeViewModel() {
        mTrack = new MutableLiveData<>();
        mAudioFeaturesTrack = new MutableLiveData<>();
        mPlayerState = new MutableLiveData<>();
    }

    public void setTrack(Track track) {
        this.mTrack.setValue(track);
    }

    public LiveData<Track> getTrack() {
        return mTrack;
    }

    public void setAudioFeaturesTrack(AudioFeaturesTrack audioFeaturesTrack) {
        this.mAudioFeaturesTrack.setValue(audioFeaturesTrack);
    }

    public LiveData<AudioFeaturesTrack> getAudioFeaturesTrack() {
        return mAudioFeaturesTrack;
    }

    public void setPlayerState(PlayerState playerState) {
        this.mPlayerState.setValue(playerState);
    }

    public MutableLiveData<PlayerState> getPlayerState() {
        return mPlayerState;
    }
}