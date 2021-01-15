package de.dbis.myhealth.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "spotify_data_table")
public class SpotifyData {
    @NonNull
    @PrimaryKey
    private String deviceId;
    private String accessToken;

    @Ignore
    public SpotifyData() {
    }

    public SpotifyData(@NonNull String deviceId, String accessToken) {
        this.deviceId = deviceId;
        this.accessToken = accessToken;
    }

    @NonNull
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "SpotifyData{" +
                "deviceId='" + deviceId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
