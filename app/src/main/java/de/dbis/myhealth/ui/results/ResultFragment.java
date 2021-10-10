package de.dbis.myhealth.ui.results;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.ResultAdapter;
import de.dbis.myhealth.databinding.FragmentResultBinding;
import de.dbis.myhealth.models.QuestionnaireResult;

public class ResultFragment extends Fragment {

    // ViewModels
    private ResultViewModel mResultViewModel;

    // Views
    private View mRoot;

    // LiveData
    private LiveData<List<QuestionnaireResult>> mQuestionnaireResultsLiveData;

    // Adapter
    private ResultAdapter mResultAdapter;

    public static ResultFragment newInstance() {
        return new ResultFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ViewModel
        this.mResultViewModel = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);

        // Binding
        FragmentResultBinding mFragmentResultBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result, container, false);
        mFragmentResultBinding.setResultViewModel(this.mResultViewModel);
        this.mRoot = mFragmentResultBinding.getRoot();


        // Recyclerview
        RecyclerView recyclerView = this.mRoot.findViewById(R.id.result_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mResultAdapter = new ResultAdapter(requireActivity());
        recyclerView.setAdapter(this.mResultAdapter);

        // LiveData
        this.mQuestionnaireResultsLiveData = this.mResultViewModel.getSelectedQuestionnaireResults();

        return this.mRoot;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mQuestionnaireResultsLiveData.observe(getViewLifecycleOwner(), questionnaireResults -> this.mResultAdapter.setData(questionnaireResults));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        // remove results
        this.mResultViewModel.setSelectedQuestionnaireResults(new ArrayList<>());

        // remove observer
        if (this.mQuestionnaireResultsLiveData != null && this.mQuestionnaireResultsLiveData.hasObservers()) {
            this.mQuestionnaireResultsLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}