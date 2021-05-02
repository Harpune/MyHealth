package de.dbis.myhealth.ui.stats;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Gamification;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.QuestionnaireResult;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_RESULTS;
import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_SESSIONS;

public class StatsViewModel extends AndroidViewModel {
    private final String TAG = getClass().getSimpleName();

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private final MutableLiveData<List<HealthSession>> mAllHealthSessions;
    private final MutableLiveData<HealthSession> mCurrentHealthSession;

    private SharedPreferences mSharedPreferences;

    public StatsViewModel(@NonNull Application application) {
        super(application);

        // live data
        this.mAllHealthSessions = new MutableLiveData<>();
        this.mCurrentHealthSession = new MutableLiveData<>();

        // preferences
        this.mSharedPreferences = getApplication().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // firebase
        this.firestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();

        // settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        this.firestore.setFirestoreSettings(settings);

        // init
        this.startNewSession();
    }

    public void setAllHealthSessions(List<HealthSession> healthSessions) {
        this.mAllHealthSessions.setValue(healthSessions);
    }

    public void addHealthSession(HealthSession healthSession) {
        List<HealthSession> healthSessions = this.mAllHealthSessions.getValue();
        if (healthSessions == null) {
            healthSessions = new ArrayList<>();
        }

        // because of stream
        List<HealthSession> finalHealthSessions = healthSessions;

        // check if session already exists and replace if true
        if (finalHealthSessions.stream().anyMatch(savedSession -> savedSession.getId().equalsIgnoreCase(healthSession.getId()))) {
            // exists, then get index and set new healthSession at index
            OptionalInt indexOpt = IntStream.range(0, finalHealthSessions.size())
                    .filter(i -> finalHealthSessions.get(i).getId().equalsIgnoreCase(healthSession.getId()))
                    .findFirst();
            if (indexOpt.isPresent()) {
                finalHealthSessions.set(indexOpt.getAsInt(), healthSession);
            } else {
                finalHealthSessions.add(healthSession);
            }
        } else {
            finalHealthSessions.add(healthSession);
        }

        // re-add all sessions
        this.mAllHealthSessions.setValue(finalHealthSessions);
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

    public void incrementAppTime(long intervalUpdate) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.firestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("timeAppOpened", FieldValue.increment(intervalUpdate));
        }
    }

    public void incrementMusicTime(String trackId, long intervalUpdate) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.firestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("timeMusic." + trackId, FieldValue.increment(intervalUpdate));
        }
    }

    public void uploadQuestionnaireResult(QuestionnaireResult questionnaireResult) {
        HealthSession healthSession = this.getCurrentHealthSession().getValue();
        if (healthSession != null) {
            this.firestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .collection(FIREBASE_COLLECTION_RESULTS)
                    .document()
                    .set(questionnaireResult);
        }

    }

    public void startNewSession() {
        FirebaseUser firebaseUser = this.firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // get set preference
            Map<String, ?> preference = this.mSharedPreferences.getAll();
            preference.remove(getApplication().getString(R.string.access_token));

            // create session
            HealthSession startedHealthSession = new HealthSession(
                    firebaseUser.getUid(),
                    new Date(),
                    new ArrayList<>(),
                    preference,
                    0L,
                    new HashMap<>());

            // upload session
            DocumentReference documentReference = this.firestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document();
            startedHealthSession.setId(documentReference.getId());
            documentReference.set(startedHealthSession).addOnSuccessListener(aVoid -> firestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(documentReference.getId())
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            HealthSession healthSession = documentSnapshot.toObject(HealthSession.class);
                            setCurrentHealthSession(healthSession);
//                            addHealthSession(healthSession);
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }));
        }
    }

    public void loadHealthSessions(FirebaseUser firebaseUser) {
        // get health sessions
        this.firestore
                .collection(FIREBASE_COLLECTION_SESSIONS)
                .whereEqualTo("userId", firebaseUser.getUid())
                .get()
                .addOnCompleteListener(sessionTask -> {

                    if (sessionTask.isSuccessful()) {
                        QuerySnapshot sessionSnapshot = sessionTask.getResult();
                        if (sessionSnapshot != null) {

                            // collect sessions in list
                            sessionSnapshot.getDocuments().forEach(healthDocument -> {

                                // parse health session
                                HealthSession healthSession = healthDocument.toObject(HealthSession.class);
                                if (healthSession != null) {
                                    healthSession.setId(healthDocument.getId());
                                }

                                // get results of session
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

                                                addHealthSession(healthSession);

                                            }
                                        });
                            });
                        }
                    }
                });
    }

    public List<Gamification> getLocalGamifications() {
        List<Gamification> gamifications = new ArrayList<>();
        String[] ids = getApplication().getResources().getStringArray(R.array.gamification_keys);
        String[] images = getApplication().getResources().getStringArray(R.array.gamification_images);
        String[] descriptions = getApplication().getResources().getStringArray(R.array.gamification_descriptions);

        for (int i = 0; i < descriptions.length; i++) {
            Gamification gamification = new Gamification(ids[i], images[i], descriptions[i], new ArrayList<>());
            gamifications.add(gamification);
        }

        return gamifications;
    }

    public void generateGamifications() {
        String[] ids = getApplication().getResources().getStringArray(R.array.gamification_keys);
        String[] images = getApplication().getResources().getStringArray(R.array.gamification_images);
        String[] descriptions = getApplication().getResources().getStringArray(R.array.gamification_descriptions);

        for (int i = 0; i < descriptions.length; i++) {
            Gamification gamification = new Gamification(ids[i], images[i], descriptions[i], new ArrayList<>());
            this.firestore.collection("gamification").document(gamification.getId()).set(gamification);
        }
    }
}