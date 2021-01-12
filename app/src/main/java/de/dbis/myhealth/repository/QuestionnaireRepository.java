package de.dbis.myhealth.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.R;
import de.dbis.myhealth.dao.QuestionnaireDao;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.util.AppDatabase;

public class QuestionnaireRepository {
    private final static String TAG = "QuestionnaireRepository";

    // db
    private final QuestionnaireDao mQuestionnaireDao;
    private final LiveData<List<Questionnaire>> mQuestionnaires;

    // network
    private final static String FIREBASE_COLLECTION_QUESTIONNAIRES = "questionnaire";
    private final CollectionReference ref;

    public QuestionnaireRepository(Application application) {
        // db
        AppDatabase db = AppDatabase.getInstance(application);
        this.mQuestionnaireDao = db.questionnaireDao();
        this.mQuestionnaires = this.mQuestionnaireDao.getAll();

        // network
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        this.ref = firestore.collection(FIREBASE_COLLECTION_QUESTIONNAIRES);
        this.subscribeToQuestionnaires();
    }

    public LiveData<List<Questionnaire>> getAllQuestionnaires() {
        return this.mQuestionnaires;
    }

    public void insert(Questionnaire questionnaire) {
        AppDatabase.databaseWriteExecutor.execute(() -> mQuestionnaireDao.insert(questionnaire));
    }

    private void subscribeToQuestionnaires() {
        this.ref.addSnapshotListener((task, error) -> {
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
        this.ref.document("TFI").set(questionnaire);

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
        this.ref.document("THI").set(questionnaire);

    }
}
