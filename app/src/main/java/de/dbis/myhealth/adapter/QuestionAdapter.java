package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewStubProxy;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.preference.PowerPreference;
import com.preference.Preference;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionBinding;
import de.dbis.myhealth.databinding.ItemQuestionSlider0100Binding;
import de.dbis.myhealth.databinding.ItemQuestionSlider010Binding;
import de.dbis.myhealth.databinding.ItemQuestionYesNoBinding;
import de.dbis.myhealth.databinding.ItemQuestionYesNoSometimesBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private static final String TAG = "QuestionAdapter";

    // helper classes
    public QuestionnairesViewModel mQuestionnairesViewModel;
    private final Preference mPreference = PowerPreference.getDefaultFile();
    private final LifecycleOwner mLifecycleOwner;

    // questionnaire
    private Questionnaire mQuestionnaire;
    private QuestionnaireSetting mQuestionnaireSetting;
    private List<Question> mSelectedQuestions;
    private List<Question> mAllQuestions = new ArrayList<>();
    private LiveData<QuestionnaireSetting> mQuestionnaireSettingLiveData;

    // timer
    private final StopWatch mStopWatch;
    private long[] mTimers;
    private long mLastSplit;

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemQuestionBinding binding;
        private final ViewStubProxy viewStubProxy;

        public QuestionViewHolder(@NonNull ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Long click on question
            LinearLayout questionLayout = this.binding.getRoot().findViewById(R.id.question_layout);
            questionLayout.setOnLongClickListener(view -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                String delete = view.getContext().getString(R.string.delete);
                new MaterialAlertDialogBuilder(view.getContext())
                        .setTitle(delete)
                        .setMessage("Do you really want to remove this question from the questionnaire? You can add it back later by click +")
                        .setPositiveButton(delete, (dialogInterface, i) -> {
//                            binding.getQuestion().setResult(null);
                            removeAt(getLayoutPosition());
                        })
                        .show();
                return false;
            });


            ViewStub viewStub = this.binding.getRoot().findViewById(R.id.view_stub);
            this.viewStubProxy = new ViewStubProxy(viewStub);
            this.viewStubProxy.setOnInflateListener((inflatedViewStub, inflatedView) -> {
                switch (this.binding.getQuestion().getQuestionType()) {
                    case YES_NO:
                        RadioGroup yesNoRadioGroup = inflatedView.findViewById(R.id.group_yes_no);
                        yesNoRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
                            this.updateTime();
                            if (id == R.id.yes) {
                                this.updateValue(1);
                            } else if (id == R.id.no) {
                                this.updateValue(2);
                            } else {
                                this.updateValue(-1);
                            }
                        });
                        break;
                    case YES_NO_SOMETIMES:
                        RadioGroup yesNoSometimesRadioGroup = inflatedView.findViewById(R.id.group_yes_no_sometimes);
                        yesNoSometimesRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
                            this.updateTime();
                            if (id == R.id.yes) {
                                this.updateValue(1);
                            } else if (id == R.id.no) {
                                this.updateValue(2);
                            } else if (id == R.id.sometimes) {
                                this.updateValue(0);
                            } else {
                                this.updateValue(-1);
                            }
                        });
                        break;
                    case SLIDER_0_10:
                        Slider slider010 = inflatedView.findViewById(R.id.slider_0_10);
                        slider010.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                            @Override
                            public void onStartTrackingTouch(@NonNull Slider slider) {

                            }

                            @Override
                            public void onStopTrackingTouch(@NonNull Slider slider) {
                                updateTime();
                                updateValue(Math.round(slider.getValue()));
                            }
                        });
                        break;
                    case SLIDER_0_100:
                        Slider slider0100 = inflatedView.findViewById(R.id.slider_0_100);
                        slider0100.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                            @Override
                            public void onStartTrackingTouch(@NonNull Slider slider) {

                            }

                            @Override
                            public void onStopTrackingTouch(@NonNull Slider slider) {
                                updateTime();
                                updateValue(Math.round(slider.getValue()));
                            }
                        });
                        break;
                }
            });
        }

        public void bind(Question question) {
            this.binding.setQuestion(question);
            this.binding.executePendingBindings();
        }

        public void inflate(Question.QuestionType questionType) {
            if (this.viewStubProxy.getViewStub() != null) {
                switch (questionType) {
                    case YES_NO:
                        this.viewStubProxy.getViewStub().setLayoutResource(R.layout.item_question_yes_no);

                        ItemQuestionYesNoBinding yesNoBinding = ItemQuestionYesNoBinding.inflate(LayoutInflater.from(this.binding.getRoot().getContext()));
                        yesNoBinding.setQuestion(this.binding.getQuestion());
                        yesNoBinding.executePendingBindings();
                        this.viewStubProxy.setContainingBinding(yesNoBinding);
                        break;
                    case YES_NO_SOMETIMES:
                        this.viewStubProxy.getViewStub().setLayoutResource(R.layout.item_question_yes_no_sometimes);

                        ItemQuestionYesNoSometimesBinding yesNoSometimesBinding = ItemQuestionYesNoSometimesBinding.inflate(LayoutInflater.from(this.binding.getRoot().getContext()));
                        yesNoSometimesBinding.setQuestion(this.binding.getQuestion());
                        yesNoSometimesBinding.executePendingBindings();
                        this.viewStubProxy.setContainingBinding(yesNoSometimesBinding);
                        break;
                    case SLIDER_0_10:
                        this.viewStubProxy.getViewStub().setLayoutResource(R.layout.item_question_slider_0_10);

                        ItemQuestionSlider010Binding slider010Binding = ItemQuestionSlider010Binding.inflate(LayoutInflater.from(this.binding.getRoot().getContext()));
                        slider010Binding.setQuestion(this.binding.getQuestion());
                        slider010Binding.executePendingBindings();
                        this.viewStubProxy.setContainingBinding(slider010Binding);
                        break;
                    case SLIDER_0_100:
                        this.viewStubProxy.getViewStub().setLayoutResource(R.layout.item_question_slider_0_100);

                        ItemQuestionSlider0100Binding slider0100Binding = ItemQuestionSlider0100Binding.inflate(LayoutInflater.from(this.binding.getRoot().getContext()));
                        slider0100Binding.setQuestion(this.binding.getQuestion());
                        slider0100Binding.executePendingBindings();
                        this.viewStubProxy.setContainingBinding(slider0100Binding);
                        break;
                }

                if (!this.viewStubProxy.isInflated()) {
                    this.viewStubProxy.getViewStub().inflate();
                }
            } else {
                Log.d(TAG, this.binding.getQuestion().toString());
            }
        }

        private void updateValue(int value) {
            // set value
            Question question = this.binding.getQuestion();
            question.setResult(value);

            // update selected Questionnaire with value
            List<Question> questions = mQuestionnaire.getQuestions();

            // Get index of question
            OptionalInt index = IntStream.range(0, questions.size())
                    .filter(i -> questions.get(i).getText().equals(question.getText()))
                    .findFirst();

            // update item in questions-list
            if (index.isPresent()) {
                questions.set(index.getAsInt(), question);
                mQuestionnaire.setQuestions(questions);

                // save
                mQuestionnairesViewModel.select(mQuestionnaire);
            }

        }

        private void updateTime() {
            // get position in all question of current questions
            Question question = mSelectedQuestions.get(getAdapterPosition());
            int position = mAllQuestions.indexOf(question);

            // Get interval between answered questions
            mStopWatch.split();
            long split = mStopWatch.getSplitTime();
            long interval = (split - mLastSplit);
            mTimers[position] = mTimers[position] + interval;
            mLastSplit = split;
            mStopWatch.unsplit();
        }
    }

    public QuestionAdapter(Activity activity, LifecycleOwner lifecycleOwner, StopWatch stopWatch) {
        this.mLifecycleOwner = lifecycleOwner;
        this.mStopWatch = stopWatch;

        this.mLastSplit = stopWatch.getTime(TimeUnit.MILLISECONDS);

        this.mQuestionnairesViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(QuestionnairesViewModel.class);

        // get and set questionnaire
        Questionnaire questionnaire = this.mQuestionnairesViewModel.getSelectedQuestionnaire().getValue();
        if (questionnaire != null) {
            this.setQuestionnaire(questionnaire);

            // get and set questionnaire settings
            this.mQuestionnaireSettingLiveData = this.mQuestionnairesViewModel.getQuestionnaireSetting();
            this.mQuestionnaireSettingLiveData.observe(this.mLifecycleOwner, this::setQuestionnaireSetting);
        }
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.mQuestionnaire = questionnaire;
        this.mSelectedQuestions = questionnaire.getQuestions();
        this.mAllQuestions = this.cloneQuestions(this.mSelectedQuestions);
        this.mTimers = new long[this.mAllQuestions.size()];
        notifyDataSetChanged();
    }

    private void setQuestionnaireSetting(QuestionnaireSetting questionnaireSetting) {
        // questionnaire view model
        this.mQuestionnaireSetting = questionnaireSetting;

        this.mSelectedQuestions = this.cloneQuestions(this.mAllQuestions);

        if (questionnaireSetting != null) {
            List<Question> removedQuestions = questionnaireSetting.getRemovedQuestions();
            for (ListIterator<Question> iter = this.mSelectedQuestions.listIterator(); iter.hasNext(); ) {
                Question question = iter.next();
                if (removedQuestions.contains(question)) {
                    iter.remove();
                    notifyItemRemoved(iter.nextIndex());
                    notifyItemRangeRemoved(iter.nextIndex(), this.mSelectedQuestions.size());
                }
            }
        }
    }

    public void removeAt(int position) {
        Question questionToRemove = this.mSelectedQuestions.get(position);
        questionToRemove.setResult(null);

        QuestionnaireSetting questionnaireSetting = this.mQuestionnaireSetting;
        if (questionnaireSetting == null) {
            questionnaireSetting = new QuestionnaireSetting(this.mQuestionnaire.getId(), new ArrayList<>());
        }
        questionnaireSetting.addRemovedQuestions(questionToRemove);
        this.mQuestionnairesViewModel.setQuestionnaireSetting(questionnaireSetting);

    }

    public void removeObserver() {
        if (this.mQuestionnaireSettingLiveData != null) {
            this.mQuestionnaireSettingLiveData.removeObservers(this.mLifecycleOwner);
        }
    }

    public long[] getTimers() {
        return this.mTimers;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemQuestionBinding itemBinding = ItemQuestionBinding.inflate(layoutInflater, parent, false);
        return new QuestionViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = this.mSelectedQuestions.get(position);
        holder.bind(question);
        holder.inflate(question.getQuestionType());
    }

    @Override
    public int getItemCount() {
        return this.mSelectedQuestions != null ? this.mSelectedQuestions.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private List<Question> cloneQuestions(List<Question> questions) {
        List<Question> questionList = new ArrayList<>();
        for (Question q : questions) {
            questionList.add((Question) q.clone());
        }
        return questionList;
    }

}
