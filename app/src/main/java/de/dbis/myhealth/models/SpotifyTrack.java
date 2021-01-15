package de.dbis.myhealth.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import de.dbis.myhealth.util.converter.AudioFeaturesTrackConverter;
import de.dbis.myhealth.util.converter.TrackConverter;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;


@Entity(tableName = "spotify_track_table")
public class SpotifyTrack {
    @NonNull
    @PrimaryKey
    private String trackId;
    @TypeConverters(TrackConverter.class)
    private Track track;
    @TypeConverters(AudioFeaturesTrackConverter.class)
    private AudioFeaturesTrack audioFeaturesTrack;
    private long minutesListened;

    @Ignore
    public SpotifyTrack() {
    }

    @Ignore
    public SpotifyTrack(@NotNull String trackId) {
        this.trackId = trackId;
    }

    public SpotifyTrack(@NonNull String trackId, Track track, AudioFeaturesTrack audioFeaturesTrack, long minutesListened) {
        this.trackId = trackId;
        this.track = track;
        this.audioFeaturesTrack = audioFeaturesTrack;
        this.minutesListened = minutesListened;
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

    public long getMinutesListened() {
        return minutesListened;
    }

    public void setMinutesListened(long minutesListened) {
        this.minutesListened = minutesListened;
    }
}
