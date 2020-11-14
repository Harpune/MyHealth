package de.dbis.myhealth.ui.questionnaires;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QuestionnairesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QuestionnairesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}