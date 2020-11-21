package de.dbis.myhealth.ui.questionnaire;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;

public class QuestionnaireFragment extends Fragment {

    private QuestionnaireViewModel mViewModel;

    public static QuestionnaireFragment newInstance() {
        return new QuestionnaireFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questionnaire, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(QuestionnaireViewModel.class);
        // TODO: Use the ViewModel
    }

}