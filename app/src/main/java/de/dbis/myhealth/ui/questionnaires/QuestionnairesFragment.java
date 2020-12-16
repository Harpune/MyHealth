package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionnairesAdapter;
import de.dbis.myhealth.databinding.ItemQuestionnairesBinding;
import de.dbis.myhealth.models.Questionnaire;

public class QuestionnairesFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create fragment
        View root = inflater.inflate(R.layout.fragment_questionnaires, container, false);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        QuestionnairesAdapter mQuestionnairesAdapter = new QuestionnairesAdapter(requireActivity());

        RecyclerView recyclerView = root.findViewById(R.id.questionnairesRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(mQuestionnairesAdapter);

        return root;
    }

    public void showQuestionnaire(Questionnaire questionnaire){
        QuestionnairesViewModel viewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        viewModel.select(questionnaire);
    }
}