package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentQuestionnaireBinding;
import de.dbis.myhealth.models.Questionnaire;

public class QuestionnaireFragment extends Fragment {
    private final static String TAG = "QuestionnaireFragment";

    private FragmentQuestionnaireBinding mBinding;
    public Questionnaire mQuestionnaire;

    public static QuestionnaireFragment newInstance() {
        return new QuestionnaireFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_questionnaire, container, false);
        return this.mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        QuestionnairesViewModel mViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mBinding.setLifecycleOwner(this);

        mViewModel.getSelected().observe(getViewLifecycleOwner(), this::updateQuestionnaire);
    }

    private void updateQuestionnaire(Questionnaire questionnaire) {
        Log.d(TAG, questionnaire.toString());
        this.mQuestionnaire = questionnaire;
        this.mBinding.setQuestionnaire(questionnaire);

    }

}