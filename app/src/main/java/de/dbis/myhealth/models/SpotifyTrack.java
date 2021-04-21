package de.dbis.myhealth.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


public class SpotifyTrack {
    @NonNull
    private String trackId;
    private Track track;
    private AudioFeaturesTrack audioFeaturesTrack;
    private Image image;

    public SpotifyTrack(@NotNull String trackId) {
        this.trackId = trackId;
    }

    public SpotifyTrack(@NonNull String trackId, Track track, AudioFeaturesTrack audioFeaturesTrack) {
        this.trackId = trackId;
        this.track = track;
        this.audioFeaturesTrack = audioFeaturesTrack;
    }

    @NonNull
    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(@NonNull String trackId) {
        this.trackId = trackId;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public AudioFeaturesTrack getAudioFeaturesTrack() {
        return audioFeaturesTrack;
    }

    public void setAudioFeaturesTrack(AudioFeaturesTrack audioFeaturesTrack) {
        this.audioFeaturesTrack = audioFeaturesTrack;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
