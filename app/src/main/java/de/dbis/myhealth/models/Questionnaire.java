package de.dbis.myhealth.models;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Questionnaire {

    private String id;
    private String title;
    private String description;
    private List<Question> questions;

    public Questionnaire() {
    }

    public Questionnaire(String title, String description, List<Question> questions) {
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "Questionnaire{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", questions=" + questions +
                '}';
    }
}
