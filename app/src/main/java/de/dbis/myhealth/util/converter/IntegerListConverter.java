package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class IntegerListConverter {

    private final Gson gson = new Gson();
    private final Type type = new TypeToken<List<Long>>() {
    }.getType();

    @TypeConverter
    public String fromIntegerList(List<Integer> resultList) {
        if (resultList == null) return null;
        return this.gson.toJson(resultList, this.type);
    }

    @TypeConverter
    public List<Integer> toIntegerList(String json) {
        if (json == null) return null;
        return this.gson.fromJson(json, this.type);
    }
}
