package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class LongListConverter {
    Gson gson = new Gson();
    Type type = new TypeToken<List<Long>>() {
    }.getType();

    @TypeConverter
    public String fromLongList(List<Long> resultList) {
        if (resultList == null) return null;
        return this.gson.toJson(resultList, this.type);
    }

    @TypeConverter
    public List<Long> toLongList(String json) {
        if (json == null) return null;
        return this.gson.fromJson(json, this.type);
    }
}
