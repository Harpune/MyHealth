package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import kaaes.spotify.webapi.android.models.Track;

public class TrackConverter {

    private final Gson gson = new Gson();
    private final Type type = new TypeToken<Track>() {
    }.getType();

    @TypeConverter
    public String fromTrackList(Track track) {
        if (track == null) return null;
        return this.gson.toJson(track, this.type);
    }

    @TypeConverter
    public Track toTrack(String json) {
        if (json == null) return null;
        return this.gson.fromJson(json, this.type);
    }
}
