package de.dbis.myhealth.ui.collector;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CollectorViewModel extends ViewModel {
    private final MutableLiveData<Integer> answeredQuestionnaires;

    public CollectorViewModel() {
        this.answeredQuestionnaires = new MutableLiveData<>();
    }

    // TODO: Implement the ViewModel
    public void incrementAnsweredQuestionnaires() {
        Integer val = this.answeredQuestionnaires.getValue();
    }

    public Integer getAnsweredQuestionnaires() {
        return this.answeredQuestionnaires.getValue();
    }
}