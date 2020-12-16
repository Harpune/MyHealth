package de.dbis.myhealth.models;


import org.jetbrains.annotations.NotNull;

public class Question {
    private String text;
    private QuestionType type;

    public Question() {
    }

    public Question(String text, QuestionType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    @NotNull
    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }

    public enum QuestionType {
        YES_NO,
        YES_NO_MAYBE,
        SLIDER_1_10
    }
}
