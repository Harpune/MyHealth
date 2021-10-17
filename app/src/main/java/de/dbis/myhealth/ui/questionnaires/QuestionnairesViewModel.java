package de.dbis.myhealth.ui.questionnaires;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.QuestionResult;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_QUESTIONNAIRES;

public class QuestionnairesViewModel extends AndroidViewModel {
    private final static String TAG = "QuestionnairesViewModel";

    private Preference mPreference;

    private final MutableLiveData<List<Questionnaire>> mAllQuestionnaires;
    private final MutableLiveData<Questionnaire> mSelectedQuestionnaire;
    private final MutableLiveData<List<QuestionnaireResult>> mAllQuestionnaireResults;
    private final MutableLiveData<QuestionnaireSetting> mQuestionnaireSetting;
    private final MutableLiveData<List<QuestionResult>> mQuestionResults;

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
        this.mQuestionResults = new MutableLiveData<>();

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

    public void subscribeToQuestionnaires() {
        this.firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES + "_" + Locale.getDefault().getLanguage())
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


    public void addQuestionResult(QuestionResult questionResult) {
        List<QuestionResult> questionResults = this.mQuestionResults.getValue();
        if (questionResults == null) {
            questionResults = new ArrayList<>();
        }

        if (questionResults.stream().anyMatch(questionResult1 -> questionResult.getQuestionNumber() == questionResult1.getQuestionNumber())) {

        }

    }

    //    private void generateTHI() {
//        String[] questionStrings = getApplication().getResources().getStringArray(R.array.thi_survey_questions);
//        List<Question> questions = new ArrayList<>();
//        for (int i = 0; i < questionStrings.length; i++) {
//            Question question = new Question(questionStrings[i], Question.QuestionType.YES_NO_SOMETIMES);
//            questions.add(question);
//        }
//
//        Questionnaire questionnaire = new Questionnaire("Tinnitus Handicap Inventory", getApplication().getString(R.string.thi_description), questions);
//        this.firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES + "_" + Locale.getDefault().getLanguage())
//                .document("THI")
//                .set(questionnaire);
//    }
//
//    private void generateTFI() {
//        String[] questionStrings = getApplication().getResources().getStringArray(R.array.tfi_survey_questions);
//
//        List<Question> questions = new ArrayList<>();
//        for (int i = 0; i < questionStrings.length; i++) {
//            Question question;
//            if (i == 0 || i == 1 || i == 2) {
//                question = new Question(questionStrings[i], Question.QuestionType.SLIDER_0_100);
//            } else {
//                question = new Question(questionStrings[i], Question.QuestionType.SLIDER_0_10);
//            }
//            questions.add(question);
//        }
//
//        Questionnaire questionnaire = new Questionnaire("Tinnitus Functional Index", getApplication().getString(R.string.tfi_description), questions);
//        this.firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES + "_" + Locale.getDefault().getLanguage())
//                .document("TFI")
//                .set(questionnaire);
//    }
}