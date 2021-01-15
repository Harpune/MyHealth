package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.SpotifyData;

@Dao
public interface SpotifyDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SpotifyData spotifyData);

    @Query("SELECT EXISTS(SELECT * FROM spotify_data_table WHERE deviceId = :deviceId)")
    boolean exists(String deviceId);

    @Query("UPDATE spotify_data_table SET accessToken = :accessToken WHERE deviceId = :deviceId")
    void updateAccessToken(String deviceId, String accessToken);

    @Query("SELECT * FROM spotify_data_table ORDER BY deviceId ASC")
    LiveData<List<SpotifyData>> getAll();

    @Query("SELECT * FROM spotify_data_table WHERE deviceId = :deviceId")
    LiveData<SpotifyData> getByDeviceId(String deviceId);

    @Query("DELETE FROM spotify_track_table")
    void deleteAll();
}

