package de.dbis.myhealth.ui.questionnaires;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentChatBinding;
import de.dbis.myhealth.models.ChatMessage;
import de.dbis.myhealth.models.ChatUser;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.QuestionResult;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.ui.stats.StatsViewModel;
import de.dbis.myhealth.ui.user.UserViewModel;

public class ChatFragment extends Fragment implements MessageHolders.ContentChecker<ChatMessage> {

    private static final int TYPE_CAROUSEL = 1;

    private SharedPreferences mSharedPreferences;

    private QuestionnairesViewModel mQuestionnairesViewModel;
    private StatsViewModel mStatsViewModel;
    private UserViewModel mUserViewModel;

    // LiveData
    private LiveData<Questionnaire> mQuestionnaireLiveData;
    private LiveData<QuestionnaireSetting> mQuestionnaireSettingLiveData;

    // References
    private FragmentChatBinding mFragmentChatBinding;
    private QuestionnaireSetting mQuestionnaireSetting;
    private Questionnaire mQuestionnaire;
    private static List<QuestionResult> mQuestionResults;

    // Chat
    private final ChatUser mMyHealthChatUser = new ChatUser("MyHealth", "MyHealth", "");
    private final ChatUser mChatUser = new ChatUser("user", "user", "");
    private List<ChatMessage> allChatMessages;
    private List<ChatMessage> enabledChatMessages;
    private List<ChatMessage> enabledChatMessagesQueue;
    private List<ChatMessage> questions;
    private List<ChatMessage> answers;
    private MessagesListAdapter<ChatMessage> mMessagesListAdapter;

    // Views
    private MessagesList mMessagesList;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private final View.OnClickListener mFabClickListener = this::startQuestionnaire;

    // Stopwatch
    private static final StopWatch mStopWatch = new StopWatch();
    private static long mLastSplit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStopWatch.reset();
        mStopWatch.start();

        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mQuestionnaireLiveData = mQuestionnairesViewModel.getSelectedQuestionnaire();
        this.mQuestionnaireSettingLiveData = mQuestionnairesViewModel.getQuestionnaireSetting();

        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        this.mStatsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mFragmentChatBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
        this.mFragmentChatBinding.setLifecycleOwner(getViewLifecycleOwner());

        // setup views
        View root = mFragmentChatBinding.getRoot();
        this.toolbar = root.findViewById(R.id.toolbar);
        this.toolbar.setTitleTextColor(Color.WHITE);
        this.toolbar.setSubtitleTextColor(Color.WHITE);

        this.mMessagesList = root.findViewById(R.id.messagesList);

        this.handleToolbar();

        // set fab action in activity
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        return root;
    }

    private void handleQuestionnaire(Questionnaire questionnaire) {
        this.mFragmentChatBinding.setQuestionnaire(questionnaire);
        mQuestionnaire = questionnaire;

        if (questionnaire.getId().equalsIgnoreCase("THI")) {
            this.setupTHI();
            this.initTHIAdapter();
        } else {
            this.setupTFI();
            this.initTFIAdapter();
        }
    }

    private void handleQuestionnaireSetting(QuestionnaireSetting questionnaireSetting) {
        this.mFragmentChatBinding.setQuestionnaireSetting(questionnaireSetting);
        this.mQuestionnaireSetting = questionnaireSetting;
        this.toolbar.getMenu().findItem(R.id.questionnaire_simple_control).setVisible(questionnaireSetting.getRemovedQuestions().size() > 0);

        this.enabledChatMessagesQueue = new ArrayList<>(this.allChatMessages);

        // remove all chatMessages containing question (this includes the answer)
        List<Question> removedQuestions = questionnaireSetting.getRemovedQuestions();
        this.enabledChatMessagesQueue = this.allChatMessages.stream()
                .filter(chatMessage -> !removedQuestions.contains(chatMessage.getQuestion()))
                .collect(Collectors.toList());
        this.mMessagesListAdapter.unselectAllItems();
        this.mMessagesListAdapter.clear(true);


        this.enabledChatMessages = new ArrayList<>(this.enabledChatMessagesQueue);

        // update results
        AtomicInteger counter = new AtomicInteger();
        mQuestionResults = this.questions.stream()
                .map(question -> new QuestionResult(
                        null,
                        0L,
                        counter.getAndIncrement(),
                        !this.enabledChatMessages.contains(question)))
                .collect(Collectors.toList());

        this.mMessagesListAdapter.addToStart(this.enabledChatMessagesQueue.remove(0), true);
        this.mMessagesListAdapter.addToStart(this.enabledChatMessagesQueue.remove(0), true);
    }

    private void handleToolbar() {
        this.toolbar.getMenu().clear();
        this.toolbar.inflateMenu(R.menu.menu_questionnaire_control);
        this.toolbar.setOnMenuItemClickListener(item -> {
            mStopWatch.suspend();

            if (item.getItemId() == R.id.questionnaire_simple_control) {

                String[] removedQuestionTitles = this.mFragmentChatBinding.getQuestionnaireSetting().getRemovedQuestions().stream()
                        .map(Question::getText)
                        .sorted()
                        .toArray(String[]::new);

                boolean[] enabled = new boolean[removedQuestionTitles.length];

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.deleted_questions))
                        .setMultiChoiceItems(removedQuestionTitles, enabled, (dialogInterface, i, b) -> enabled[i] = b)
                        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                            for (int j = 0; j < enabled.length; j++) {
                                if (enabled[j]) {
                                    this.mQuestionnaireSetting.reAddQuestion(removedQuestionTitles[j]);
                                    this.mQuestionnairesViewModel.setQuestionnaireSetting(this.mQuestionnaireSetting);
                                }
                            }

                            mStopWatch.resume();
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, which) -> mStopWatch.resume())
                        .setCancelable(false)
                        .show();

            }

            if (item.getItemId() == R.id.questionnaire_simple_delete) {

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.deleted_questions))
                        .setMessage(getString(R.string.remove_questions_chat, this.mMessagesListAdapter.getSelectedMessages().size()))
                        .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

                            if (this.mQuestionnaireSetting == null) {
                                this.mQuestionnaireSetting = new QuestionnaireSetting(this.mQuestionnaire.getId(), new ArrayList<>());
                            }
                            this.mMessagesListAdapter.getSelectedMessages().forEach(chatMessage -> this.mQuestionnaireSetting.addRemovedQuestions(chatMessage.getQuestion()));
                            this.mQuestionnairesViewModel.setQuestionnaireSetting(this.mQuestionnaireSetting);
                            mStopWatch.resume();
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, which) -> mStopWatch.resume())
                        .setCancelable(false)
                        .show();
            }

            return false;
        });
    }

    private void setupTHI() {
        String[] thi = requireActivity().getResources().getStringArray(R.array.thi_survey_questions);

        AtomicInteger i = new AtomicInteger();
        this.questions = Arrays.stream(thi)
                .map(question -> new ChatMessage(
                        UUID.randomUUID().toString(),
                        question,
                        mMyHealthChatUser,
                        new Date(),
                        i.getAndIncrement(),
                        new Question(question, Question.QuestionType.YES_NO_SOMETIMES)))
                .collect(Collectors.toList());

        AtomicInteger j = new AtomicInteger();
        this.answers = Arrays.stream(thi)
                .map(question -> new ChatMessage(
                        UUID.randomUUID().toString(),
                        "test",
                        mChatUser,
                        new Date(),
                        j.getAndIncrement(),
                        new Question(question, Question.QuestionType.YES_NO_SOMETIMES)))
                .collect(Collectors.toList());

        this.allChatMessages = merge(this.questions, this.answers);
    }

    private void initTHIAdapter() {
        MessageHolders messageHolders = new MessageHolders()
                .setOutcomingTextHolder(OutcomingTHIMessageViewHolder.class)
                .setOutcomingTextLayout(R.layout.item_outcoming_thi);

        ImageLoader mImageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
        this.mMessagesListAdapter = new MessagesListAdapter<ChatMessage>(this.mChatUser.getId(), messageHolders, mImageLoader) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.setIsRecyclable(false);
            }
        };

        this.mMessagesListAdapter.enableSelectionMode(count -> this.toolbar.getMenu().findItem(R.id.questionnaire_simple_delete).setVisible(count > 0));
        this.mMessagesList.getRecycledViewPool().setMaxRecycledViews(TYPE_CAROUSEL, 0);
        this.mMessagesList.setAdapter(this.mMessagesListAdapter);
    }

    private void setupTFI() {
        String[] tfi = requireActivity().getResources().getStringArray(R.array.tfi_survey_questions);

        AtomicInteger i = new AtomicInteger();
        this.questions = Arrays.stream(tfi)
                .map(question -> new ChatMessage(
                        UUID.randomUUID().toString(),
                        question,
                        mMyHealthChatUser,
                        new Date(),
                        i.getAndIncrement(),
                        question.contains("1.") || question.contains("2.") ? new Question(question, Question.QuestionType.SLIDER_0_10) : new Question(question, Question.QuestionType.SLIDER_0_100)))
                .collect(Collectors.toList());

        AtomicInteger j = new AtomicInteger();
        this.answers = Arrays.stream(tfi)
                .map(question -> new ChatMessage(
                        UUID.randomUUID().toString(),
                        "test",
                        mChatUser,
                        new Date(),
                        j.getAndIncrement(),
                        question.contains("1.") || question.contains("2.") ? new Question(question, Question.QuestionType.SLIDER_0_10) : new Question(question, Question.QuestionType.SLIDER_0_100)))
                .collect(Collectors.toList());

        this.allChatMessages = merge(this.questions, this.answers);
    }

    private void initTFIAdapter() {
        MessageHolders messageHolders = new MessageHolders()
                .setOutcomingTextHolder(OutcomingTFIMessageViewHolder.class)
                .setOutcomingTextLayout(R.layout.item_outcoming_tfi);

        ImageLoader mImageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
        this.mMessagesListAdapter = new MessagesListAdapter<ChatMessage>(this.mChatUser.getId(), messageHolders, mImageLoader) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.setIsRecyclable(false);
            }
        };
        this.mMessagesListAdapter.enableSelectionMode(count -> this.toolbar.getMenu().findItem(R.id.questionnaire_simple_delete).setVisible(count > 0));
        this.mMessagesList.getRecycledViewPool().setMaxRecycledViews(TYPE_CAROUSEL, 0);
        this.mMessagesList.setAdapter(this.mMessagesListAdapter);
    }

    private void startQuestionnaire(View view) {
        this.fab = (FloatingActionButton) view;
        updateQuestionnaire();
    }

    public void updateQuestionnaire() {
        if (this.enabledChatMessagesQueue.isEmpty()) {

            if (mStopWatch.isStarted() && !mStopWatch.isSuspended()) {
                mStopWatch.suspend();
            }

            // update fab
            this.fab.setImageResource(R.drawable.ic_baseline_check_24);

            // show dialog
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.submit_results))
                    .setMessage(getString(R.string.submit_results_summary, mQuestionResults.stream().filter(questionResult -> questionResult.getValue() != null).count(), this.questions.size()))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mStopWatch.stop();
                        save();
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                        if (mStopWatch.isSuspended()) {
                            mStopWatch.resume();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            this.mMessagesListAdapter.addToStart(this.enabledChatMessagesQueue.remove(0), true);
            this.mMessagesListAdapter.addToStart(this.enabledChatMessagesQueue.remove(0), true);

            if (this.enabledChatMessagesQueue.isEmpty()) {
                // update fab
                this.fab.setImageResource(R.drawable.ic_baseline_check_24);
            }
        }
    }

    private void save() {
        // user
        FirebaseUser firebaseUser = this.mUserViewModel.getFirebaseUser().getValue();

        // IDs
        String questionnaireId = this.mQuestionnaire.getId();
        String userId = firebaseUser != null ? firebaseUser.getUid() : "no_id";
        String trackId = this.mSharedPreferences.getString(getString(R.string.current_spotify_track_key), null);
        OptionalDouble optionalDouble = mQuestionResults.stream().filter(questionResult -> questionResult.getDuration() != null).mapToLong(QuestionResult::getDuration).average();
        double averageDuration = optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0;


        // build result
        QuestionnaireResult result = new QuestionnaireResult(
                questionnaireId,
                userId,
                trackId,
                new Date(),
                mStopWatch.getTime(),
                averageDuration,
                mQuestionResults
        );
        this.mStatsViewModel.uploadQuestionnaireResult(result);
        this.mQuestionnairesViewModel.resetSelected();

    }

    @Override
    public boolean hasContentFor(ChatMessage message, byte type) {
        return true;
    }

    public static <T> ArrayList<T> merge(Collection<T> a, Collection<T> b) {
        Iterator<T> itA = a.iterator();
        Iterator<T> itB = b.iterator();
        ArrayList<T> result = new ArrayList<>();

        while (itA.hasNext() || itB.hasNext()) {
            if (itA.hasNext()) result.add(itA.next());
            if (itB.hasNext()) result.add(itB.next());
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (this.mQuestionnaireLiveData != null) {
            this.mQuestionnaireLiveData.observe(getViewLifecycleOwner(), this::handleQuestionnaire);
        }

        if (this.mQuestionnaireSettingLiveData != null) {
            this.mQuestionnaireSettingLiveData.observe(getViewLifecycleOwner(), this::handleQuestionnaireSetting);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save questionnaire settings for later use
        if (mStopWatch.isStarted()) {
            mStopWatch.suspend();
        }

        if (this.mQuestionnaireLiveData != null) {
            this.mQuestionnaireLiveData.removeObservers(getViewLifecycleOwner());
        }

        if (this.mQuestionnaireSettingLiveData != null) {
            this.mQuestionnaireSettingLiveData.removeObservers(getViewLifecycleOwner());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStopWatch.isSuspended()) {
            mStopWatch.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStopWatch.isStarted()) {
            mStopWatch.reset();
        }
    }

    static class OutcomingTFIMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<ChatMessage> {

        private ChatMessage chatMessage;
        private final Slider slider;

        public OutcomingTFIMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            this.slider = itemView.findViewById(R.id.slider_0_100_chat);

            this.slider.addOnChangeListener((slider, value, fromUser) -> {
                this.chatMessage.getQuestion().setResult(Math.round(value));
                updateValue(chatMessage, Math.round(value));
            });
        }


        @Override
        public void onBind(ChatMessage message) {
            super.onBind(message);
            if (this.chatMessage == null) {
                this.chatMessage = message;
            }
            QuestionResult result = mQuestionResults.get(message.getPosition());

            if (result.getValue() != null) {
                this.slider.setValue(result.getValue());
            }
        }
    }


    static class OutcomingTHIMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<ChatMessage> {

        private final RadioGroup radioGroup;
        private ChatMessage chatMessage;

        public OutcomingTHIMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            this.radioGroup = itemView.findViewById(R.id.group_yes_no_sometimes_chat);
            this.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.yes) {
                    updateValue(chatMessage, 1);
                } else if (checkedId == R.id.no) {
                    updateValue(chatMessage, 2);
                } else if (checkedId == R.id.sometimes) {
                    updateValue(chatMessage, -1);
                }
            });
        }

        @Override
        public void onBind(ChatMessage message) {
            super.onBind(message);
            if (this.chatMessage == null) {
                this.chatMessage = message;
            }
            QuestionResult result = mQuestionResults.get(message.getPosition());

            this.radioGroup.clearCheck();
            if (result.getValue() != null) {
                if (result.getValue() == 1) {
                    ((RadioButton) this.radioGroup.getChildAt(0)).setChecked(true);
                } else if (result.getValue() == 2) {
                    ((RadioButton) this.radioGroup.getChildAt(2)).setChecked(true);
                } else if (result.getValue() == -1) {
                    ((RadioButton) this.radioGroup.getChildAt(1)).setChecked(true);
                }
            }
        }
    }

    public static void updateValue(ChatMessage chatMessage, int value) {
        int pos = chatMessage.getPosition();

        // get result
        QuestionResult result = mQuestionResults.get(pos);

        // set duration
        Long duration = result.getDuration();
        if (duration == null) {
            duration = 0L;
        }

        mStopWatch.split();
        long split = mStopWatch.getSplitTime();
        long interval = (split - mLastSplit);
        result.setDuration(duration + interval);
        mLastSplit = split;
        mStopWatch.unsplit();

        // set value
        result.setValue(value);
        mQuestionResults.set(pos, result);
    }
}