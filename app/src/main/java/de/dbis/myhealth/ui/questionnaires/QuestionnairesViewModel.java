package de.dbis.myhealth.ui.questionnaires;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public void generateQuestionnaire() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("Question 1", Question.QuestionType.YES_NO));
        questions.add(new Question("Question 2", Question.QuestionType.YES_NO));
        questions.add(new Question("Question 3", Question.QuestionType.YES_NO));
        questions.add(new Question("Question 4", Question.QuestionType.YES_NO));
        questions.add(new Question("Question 5", Question.QuestionType.YES_NO));
        Questionnaire questionnaire = new Questionnaire("Title 1", "Beschreibung 1", questions);
        this.ref.document("Q2").set(questionnaire);

    }
}