package de.dbis.myhealth.ui.stats;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.SpotifySession;
import de.dbis.myhealth.ui.user.UserViewModel;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_RESULTS;
import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_SESSIONS;

public class StatsViewModel extends AndroidViewModel {
    private final String TAG = getClass().getSimpleName();

    private UserViewModel mUserViewModel;
    private final FirebaseFirestore mFirestore;

    private final MutableLiveData<List<HealthSession>> mAllHealthSessions;
    private final MutableLiveData<HealthSession> mCurrentHealthSession;
    private final MutableLiveData<String> mCurrentHealthSessionId;

    private final SharedPreferences mSharedPreferences;

    public StatsViewModel(@NonNull Application application) {
        super(application);

        // live data
        this.mAllHealthSessions = new MutableLiveData<>();
        this.mCurrentHealthSession = new MutableLiveData<>();
        this.mCurrentHealthSessionId = new MutableLiveData<>();

        // preferences
        this.mSharedPreferences = getApplication().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // firebase
        this.mFirestore = FirebaseFirestore.getInstance();

        // settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        this.mFirestore.setFirestoreSettings(settings);
    }

    public void setAllHealthSessions(List<HealthSession> healthSessions) {
        this.mAllHealthSessions.setValue(healthSessions);
    }

    public void addHealthSession(HealthSession healthSession) {
        final List<HealthSession> healthSessions = this.getAllHealthSessions().getValue() != null ? this.getAllHealthSessions().getValue() : new ArrayList<>();

        // check if session already exists and replace if true
        if (healthSessions.stream().anyMatch(savedSession -> savedSession.getId().equalsIgnoreCase(healthSession.getId()))) {
            // exists, then get index and set new healthSession at index
            OptionalInt indexOpt = IntStream.range(0, healthSessions.size())
                    .filter(i -> healthSessions.get(i).getId().equalsIgnoreCase(healthSession.getId()))
                    .findFirst();
            if (indexOpt.isPresent()) {
                healthSessions.set(indexOpt.getAsInt(), healthSession);
            } else {
                healthSessions.add(healthSession);
            }
        } else {
            healthSessions.add(healthSession);
        }

//        healthSessions.sort((h1, h2) -> h1.getDate().compareTo(h2.getDate()));


        this.setAllHealthSessions(healthSessions);
    }

    public LiveData<List<HealthSession>> getAllHealthSessions() {
        return this.mAllHealthSessions;
    }

    public void setCurrentHealthSession(HealthSession healthSession) {
        this.mCurrentHealthSession.setValue(healthSession);
    }

    public LiveData<HealthSession> getCurrentHealthSession() {
        return this.mCurrentHealthSession;
    }

    public LiveData<String> getCurrentHealthSessionId() {
        return this.mCurrentHealthSessionId;
    }

    public void setCurrentHealthSessionId(String sessionId) {
        this.mCurrentHealthSessionId.setValue(sessionId);
    }

    public void incrementAppTime(long intervalUpdate) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.mFirestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("timeAppOpened", FieldValue.increment(intervalUpdate));
        }
    }

    public void incrementMusicTime(String trackId, long intervalUpdate) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("spotifySession." + trackId + ".time", FieldValue.increment(intervalUpdate));
        }
    }

    public void setVolume(String trackId, int volume) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("spotifySession." + trackId + ".volume", volume);
        }
    }

    public void updatePreference() {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            Map<String, Object> preferences = this.getPreferences();
            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("savedPreferences", preferences);
        }
    }

    public void addSpotifySession(String currentSpotifyTrack) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {

            int volume = this.mSharedPreferences.getInt(getApplication().getString(R.string.spotify_volume_key), 25);

            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("spotifySession." + currentSpotifyTrack + ".id", currentSpotifyTrack);
            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("spotifySession." + currentSpotifyTrack + ".volume", volume);
            this.mFirestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("spotifySession." + currentSpotifyTrack + ".time", FieldValue.increment(0));
        }
    }

    public void uploadQuestionnaireResult(QuestionnaireResult questionnaireResult) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            this.mFirestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .collection(FIREBASE_COLLECTION_RESULTS)
                    .document()
                    .set(questionnaireResult);
        }
    }

    public void startNewSession(FirebaseUser firebaseUser) {
        // get set preference
        Map<String, Object> preference = this.getPreferences();

        // get spotify session
        String currentSpotifyTrack = this.mSharedPreferences.getString(getApplication().getString(R.string.current_spotify_track_key), "unknown");
        SpotifySession spotifySession = new SpotifySession();
        spotifySession.setId(currentSpotifyTrack);
        spotifySession.setTime(0L);
        spotifySession.setVolume(this.mSharedPreferences.getInt(getApplication().getString(R.string.spotify_volume_key), 25));

        Map<String, SpotifySession> spotifySessions = new HashMap<>();
        spotifySessions.put(currentSpotifyTrack, spotifySession);

        // create session
        HealthSession startedHealthSession = new HealthSession(
                firebaseUser.getUid(),
                new Date(),
                new ArrayList<>(),
                preference,
                0L,
                spotifySessions);

        // create session reference
        DocumentReference documentReference = this.mFirestore
                .collection(FIREBASE_COLLECTION_SESSIONS)
                .document();

        // set id
        startedHealthSession.setId(documentReference.getId());
        this.setCurrentHealthSessionId(documentReference.getId());

        // upload session and listen for updates eg new questionnaire results
        documentReference.set(startedHealthSession).addOnSuccessListener(aVoid -> mFirestore
                .collection(FIREBASE_COLLECTION_SESSIONS)
                .document(documentReference.getId())
                .addSnapshotListener((healthDocument, error) -> {
                    {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (healthDocument != null && healthDocument.exists()) {
                            HealthSession healthSession = healthDocument.toObject(HealthSession.class);
                            if (healthSession != null) {
                                healthDocument.getReference()
                                        .collection(FIREBASE_COLLECTION_RESULTS)
                                        .whereEqualTo("userId", firebaseUser.getUid())
                                        .get()
                                        .addOnCompleteListener(resultTask -> {
                                            if (resultTask.isSuccessful()) {
                                                QuerySnapshot resultSnapshot = resultTask.getResult();
                                                if (resultSnapshot != null && !resultSnapshot.isEmpty()) {
                                                    List<QuestionnaireResult> questionnaireResults = resultSnapshot.getDocuments().stream().map(resultDocument -> {
                                                        QuestionnaireResult questionnaireResult = resultDocument.toObject(QuestionnaireResult.class);
                                                        if (questionnaireResult != null) {
                                                            questionnaireResult.setResultId(resultDocument.getId());
                                                        }
                                                        return questionnaireResult;
                                                    }).collect(Collectors.toList());

                                                    // add results
                                                    healthSession.setQuestionnaireResults(questionnaireResults);
                                                } else {
                                                    healthSession.setQuestionnaireResults(new ArrayList<>());
                                                }
                                            }

                                            addHealthSession(healthSession);
                                            setCurrentHealthSession(healthSession);
                                        });
                            }
                        }
                    }
                }));
    }

    public void loadHealthSessions(FirebaseUser firebaseUser, HealthSession currentHealthSession) {
        this.mFirestore
                .collection(FIREBASE_COLLECTION_SESSIONS)
                .whereEqualTo("userId", firebaseUser.getUid())
                .whereNotEqualTo("id", currentHealthSession.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot sessionSnapshot = task.getResult();
                        if (sessionSnapshot != null) {

                            // collect sessions in list
                            sessionSnapshot.getDocuments().forEach(healthDocument -> {

                                // parse health session
                                HealthSession healthSession = healthDocument.toObject(HealthSession.class);
                                if (healthSession != null) {
                                    healthSession.setId(healthDocument.getId());
                                }

                                // get results of this session
                                healthDocument.getReference()
                                        .collection(FIREBASE_COLLECTION_RESULTS)
                                        .whereEqualTo("userId", firebaseUser.getUid())
                                        .get()
                                        .addOnCompleteListener(resultTask -> {
                                            // parse questionnaire result
                                            if (resultTask.isSuccessful()) {
                                                QuerySnapshot resultSnapshot = resultTask.getResult();
                                                if (resultSnapshot != null && !resultSnapshot.isEmpty()) {
                                                    List<QuestionnaireResult> questionnaireResults = resultSnapshot.getDocuments().stream().map(resultDocument -> {
                                                        QuestionnaireResult questionnaireResult = resultDocument.toObject(QuestionnaireResult.class);
                                                        if (questionnaireResult != null) {
                                                            questionnaireResult.setResultId(resultDocument.getId());
                                                        }
                                                        return questionnaireResult;
                                                    }).collect(Collectors.toList());

                                                    // add results
                                                    healthSession.setQuestionnaireResults(questionnaireResults);
                                                } else {
                                                    healthSession.setQuestionnaireResults(new ArrayList<>());
                                                }
                                            }

                                            if (resultTask.isCanceled()) {
                                                Log.e(TAG, "Could not get questionnaire results of this session", resultTask.getException());
                                                healthSession.setQuestionnaireResults(new ArrayList<>());
                                            }

                                            addHealthSession(healthSession);
                                        });
                            });
                        }
                    }
                });
    }

    private Map<String, Object> getPreferences() {
        Map<String, Object> preference = new HashMap<>();
        try {
            preference = (Map<String, Object>) this.mSharedPreferences.getAll();
            preference.remove(getApplication().getString(R.string.access_token));
            HashSet<String> gamificationMap = (HashSet<String>) preference.get(getApplication().getString(R.string.general_gamification_key));
            List<String> gamificationList = new ArrayList<>(gamificationMap);

            preference.put(getApplication().getString(R.string.general_gamification_key), gamificationList);

        } catch (Exception e) {
            Log.e(TAG, "Casting error", e);
        }
        return preference;
    }
}