package de.dbis.myhealth.ui.questionnaires;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionAdapter;
import de.dbis.myhealth.adapter.QuestionnaireAdapter;
import de.dbis.myhealth.databinding.FragmentQuestionnaireBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;

public class QuestionnaireFragment extends Fragment {
    private final static String TAG = "QuestionnaireFragment";

    private QuestionnairesViewModel mQuestionnairesViewModel;
    private SharedPreferences mSharedPreferences;

    public static QuestionnaireFragment newInstance() {
        return new QuestionnaireFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentQuestionnaireBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_questionnaire, container, false);
        View root = binding.getRoot();

        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);


        // Get current questionnaire
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mQuestionnairesViewModel.getSelected().observe(requireActivity(), binding::setQuestionnaire);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        QuestionAdapter questionAdapter = new QuestionAdapter(requireActivity());

        RecyclerView recyclerView = root.findViewById(R.id.questionRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(questionAdapter);

        ((MainActivity) requireActivity()).setFabClickListener(mFabClickListener);

        return root;
    }

    private final View.OnClickListener mFabClickListener = view -> {
        Questionnaire questionnaire = this.mQuestionnairesViewModel.getSelected().getValue();
        Log.d(TAG, "Result Questionnaire" + questionnaire);

        if (questionnaire != null) {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Done")
                    .setMessage("Are you done with this questionnaire?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {

                        // IDs
                        String userId = this.mSharedPreferences.getString(getString(R.string.device_id), UUID.randomUUID().toString());
                        String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track), null);
                        String resultId = UUID.randomUUID().toString();

                        // get result data
                        List<Integer> resultEntries = Optional.ofNullable(questionnaire.getQuestions())
                                .map(Collection::stream)
                                .orElseGet(Stream::empty)
                                .map(Question::getResult)
                                .collect(Collectors.toList());


                        // build result
                        QuestionnaireResult result = new QuestionnaireResult(
                                resultId,
                                userId,
                                trackId,
                                new Date(),
                                100000L,
                                questionnaire.getId(),
                                resultEntries,
                                new ArrayList<>(),
                                new ArrayList<>()
                        );

                        // save result
                        this.mQuestionnairesViewModel.sendResult(result);
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    };
}