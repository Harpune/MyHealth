package de.dbis.myhealth.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.dbis.myhealth.util.converter.QuestionListConverter;

@Entity(tableName = "questionnaire_setting_table")
public class QuestionnaireSetting {
    @NonNull
    @PrimaryKey
    private String questionnaireId;
    @TypeConverters(QuestionListConverter.class)
    private List<Question> removedQuestions;

    @Ignore
    public QuestionnaireSetting() {
    }

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
