package de.dbis.myhealth.util;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.dbis.myhealth.models.Question;

public class QuestionConverter {
    @TypeConverter
    public String fromQuestionList(List<Question> questionList) {
        if (questionList == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Question>>() {
        }.getType();
        String json = gson.toJson(questionList, type);
        Log.d("JSON", json);
        return json;
    }

    @TypeConverter
    public List<Question> toQuestionList(String json) {
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Question>>() {
        }.getType();
        List<Question> questionList = gson.fromJson(json, type);
        Log.d("questionList", questionList.toString());
        return questionList;
    }
}
