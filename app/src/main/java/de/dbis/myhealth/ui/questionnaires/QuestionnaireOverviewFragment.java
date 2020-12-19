package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionnaireAdapter;

public class QuestionnaireOverviewFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create fragment
        View root = inflater.inflate(R.layout.fragment_questionnaire_overview, container, false);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        QuestionnaireAdapter questionnaireAdapter = new QuestionnaireAdapter(requireActivity());

        RecyclerView recyclerView = root.findViewById(R.id.questionnairesRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(questionnaireAdapter);

        return root;
    }
}