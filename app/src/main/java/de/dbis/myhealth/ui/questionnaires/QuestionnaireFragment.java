package de.dbis.myhealth.ui.questionnaires;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
import de.dbis.myhealth.databinding.FragmentQuestionnaireBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;

public class QuestionnaireFragment extends Fragment {
    private final static String TAG = "QuestionnaireFragment";

    private QuestionnairesViewModel mQuestionnairesViewModel;
    private SharedPreferences mSharedPreferences;
    private QuestionAdapter mQuestionAdapter;

    private LiveData<Questionnaire> mQuestionnairesLiveDataLiveData;
    private LiveData<QuestionnaireSetting> mQuestionnaireSettingLiveData;

    private final View.OnClickListener mFabClickListener = this::save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentQuestionnaireBinding mQuestionnaireBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_questionnaire, container, false);
        View root = mQuestionnaireBinding.getRoot();

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);

        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // Get current questionnaire
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mQuestionnairesLiveDataLiveData = this.mQuestionnairesViewModel.getSelected();
        this.mQuestionnairesLiveDataLiveData.observe(getViewLifecycleOwner(), questionnaire -> {
            // bind
            mQuestionnaireBinding.setQuestionnaire(questionnaire);

            // observe settings of current questionnaire
            this.mQuestionnaireSettingLiveData = this.mQuestionnairesViewModel.getQuestionnaireSetting(questionnaire.getId());
            this.mQuestionnaireSettingLiveData.observe(getViewLifecycleOwner(), questionnaireSetting -> {
                toolbar.getMenu().clear();

                // only show if questions where removed
                if (questionnaireSetting != null && !questionnaireSetting.getRemovedQuestions().isEmpty()) {

                    // Get remove questions and update boolean array with deletion information
                    String[] removedQuestionTitles = questionnaireSetting.getRemovedQuestions().stream()
                            .map(Question::getText)
                            .sorted()
                            .toArray(String[]::new);
                    boolean[] enabled = new boolean[removedQuestionTitles.length];

                    // Setup Menu
                    toolbar.inflateMenu(R.menu.menu_questionnaire_control);
                    toolbar.setOnMenuItemClickListener(item -> {

                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Deleted Questions")
                                .setMultiChoiceItems(removedQuestionTitles, enabled, (dialogInterface, i, b) -> {
                                    enabled[i] = b;
                                    Log.d(TAG, "Clicked");
                                })
                                .setPositiveButton("Enable", (dialogInterface, i) -> {
                                    for (int j = 0; j < enabled.length; j++) {
                                        if (enabled[j]) {
                                            questionnaireSetting.reAddQuestion(removedQuestionTitles[j]);
                                            this.mQuestionnairesViewModel.insertQuestionnaireSetting(questionnaireSetting);
                                        }
                                    }
                                    Log.d(TAG, "Dome");
                                })
                                .show();

                        return false;
                    });

                }
            });
        });

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        this.mQuestionAdapter = new QuestionAdapter(requireActivity(), getViewLifecycleOwner());

        RecyclerView recyclerView = root.findViewById(R.id.questionRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(this.mQuestionAdapter);

        // update action of fab
        ((MainActivity) requireActivity()).setFabClickListener(mFabClickListener);

        return root;
    }

    private void save(View view) {
        Questionnaire questionnaire = this.mQuestionnairesViewModel.getSelected().getValue();
        Log.d(TAG, "Result Questionnaire" + questionnaire);

        if (questionnaire != null) {
            new MaterialAlertDialogBuilder(requireContext())
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

    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.mQuestionnaireSettingLiveData != null) {
            this.mQuestionnaireSettingLiveData.removeObservers(getViewLifecycleOwner());
        }

        if (this.mQuestionnairesLiveDataLiveData != null) {
            this.mQuestionnairesLiveDataLiveData.removeObservers(getViewLifecycleOwner());
        }

        this.mQuestionAdapter.removeObserver();
    }
}