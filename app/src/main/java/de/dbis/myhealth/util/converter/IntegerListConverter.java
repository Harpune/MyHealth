package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class IntegerListConverter {
    @TypeConverter
    public String fromIntegerList(List<Integer> resultList) {
        if (resultList == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        String json = gson.toJson(resultList, type);
        Log.d("JSON", json);
        return json;
    }

    @TypeConverter
    public List<Integer> toIntegerList(String json) {
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        List<Integer> resultList = gson.fromJson(json, type);
        Log.d("resultList", resultList.toString());
        return resultList;
    }
}
