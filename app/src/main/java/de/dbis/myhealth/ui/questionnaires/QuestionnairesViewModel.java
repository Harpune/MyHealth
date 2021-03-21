package de.dbis.myhealth.ui.questionnaires;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_QUESTIONNAIRES;
import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_RESULTS;

public class QuestionnairesViewModel extends AndroidViewModel {
    private final static String TAG = "QuestionnairesViewModel";

    private Preference mPreference;

    private final MutableLiveData<List<Questionnaire>> mAllQuestionnaires;
    private final MutableLiveData<Questionnaire> mSelectedQuestionnaire;
    private final MutableLiveData<List<QuestionnaireResult>> mAllQuestionnaireResults;
    private final MutableLiveData<QuestionnaireSetting> mQuestionnaireSetting;

    // Firestore
    private final FirebaseFirestore firestore;

    public QuestionnairesViewModel(Application application) {
        super(application);

        // preference
        this.mPreference = PowerPreference.getDefaultFile();

        // liveData
        this.mAllQuestionnaires = new MutableLiveData<>();
        this.mAllQuestionnaireResults = new MutableLiveData<>();
        this.mSelectedQuestionnaire = new MutableLiveData<>();
        this.mQuestionnaireSetting = new MutableLiveData<>();

        // firestore
        this.firestore = FirebaseFirestore.getInstance();
//        this.firestore.useEmulator("127.0.0.1", 8080);
        // settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.firestore.setFirestoreSettings(settings);

        // subscribe to data
        this.subscribeToQuestionnaires();
        this.subscribeToResults();
    }

    public LiveData<List<Questionnaire>> getAllQuestionnaires() {
        return this.mAllQuestionnaires;
    }

    public void select(Questionnaire questionnaire) {
        this.mSelectedQuestionnaire.setValue(questionnaire);
    }

    public LiveData<Questionnaire> getSelectedQuestionnaire() {
        return this.mSelectedQuestionnaire;
    }

    public void resetSelected() {
        Questionnaire questionnaire = this.mSelectedQuestionnaire.getValue();
        if (questionnaire != null) {
            questionnaire.getQuestions().forEach(question -> question.setResult(null));
        }
    }

    public void setQuestionnaireSetting(QuestionnaireSetting questionnaireSetting) {
        this.mPreference.setObject(questionnaireSetting.getQuestionnaireId(), questionnaireSetting);
        this.mQuestionnaireSetting.setValue(questionnaireSetting);
    }

    public LiveData<QuestionnaireSetting> getQuestionnaireSetting() {
        return this.mQuestionnaireSetting;
    }

    /**
     * Save result of the questionnaire in firabe. Get ID beforehand to keep reference.
     *
     * @param result Result of any questionnaire.
     */
    public void sendResult(QuestionnaireResult result) {
        DocumentReference documentReference = this.firestore.collection(FIREBASE_COLLECTION_RESULTS).document();
        result.setResultId(documentReference.getId());
        documentReference.set(result);
    }

    public void subscribeToQuestionnaires() {
        this.firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES)
                .addSnapshotListener((task, error) -> {
                    Log.d(TAG, "Something changed!");
                    if (error != null) {
                        Log.w(TAG, "Listen failed", error);
                        return;
                    }

                    if (task != null && !task.isEmpty()) {
                        Log.d(TAG, "Current data: " + task.toString());

                        // parse data
                        List<Questionnaire> questionnaires = task.getDocuments().stream()
                                .map(documentSnapshot -> {
                                    Questionnaire questionnaire = documentSnapshot.toObject(Questionnaire.class);
                                    if (questionnaire != null) {
                                        questionnaire.setId(documentSnapshot.getId());

                                    }
                                    return questionnaire;
                                })
                                .collect(Collectors.toList());

                        // set to LiveData
                        this.mAllQuestionnaires.setValue(questionnaires);
                        Log.d(TAG, "Questionnaires from Firestore: " + questionnaires.toString());
                    } else {
                        Log.d(TAG, "No Questionnaires");
                    }
                });
    }

    private void subscribeToResults() {
        this.firestore.collection(FIREBASE_COLLECTION_RESULTS)
                .addSnapshotListener((task, error) -> {
                    Log.d(TAG, "QuestionnaireResult changed!");
                    if (error != null) {
                        Log.w(TAG, "Listen failed", error);
                        return;
                    }

                    if (task != null && !task.isEmpty()) {
                        Log.d(TAG, "Current data: " + task.toString());
                        List<QuestionnaireResult> questionnaireResults = task.getDocuments().stream()
                                .map(documentSnapshot -> {
                                    QuestionnaireResult questionnaireResult = documentSnapshot.toObject(QuestionnaireResult.class);
                                    if (questionnaireResult != null) {
                                        questionnaireResult.setResultId(documentSnapshot.getId());
                                    }
                                    return questionnaireResult;
                                })
                                .collect(Collectors.toList());
                        this.mAllQuestionnaireResults.setValue(questionnaireResults);
                        Log.d(TAG, "Questionnaire results from Firestore: " + questionnaireResults.toString());
                    } else {
                        Log.d(TAG, "No results found");
                    }
                });
    }
}