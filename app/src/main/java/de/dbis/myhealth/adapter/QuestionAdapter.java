package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private static final String TAG = "QuestionAdapter";
    private List<Question> mQuestions;
    public QuestionnairesViewModel mQuestionnairesViewModel;

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemQuestionBinding binding;
        private final LinearLayout questionLayout;

        public QuestionViewHolder(@NonNull ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.questionLayout = this.binding.getRoot().findViewById(R.id.question_layout);
        }

        public void bind(Question question) {
            this.binding.setQuestion(question);

            switch (question.getQuestionType()) {
                case YES_NO:
                    View yesNoLayout = this.questionLayout.findViewById(R.id.layout_yes_no);
                    RadioGroup yesNo = yesNoLayout.findViewById(R.id.group_yes_no);
                    yesNo.setOnCheckedChangeListener((radioGroup, id) -> {
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
                    View yesNoSometimesLayout = this.questionLayout.findViewById(R.id.layout_yes_no_sometimes);
                    RadioGroup yesNoSometimes = yesNoSometimesLayout.findViewById(R.id.group_yes_no_sometimes);
                    yesNoSometimes.setOnCheckedChangeListener((radioGroup, id) -> {
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
                case SLIDER_0_10:
                    View slider010Layout = this.questionLayout.findViewById(R.id.layout_slider_0_10);
                    Slider slider010 = slider010Layout.findViewById(R.id.slider_0_10);
                    slider010.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                        @Override
                        public void onStartTrackingTouch(@NonNull Slider slider) {

                        }

                        @Override
                        public void onStopTrackingTouch(@NonNull Slider slider) {
                            updateValue(Math.round(slider.getValue()));
                        }
                    });
                case SLIDER_0_100:
                    View slider0100Layout = this.questionLayout.findViewById(R.id.layout_slider_0_100);
                    Slider slider0100 = slider0100Layout.findViewById(R.id.slider_0_100);
                    slider0100.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                        @Override
                        public void onStartTrackingTouch(@NonNull Slider slider) {

                        }

                        @Override
                        public void onStopTrackingTouch(@NonNull Slider slider) {
                            updateValue(Math.round(slider.getValue()));
                        }
                    });
            }
            this.binding.executePendingBindings();
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

        // Long click on question
        holder.questionLayout.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
//            v.vibrate(VibrationEffect.createOneShot(100, 50));
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
