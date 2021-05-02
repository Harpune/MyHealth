package de.dbis.myhealth.ui.stats.results;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;

public class ResultViewModel extends ViewModel {
    private final MutableLiveData<List<QuestionnaireResult>> mSelectedQuestionnaireResults;
    private final MutableLiveData<Questionnaire> mQuestionnaire;
    private final MutableLiveData<LocalDate> mLocalDate;

    public ResultViewModel() {
        this.mSelectedQuestionnaireResults = new MutableLiveData<>();
        this.mQuestionnaire = new MutableLiveData<>();
        this.mLocalDate = new MutableLiveData<>();
    }

    public void setSelectedQuestionnaireResults(List<QuestionnaireResult> questionnaireResults) {
        questionnaireResults.sort((a, b) -> a.getStartExecutionDate().compareTo(b.getStartExecutionDate()));
        this.mSelectedQuestionnaireResults.setValue(questionnaireResults);
    }

    public LiveData<List<QuestionnaireResult>> getSelectedQuestionnaireResults() {
        return this.mSelectedQuestionnaireResults;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.mQuestionnaire.setValue(questionnaire);
    }

    public LiveData<Questionnaire> getQuestionnaire() {
        return this.mQuestionnaire;
    }

    public void setDate(LocalDate localDate) {
        this.mLocalDate.setValue(localDate);
    }

    public LiveData<LocalDate> getLocalDate() {
        return this.mLocalDate;
    }
}