package de.dbis.myhealth.ui.questionnaires;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.Result;
import de.dbis.myhealth.repository.QuestionnaireRepository;

public class QuestionnairesViewModel extends AndroidViewModel {
    private final static String TAG = "QuestionnairesViewModel";

    private final QuestionnaireRepository mRepository;
    private final LiveData<List<Questionnaire>> mAllQuestionnaires;
    private final MutableLiveData<Questionnaire> mQuestionnaire;

    public QuestionnairesViewModel(Application application) {
        super(application);
        this.mRepository = new QuestionnaireRepository(application);
        this.mAllQuestionnaires = this.mRepository.getAllQuestionnaires();
        this.mQuestionnaire = new MutableLiveData<>();
    }

    public LiveData<List<Questionnaire>> getQuestionnaires() {
        return this.mAllQuestionnaires;
    }

    public void insert(Questionnaire questionnaire) {
        this.mRepository.insert(questionnaire);
    }

    public void select(Questionnaire questionnaire) {
        this.mQuestionnaire.setValue(questionnaire);
    }

    public LiveData<Questionnaire> getSelected() {
        return this.mQuestionnaire;
    }

    public void updateQuestion(Question question) {
        // Get questionnaire and questions
        Questionnaire questionnaire = this.mQuestionnaire.getValue();
        if (questionnaire != null) {
            List<Question> questions = questionnaire.getQuestions();

            // Get index of question
            OptionalInt index = IntStream.range(0, questions.size())
                    .filter(i -> questions.get(i).getText().equals(question.getText()))
                    .findFirst();

            // update item in questions-list
            if (index.isPresent()) {
                questions.set(index.getAsInt(), question);
                questionnaire.setQuestions(questions);

                // save
                this.mQuestionnaire.setValue(questionnaire);
            }
        }
    }

    public void sendResult(Result result) {
        this.mRepository.sendResult(result);
    }

    public void generateTHI() {
        this.mRepository.generateTHI(getApplication());
    }

    public void generateTFI() {
        this.mRepository.generateTFI(getApplication());
    }
}