package de.dbis.myhealth.ui.stats;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Gamification;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.ui.home.HomeViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_GAMIFICATION;
import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_RESULTS;
import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_SESSIONS;

public class StatsViewModel extends AndroidViewModel {
    private final String TAG = getClass().getSimpleName();

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private final MutableLiveData<List<Gamification>> mGamifications;
    private final MutableLiveData<List<HealthSession>> mHealthSessions;
    private final MutableLiveData<HealthSession> mHealthSession;

    private SharedPreferences mSharedPreferences;

    public StatsViewModel(@NonNull Application application) {
        super(application);

        // live data
        this.mGamifications = new MutableLiveData<>();
        this.mHealthSessions = new MutableLiveData<>();
        this.mHealthSession = new MutableLiveData<>();

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

    public LiveData<List<Gamification>> getGamifications() {
        return this.mGamifications;
    }

    public void setGamifications(List<Gamification> gamification) {
        this.mGamifications.setValue(gamification);
    }

    public void setHealthSessions(List<HealthSession> healthSessions) {
        this.mHealthSessions.setValue(healthSessions);
    }

    public LiveData<List<HealthSession>> getHealthSessions() {
        return this.mHealthSessions;
    }

    public void setHealthSession(HealthSession healthSession) {
        this.mHealthSession.setValue(healthSession);
    }

    public LiveData<HealthSession> getHealthSession() {
        return this.mHealthSession;
    }

    public void incrementAppTime(long intervalUpdate) {
        HealthSession healthSession = this.getHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.firestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("timeAppOpened", FieldValue.increment(intervalUpdate));
        }
    }

    public void incrementMusicTime(String trackId, long intervalUpdate) {
        HealthSession healthSession = this.getHealthSession().getValue();
        if (healthSession != null) {
            // update on server
            this.firestore
                    .collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .update("timeMusic." + trackId, FieldValue.increment(intervalUpdate));
        }
    }

    public void addQuestionnaireResult(QuestionnaireResult questionnaireResult) {
        HealthSession healthSession = this.getHealthSession().getValue();
        if (healthSession != null) {
            this.firestore.collection(FIREBASE_COLLECTION_SESSIONS)
                    .document(healthSession.getId())
                    .collection(FIREBASE_COLLECTION_RESULTS)
                    .document();
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
            DocumentReference documentReference = this.firestore.collection(FIREBASE_COLLECTION_SESSIONS).document();
            startedHealthSession.setId(documentReference.getId());
            documentReference.set(startedHealthSession);

            // create listener
            documentReference.addSnapshotListener(MetadataChanges.INCLUDE, (documentSnapshot, error) -> {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    HealthSession healthSession = documentSnapshot.toObject(HealthSession.class);
                    this.setHealthSession(healthSession);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            });
        }
    }

    public void loadHealthSessions(FirebaseUser firebaseUser) {
        this.firestore.collection(FIREBASE_COLLECTION_SESSIONS)
                .whereEqualTo("userId", firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            List<HealthSession> healthSessions = querySnapshot.getDocuments().stream().map(document -> {
                                HealthSession healthSession = document.toObject(HealthSession.class);
                                if (healthSession != null) {
                                    healthSession.setId(document.getId());
                                }
                                return healthSession;
                            }).collect(Collectors.toList());

                            this.setHealthSessions(healthSessions);
                        }
                    }
                });
    }

    public void loadGamifications() {
        this.firestore.collection(FIREBASE_COLLECTION_GAMIFICATION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            List<Gamification> gamifications = querySnapshot.getDocuments().stream().map(document -> document.toObject(Gamification.class)).collect(Collectors.toList());
                            this.setGamifications(gamifications);
                        }
                    }
                });
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