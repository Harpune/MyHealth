package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionnairesAdapter;

public class QuestionnairesFragment extends Fragment {

    private QuestionnairesViewModel mQuestionnairesViewModel;
    private QuestionnairesAdapter mQuestionnairesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);

        // Create fragment
        View root = inflater.inflate(R.layout.fragment_questionnaires, container, false);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        this.mQuestionnairesAdapter = new QuestionnairesAdapter(requireActivity(), new ArrayList<>());

        RecyclerView recyclerView = root.findViewById(R.id.questionnairesRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(this.mQuestionnairesAdapter);

        // handle Firestore data
        this.mQuestionnairesViewModel.getQuestionnaires().observe(getViewLifecycleOwner(), questionnaires -> {
            this.mQuestionnairesAdapter.setData(questionnaires);
            this.mQuestionnairesAdapter.notifyDataSetChanged();
        });

        return root;
    }
}