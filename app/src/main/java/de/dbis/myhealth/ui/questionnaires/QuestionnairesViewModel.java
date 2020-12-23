package de.dbis.myhealth.ui.questionnaires;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;

public class QuestionnairesViewModel extends ViewModel {
    private final static String TAG = "QuestionnairesViewModel";
    private final static String FIREBASE_COLLECTION_QUESTIONNAIRES = "questionnaire";

    private CollectionReference ref;
    private final MutableLiveData<List<Questionnaire>> mQuestionnaires;
    private final MutableLiveData<Questionnaire> mQuestionnaire;

    public QuestionnairesViewModel() {
        this.ref = FirebaseFirestore.getInstance().collection(FIREBASE_COLLECTION_QUESTIONNAIRES);
        this.mQuestionnaires = new MutableLiveData<>();
        this.mQuestionnaire = new MutableLiveData<>();

        this.subscribeToQuestionnaires();
    }

    public LiveData<List<Questionnaire>> getQuestionnaires() {
        return mQuestionnaires;
    }

    public void select(Questionnaire questionnaire) {
        this.mQuestionnaire.setValue(questionnaire);
    }

    public LiveData<Questionnaire> getSelected() {
        return this.mQuestionnaire;
    }

    public void updateQuestion(int position, Question question) {
        Questionnaire questionnaire = this.getSelected().getValue();
        if (questionnaire != null) {
            questionnaire.getQuestions().set(position, question);
        }
    }

    private void subscribeToQuestionnaires() {
        this.ref.addSnapshotListener((task, error) -> {
            Log.d(TAG, "Something changed!");
            if (error != null) {
                Log.w(TAG, "Listen failed", error);
                this.mQuestionnaires.setValue(new ArrayList<>());
                return;
            }

            if (task != null && !task.isEmpty()) {
                Log.d(TAG, "Current data: " + task.toString());
                List<Questionnaire> questionnaires = task.getDocuments().stream()
                        .map(documentSnapshot -> {
                            Questionnaire questionnaire = documentSnapshot.toObject(Questionnaire.class);
                            if (questionnaire != null) {
                                questionnaire.setId(documentSnapshot.getId());
                                return questionnaire;
                            }
                            return null;
                        })
                        .collect(Collectors.toList());
                this.mQuestionnaires.setValue(questionnaires);

            } else {
                Log.d(TAG, "No Questionnaires");
            }
        });
    }

    public void generateTFI(Activity activity) {
        List<Question> questions = new ArrayList<>();
        String[] tfiQuestions = activity.getResources().getStringArray(R.array.tfi_survey_questions);
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

    public void generateTHI(Activity activity) {
        List<Question> questions = new ArrayList<>();
        String[] tfiQuestions = activity.getResources().getStringArray(R.array.thi_survey_questions);
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