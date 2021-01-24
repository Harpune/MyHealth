package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class StringListConverter {

    private final Gson gson = new Gson();
    private final Type type = new TypeToken<List<String>>() {
    }.getType();

    @TypeConverter
    public String fromStringList(List<String> stringList) {
        if (stringList == null) return null;
        return this.gson.toJson(stringList, this.type);
    }

    @TypeConverter
    public List<String> toStringList(String json) {
        if (json == null) return null;
        return this.gson.fromJson(json, this.type);
    }
}
