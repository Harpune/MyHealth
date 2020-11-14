package de.dbis.myhealth.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Integer> mNumberOne;
    private MutableLiveData<Integer> mNumberTwo;


    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mNumberOne = new MutableLiveData<>();
        mNumberTwo = new MutableLiveData<>();

        mText.setValue("This is home fragment");
        mNumberOne.setValue(0);
        mNumberTwo.setValue(0);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setText(String text){
        mText.setValue(text);
    }

    public LiveData<Integer> getNumberOne() {
        return mNumberOne;
    }

    public void incrementOne() {
        if(mNumberOne.getValue() != null) {
            mNumberOne.setValue(mNumberOne.getValue() + 1);
        }
    }

    public LiveData<Integer> getNumberTwo() {
        return mNumberTwo;
    }

    public void incrementTwo() {
        if(this.mNumberTwo.getValue() != null) {
            this.mNumberTwo.setValue(this.mNumberTwo.getValue() + 1);
        }
    }


}