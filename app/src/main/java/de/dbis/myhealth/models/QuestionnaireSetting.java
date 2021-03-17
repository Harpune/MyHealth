package de.dbis.myhealth.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestionnaireSetting {
    @NonNull
    private String questionnaireId;
    private List<Question> removedQuestions;

    public QuestionnaireSetting(@NotNull String questionnaireId, List<Question> removedQuestions) {
        this.questionnaireId = questionnaireId;
        this.removedQuestions = removedQuestions;
    }

    @NotNull
    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(@NotNull String questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public List<Question> getRemovedQuestions() {
        return removedQuestions;
    }

    public void addRemovedQuestions(Question question) {
        this.removedQuestions.add(question);
    }

    public void setRemovedQuestions(List<Question> removedQuestions) {
        this.removedQuestions = removedQuestions;
    }

    public void reAddQuestion(String removedQuestionTitle) {
        this.removedQuestions.removeIf(question -> question.getText().equalsIgnoreCase(removedQuestionTitle));
    }
}
