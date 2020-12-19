package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionAdapter;
import de.dbis.myhealth.adapter.QuestionnaireAdapter;
import de.dbis.myhealth.databinding.FragmentQuestionnaireBinding;

public class QuestionnaireFragment extends Fragment {
    private final static String TAG = "QuestionnaireFragment";

    public static QuestionnaireFragment newInstance() {
        return new QuestionnaireFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentQuestionnaireBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_questionnaire, container, false);
        View root = binding.getRoot();

        // Get current questionnaire
        QuestionnairesViewModel viewHolder = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        viewHolder.getSelected().observe(requireActivity(), binding::setQuestionnaire);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        QuestionAdapter questionAdapter = new QuestionAdapter(requireActivity());

        RecyclerView recyclerView = root.findViewById(R.id.questionRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(questionAdapter);

        return root;
    }
}