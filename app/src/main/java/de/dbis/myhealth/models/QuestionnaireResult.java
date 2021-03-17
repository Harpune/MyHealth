package de.dbis.myhealth.models;

import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class QuestionnaireResult {
    @NotNull
    private String resultId;
    private String userId;
    private String questionnaireId;
    private String trackId;
    private Date startExecutionDate;
    private long duration;
    private double averageDuration;
    private List<QuestionResult> questionResults;

    public QuestionnaireResult() {
    }

    public QuestionnaireResult(String questionnaireId, String userId, String trackId, Date startExecutionDate, long duration, double averageDuration, List<QuestionResult> questionResults) {
        this.questionnaireId = questionnaireId;
        this.userId = userId;
        this.trackId = trackId;
        this.startExecutionDate = startExecutionDate;
        this.duration = duration;
        this.averageDuration = averageDuration;
        this.questionResults = questionResults;
    }

    @NotNull
    @Exclude
    public String getResultId() {
        return resultId;
    }

    public void setResultId(@NotNull String resultId) {
        this.resultId = resultId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(String questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public Date getStartExecutionDate() {
        return startExecutionDate;
    }

    public void setStartExecutionDate(Date startExecutionDate) {
        this.startExecutionDate = startExecutionDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(double averageDuration) {
        this.averageDuration = averageDuration;
    }

    public List<QuestionResult> getQuestionResults() {
        return questionResults;
    }

    public void setQuestionResults(List<QuestionResult> questionResults) {
        this.questionResults = questionResults;
    }
}
