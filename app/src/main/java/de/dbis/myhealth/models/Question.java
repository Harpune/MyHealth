package de.dbis.myhealth.models;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "question_table")
public class Question implements Cloneable {
    @PrimaryKey
    private String text;
    private QuestionType questionType;
    @Exclude
    private Integer result;

    public Question() {
    }

    public Question(String text, QuestionType questionType) {
        this.text = text;
        this.questionType = questionType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    @Exclude
    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        Log.d("QuestionnaireResult", result.toString());
        this.result = result;
    }

    @NotNull
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Question)) {
            return false;
        }

        Question q = (Question) obj;
        return q.getText().equalsIgnoreCase(this.getText());
    }

    @NotNull
    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", questionType=" + questionType +
                '}';
    }

    public enum QuestionType {
        YES_NO,
        YES_NO_SOMETIMES,
        SLIDER_0_10,
        SLIDER_0_100;
    }

    public enum ResultType {
        YES_NO,
        YES_NO_MAYBE,
        SCALED
    }
}
