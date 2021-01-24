package de.dbis.myhealth.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.dao.QuestionnaireDao;
import de.dbis.myhealth.dao.QuestionnaireResultDao;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.util.AppDatabase;

public class QuestionnaireRepository {
    private final static String TAG = "QuestionnaireRepository";
    private final Application application;

    // db
    private final QuestionnaireDao mQuestionnaireDao;
    private final LiveData<List<Questionnaire>> mQuestionnaires;
    private final QuestionnaireResultDao mQuestionnaireResultDao;
    private final LiveData<List<QuestionnaireResult>> mQuestionnaireResult;

    // network
    private final FirebaseFirestore firestore;
    private final static String FIREBASE_COLLECTION_QUESTIONNAIRES = "questionnaire";
    private final CollectionReference questionnaireRef;
    private final static String FIREBASE_COLLECTION_RESULTS_PRE = "result-";

    public QuestionnaireRepository(Application application) {
        this.application = application;
        // db
        AppDatabase db = AppDatabase.getInstance(application);
        this.mQuestionnaireDao = db.questionnaireDao();
        this.mQuestionnaires = this.mQuestionnaireDao.getAll();
        this.mQuestionnaireResultDao = db.resultDao();
        this.mQuestionnaireResult = this.mQuestionnaireResultDao.getAll();

        // network
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.firestore = FirebaseFirestore.getInstance();
        this.firestore.setFirestoreSettings(settings);
        // questionnaires
        this.questionnaireRef = firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES);
        this.subscribeToQuestionnaires();
        this.subscribeToResults();
    }

    public LiveData<List<Questionnaire>> getAllQuestionnaires() {
        return this.mQuestionnaires;
    }

    public void insert(Questionnaire questionnaire) {
        AppDatabase.databaseWriteExecutor.execute(() -> mQuestionnaireDao.insert(questionnaire));
    }

    public LiveData<List<QuestionnaireResult>> getAllQuestionnaireResults() {
        return this.mQuestionnaireResult;
    }

    public void insert(QuestionnaireResult questionnaireResult) {
        AppDatabase.databaseWriteExecutor.execute(() -> mQuestionnaireResultDao.insert(questionnaireResult));
    }

    private void subscribeToQuestionnaires() {
        this.questionnaireRef.addSnapshotListener((task, error) -> {
            Log.d(TAG, "Something changed!");
            if (error != null) {
                Log.w(TAG, "Listen failed", error);
                return;
            }

            if (task != null && !task.isEmpty()) {
                Log.d(TAG, "Current data: " + task.toString());
                List<Questionnaire> questionnaires = task.getDocuments().stream()
                        .map(documentSnapshot -> {
                            Questionnaire questionnaire = documentSnapshot.toObject(Questionnaire.class);
                            if (questionnaire != null) {
                                questionnaire.setId(documentSnapshot.getId());
                                insert(questionnaire);
                            }
                            return questionnaire;
                        })
                        .collect(Collectors.toList());
                Log.d(TAG, "Questionnaires from Firestore: " + questionnaires.toString());
            } else {
                Log.d(TAG, "No Questionnaires");
            }
        });
    }

    private void subscribeToResults() {
        Context context = this.application.getApplicationContext();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(context.getString(R.string.device_id))) {
            String userId = sharedPreferences.getString(context.getString(R.string.device_id), null);
            if (userId != null) {
                this.firestore.collection(FIREBASE_COLLECTION_RESULTS_PRE + userId)
                        .addSnapshotListener((task, error) -> {
                            Log.d(TAG, "QuestionnaireResult changed!");
                            if (error != null) {
                                Log.w(TAG, "Listen failed", error);
                                return;
                            }

                            if (task != null && !task.isEmpty()) {
                                Log.d(TAG, "Current data: " + task.toString());
                                task.getDocuments().stream()
                                        .map(documentSnapshot -> documentSnapshot.toObject(QuestionnaireResult.class))
                                        .forEach(this::insert);
                            } else {
                                Log.d(TAG, "No results found");
                            }
                        });
            }
        } else {
            Log.d(TAG, "Could not subscribe to result collection. 'device_id' not saved in shared preferences");
        }
    }


    public void generateTFI(Application application) {
        List<Question> questions = new ArrayList<>();
        String[] tfiQuestions = application.getResources().getStringArray(R.array.tfi_survey_questions);
        for (String question : tfiQuestions) {
            questions.add(new Question(question, Question.QuestionType.SLIDER_0_100));
        }

        Questionnaire questionnaire = new Questionnaire(
                "Tinnitus Functional Index",
                "The Tinnitus Functional Index (TFI) is the " +
                        "first tinnitus questionnaire documented for " +
                        "responsiveness, and has the potential to become " +
                        "the new standard for evaluating the effects of " +
                        "intervention for tinnitus, with clinical patients " +
                        "and in research studies.",
                questions);
        this.questionnaireRef.document("TFI").set(questionnaire);

    }

    public void generateTHI(Application application) {
        List<Question> questions = new ArrayList<>();
        String[] tfiQuestions = application.getResources().getStringArray(R.array.thi_survey_questions);
        for (String question : tfiQuestions) {
            questions.add(new Question(question, Question.QuestionType.YES_NO_SOMETIMES));
        }

        Questionnaire questionnaire = new Questionnaire(
                "Tinnitus Handicap Inventory",
                "The purpose of this questionnaire is to identify the problems your " +
                        "tinnitus may be causing you. Check \"Yes\", \"Sometimes\", or \"No\" for each " +
                        "question. Please answer all questions.",
                questions);
        this.questionnaireRef.document("THI").set(questionnaire);

    }

    public void sendResult(QuestionnaireResult result) {
        this.firestore.collection(FIREBASE_COLLECTION_RESULTS_PRE + result.getUserId())
                .document(result.getResultId())
                .set(result);
    }
}
