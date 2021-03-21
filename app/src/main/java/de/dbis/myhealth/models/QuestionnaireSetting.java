package de.dbis.myhealth.models;

import java.util.List;

public class QuestionnaireSetting {
    private String questionnaireId;
    private List<Question> removedQuestions;

    public QuestionnaireSetting(String questionnaireId, List<Question> removedQuestions) {
        this.questionnaireId = questionnaireId;
        this.removedQuestions = removedQuestions;
    }

    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(String questionnaireId) {
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
