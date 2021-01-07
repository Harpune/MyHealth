package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ViewStubProxy;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionBinding;
import de.dbis.myhealth.databinding.ItemQuestionSlider0100Binding;
import de.dbis.myhealth.databinding.ItemQuestionSlider010Binding;
import de.dbis.myhealth.databinding.ItemQuestionYesNoBinding;
import de.dbis.myhealth.databinding.ItemQuestionYesNoSometimesBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private static final String TAG = "QuestionAdapter";
    private List<Question> mQuestions;
    public QuestionnairesViewModel mQuestionnairesViewModel;

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemQuestionBinding binding;
        private final LinearLayout questionLayout;
        private final ViewStubProxy viewStubProxy;

        public QuestionViewHolder(@NonNull ItemQuestionBinding binding) {
            super(binding.getRoot());
            // Current binding
            this.binding = binding;

            // Views
            this.questionLayout = this.binding.getRoot().findViewById(R.id.question_layout);
            ViewStub viewStub = this.binding.getRoot().findViewById(R.id.view_stub);
            this.viewStubProxy = new ViewStubProxy(viewStub);
            this.viewStubProxy.setOnInflateListener((inflatedViewStub, inflatedView) -> {
                switch (this.binding.getQuestion().getQuestionType()) {
                    case YES_NO:
                        RadioGroup yesNoRadioGroup = inflatedView.findViewById(R.id.group_yes_no);
                        yesNoRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
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

        public void inflateResult(Question.QuestionType questionType) {
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
            }
        }

        private void updateValue(int value) {
            this.binding.getQuestion().setResult(value);
            mQuestionnairesViewModel.updateQuestion(this.binding.getQuestion());
        }
    }

    public QuestionAdapter(Activity activity) {
        this.mQuestionnairesViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(QuestionnairesViewModel.class);
        this.mQuestionnairesViewModel.getSelected().observe((LifecycleOwner) activity, questionnaire -> this.setData(questionnaire.getQuestions()));
    }

    public void setData(List<Question> questions) {
        this.mQuestions = questions;
        notifyDataSetChanged();
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
        Question question = this.mQuestions.get(position);
        holder.bind(question);
        holder.inflateResult(question.getQuestionType());

        // Long click on question
        holder.questionLayout.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(100, 50));
            Toast.makeText(view.getContext(), "Hello there", Toast.LENGTH_LONG).show();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return this.mQuestions != null ? this.mQuestions.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
