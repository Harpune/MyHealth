package de.dbis.myhealth.util.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.dbis.myhealth.models.Question;

public class QuestionListConverter {
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<List<Question>>() {
    }.getType();

    @TypeConverter
    public String fromQuestionList(List<Question> questionList) {
        if (questionList == null) return null;
        return this.gson.toJson(questionList, this.type);
    }

    @TypeConverter
    public List<Question> toQuestionList(String json) {
        if (json == null) return null;
        return this.gson.fromJson(json, this.type);
    }
}
