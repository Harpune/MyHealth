package de.dbis.myhealth.ui.questionnaires;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.adapter.QuestionAdapter;
import de.dbis.myhealth.databinding.FragmentQuestionnaireBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.QuestionResult;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.ui.stats.StatsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;

public class QuestionnaireFragment extends Fragment {
    private final static String TAG = "QuestionnaireFragment";

    // Views
    private Toolbar mToolbar;

    // Questionnaire related
    private QuestionnairesViewModel mQuestionnairesViewModel;
    private UserViewModel mUserViewModel;
    private StatsViewModel mStatsViewModel;
    private SharedPreferences mSharedPreferences;
    private QuestionAdapter mQuestionAdapter;
    private QuestionnaireSetting mQuestionnaireSetting;

    private LiveData<Questionnaire> mQuestionnairesLiveData;

    // Stopwatch
    private final StopWatch mStopWatch = new StopWatch();

    // Listener for FAB
    private final View.OnClickListener mFabClickListener = this::save;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mStopWatch.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentQuestionnaireBinding mFragmentQuestionnaireBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_questionnaire, container, false);

        View root = mFragmentQuestionnaireBinding.getRoot();

        // Setup toolbar
        this.mToolbar = root.findViewById(R.id.questionnaireToolbar);
        this.mToolbar.setTitleTextColor(Color.WHITE);
        this.mToolbar.setSubtitleTextColor(Color.WHITE);

        mFragmentQuestionnaireBinding.setLifecycleOwner(getViewLifecycleOwner());

        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // Get current questionnaire
        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        this.mStatsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        // Get questionnaire
        this.mQuestionnairesLiveData = this.mQuestionnairesViewModel.getSelectedQuestionnaire();
        this.mQuestionnairesLiveData.observe(getViewLifecycleOwner(), mFragmentQuestionnaireBinding::setQuestionnaire);

        // Get questionnaire setting
        this.mQuestionnairesViewModel.getQuestionnaireSetting().observe(getViewLifecycleOwner(), this::applyQuestionnaireSetting);

        // Create recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        this.mQuestionAdapter = new QuestionAdapter(requireActivity(), getViewLifecycleOwner(), this.mStopWatch);

        RecyclerView recyclerView = root.findViewById(R.id.questionRecyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(this.mQuestionAdapter);

        // update action of fab
        ((MainActivity) requireActivity()).setFabClickListener(mFabClickListener);

        return root;
    }

    private void applyQuestionnaireSetting(QuestionnaireSetting questionnaireSetting) {
        this.mQuestionnaireSetting = questionnaireSetting;


        // only show icon in toolbar if questions where removed
        if (this.mQuestionnaireSetting != null && !this.mQuestionnaireSetting.getRemovedQuestions().isEmpty()) {

            // Get remove questions and update boolean array with deletion information
            String[] removedQuestionTitles = this.mQuestionnaireSetting.getRemovedQuestions().stream()
                    .map(Question::getText)
                    .sorted()
                    .toArray(String[]::new);
            boolean[] enabled = new boolean[removedQuestionTitles.length];

            // setup menu
            this.mToolbar.getMenu().clear();
            this.mToolbar.inflateMenu(R.menu.menu_questionnaire_control);
            this.mToolbar.setOnMenuItemClickListener(item -> {
                this.mStopWatch.suspend();
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.deleted_questions))
                        .setMultiChoiceItems(removedQuestionTitles, enabled, (dialogInterface, i, b) -> enabled[i] = b)
                        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                            for (int j = 0; j < enabled.length; j++) {
                                if (enabled[j]) {
                                    this.mQuestionnaireSetting.reAddQuestion(removedQuestionTitles[j]);
                                    this.mQuestionnairesViewModel.setQuestionnaireSetting(this.mQuestionnaireSetting);
                                    this.mQuestionAdapter.notifyDataSetChanged();
                                }
                            }
                            this.mStopWatch.resume();
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, which) -> this.mStopWatch.resume())
                        .setCancelable(false)
                        .show();

                return false;
            });
        }
    }

    private void save(View view) {
        this.mStopWatch.suspend();

        Questionnaire questionnaire = this.mQuestionnairesViewModel.getSelectedQuestionnaire().getValue();
        // Log.d(TAG, "QuestionResult Questionnaire" + questionnaire);

        if (questionnaire != null) {
            // Get questions
            List<Question> questions = questionnaire.getQuestions();
            int amountAnswered = Math.round(questions.stream()
                    .filter(question -> question.getResult() != null)
                    .count());
            int enabledQuestions = questions.size();

            // Get removed questions
            List<Question> removedQuestions = new ArrayList<>();
            if (this.mQuestionnaireSetting != null) {
                removedQuestions.addAll(this.mQuestionnaireSetting.getRemovedQuestions());
                enabledQuestions -= removedQuestions.size();
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.submit_results))
                    .setMessage(getString(R.string.submit_results_summary, amountAnswered, enabledQuestions))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

                        this.mStopWatch.stop();

                        // user
                        FirebaseUser firebaseUser = this.mUserViewModel.getFirebaseUser().getValue();

                        // IDs
                        String questionnaireId = questionnaire.getId();
                        String userId = firebaseUser != null ? firebaseUser.getUid() : "no_id";
                        String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);

                        // Timers of question
                        long[] timers = this.mQuestionAdapter.getTimers();
                        OptionalDouble optionalDouble = Arrays.stream(timers).filter(l -> l != 0L).average();
                        double averageDuration = optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0;

                        List<QuestionResult> questionResults = new ArrayList<>();
                        for (int j = 0; j < questions.size(); j++) {
                            questionResults.add(new QuestionResult(
                                    questions.get(j).getResult(),
                                    timers[j],
                                    j,
                                    removedQuestions.contains(questions.get(j))));
                        }

                        // build result
                        QuestionnaireResult result = new QuestionnaireResult(
                                questionnaireId,
                                userId,
                                trackId,
                                new Date(),
                                this.mStopWatch.getTime(),
                                averageDuration,
                                questionResults
                        );


                        // save result
//                        this.mQuestionnairesViewModel.sendResult(result);
                        this.mStatsViewModel.uploadQuestionnaireResult(result);
                        this.mQuestionnairesViewModel.resetSelected();
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                    })
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> this.mStopWatch.resume())
                    .show();
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mQuestionnairesLiveData != null) {
            this.mQuestionnairesLiveData.removeObservers(getViewLifecycleOwner());
        }

        this.mQuestionAdapter.removeObserver();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save questionnaire settings for later use
        if (this.mStopWatch.isStarted()) {
            this.mStopWatch.suspend();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mStopWatch.isSuspended()) {
            this.mStopWatch.resume();
        }
    }
}