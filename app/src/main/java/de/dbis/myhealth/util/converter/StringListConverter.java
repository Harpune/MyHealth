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
        if (stringList == null) {
            return null;
        }

        String json = this.gson.toJson(stringList, this.type);
        Log.d("JSON-stringList", json);
        return json;
    }

    @TypeConverter
    public List<String> toStringList(String json) {
        if (json == null) {
            return null;
        }

        List<String> stringList = this.gson.fromJson(json, this.type);
        Log.d("stringList", stringList.toString());
        return stringList;
    }
}
