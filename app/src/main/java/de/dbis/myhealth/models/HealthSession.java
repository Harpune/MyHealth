package de.dbis.myhealth.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HealthSession {
    private String id;
    private String userId;
    private Date date;
    @Exclude
    private List<QuestionnaireResult> questionnaireResults;
    private Map<String, ?> savedPreferences;
    private long timeAppOpened;
    private Map<String, SpotifySession> spotifySession;

    public HealthSession() {
    }

    public HealthSession(String userId, Date date, List<QuestionnaireResult> questionnaireResults, Map<String, ?> savedPreferences, long timeAppOpened, Map<String, SpotifySession> spotifySession) {
        this.userId = userId;
        this.date = date;
        this.questionnaireResults = questionnaireResults;
        this.savedPreferences = savedPreferences;
        this.timeAppOpened = timeAppOpened;
        this.spotifySession = spotifySession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<QuestionnaireResult> getQuestionnaireResults() {
        return questionnaireResults;
    }

    public void setQuestionnaireResults(List<QuestionnaireResult> questionnaireResults) {
        this.questionnaireResults = questionnaireResults;
    }

    public Map<String, ?> getSavedPreferences() {
        return savedPreferences;
    }

    public void setSavedPreferences(Map<String, ?> savedPreferences) {
        this.savedPreferences = savedPreferences;
    }

    public long getTimeAppOpened() {
        return timeAppOpened;
    }

    public void incrementTimeAppOpened(long intervalUpdate) {
        this.timeAppOpened += intervalUpdate;
    }

    public void setTimeAppOpened(long timeAppOpened) {
        this.timeAppOpened = timeAppOpened;
    }

    public Map<String, SpotifySession> getSpotifySession() {
        return spotifySession;
    }

    public void setSpotifySession(Map<String, SpotifySession> spotifySession) {
        this.spotifySession = spotifySession;
    }
}
