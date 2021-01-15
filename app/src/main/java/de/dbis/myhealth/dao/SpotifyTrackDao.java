package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.SpotifyTrack;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Track;

@Dao
public interface SpotifyTrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SpotifyTrack spotifyTrack);

    @Query("SELECT EXISTS(SELECT * FROM spotify_track_table WHERE trackId = :trackId)")
    boolean exists(String trackId);

    @Query("SELECT * FROM spotify_track_table ORDER BY trackId ASC")
    LiveData<List<SpotifyTrack>> getAll();

    @Query("SELECT * FROM spotify_track_table WHERE trackId = :trackId")
    LiveData<SpotifyTrack> getByTrackId(String trackId);

    @Query("UPDATE spotify_track_table SET track = :track WHERE trackId = :trackId")
    void updateTrack(String trackId, Track track);

    @Query("UPDATE spotify_track_table SET audioFeaturesTrack = :audioFeaturesTrack WHERE trackId = :trackId")
    void updateAudioFeaturesTrack(String trackId, AudioFeaturesTrack audioFeaturesTrack);


    @Query("DELETE FROM spotify_track_table")
    void deleteAll();
}
