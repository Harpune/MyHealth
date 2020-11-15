package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionnairesAdapter;
import de.dbis.myhealth.models.Questionnaire;

public class QuestionnairesFragment extends Fragment {

    private QuestionnairesViewModel questionnairesViewModel;
    private QuestionnairesAdapter questionnairesAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        questionnairesViewModel = new QuestionnairesViewModel();

        View root = inflater.inflate(R.layout.fragment_questionnaires, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        questionnairesAdapter = new QuestionnairesAdapter(generateData());

        RecyclerView recyclerView = root.findViewById(R.id.questionnairesRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(questionnairesAdapter);

        return root;
    }

    private List<Questionnaire> generateData(){
        List<Questionnaire> questionnaires = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            questionnaires.add(new Questionnaire(i));
        }

        return questionnaires;
    }
}