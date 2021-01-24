package de.dbis.myhealth.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.List;

import de.dbis.myhealth.util.converter.DateConverter;
import de.dbis.myhealth.util.converter.IntegerListConverter;
import de.dbis.myhealth.util.converter.LongListConverter;

@Entity(tableName = "questionnaire_result_table")
public class QuestionnaireResult {
    @PrimaryKey
    @NonNull
    private String resultId;
    private String userId;
    private String trackId;
    @TypeConverters(DateConverter.class)
    private Date startExecutionDate;
    private long duration;
    private String questionnaireId;
    @TypeConverters(IntegerListConverter.class)
    private List<Integer> resultEntries;
    @TypeConverters(IntegerListConverter.class)
    private List<Integer> removedQuestions;
    @TypeConverters(LongListConverter.class)
    private List<Long> questionAnswerTime;

    @Ignore
    public QuestionnaireResult() {
    }

    public QuestionnaireResult(@NonNull String resultId, String userId, String trackId, Date startExecutionDate, long duration, String questionnaireId, List<Integer> resultEntries, List<Integer> removedQuestions, List<Long> questionAnswerTime) {
        this.resultId = resultId;
        this.userId = userId;
        this.trackId = trackId;
        this.startExecutionDate = startExecutionDate;
        this.duration = duration;
        this.questionnaireId = questionnaireId;
        this.resultEntries = resultEntries;
        this.removedQuestions = removedQuestions;
        this.questionAnswerTime = questionAnswerTime;
    }

    @NonNull
    public String getResultId() {
        return resultId;
    }

    public void setResultId(@NonNull String resultId) {
        this.resultId = resultId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(String questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public List<Integer> getResultEntries() {
        return resultEntries;
    }

    public void setResultEntries(List<Integer> resultEntries) {
        this.resultEntries = resultEntries;
    }

    public List<Integer> getRemovedQuestions() {
        return removedQuestions;
    }

    public void setRemovedQuestions(List<Integer> removedQuestions) {
        this.removedQuestions = removedQuestions;
    }

    public List<Long> getQuestionAnswerTime() {
        return questionAnswerTime;
    }

    public void setQuestionAnswerTime(List<Long> questionAnswerTime) {
        this.questionAnswerTime = questionAnswerTime;
    }
}
