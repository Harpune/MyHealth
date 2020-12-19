package de.dbis.myhealth.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
    private static final int YES = 1;
    private static final int SOMETIMES = 0;
    private static final int NO = -1;
    private final QuestionnairesViewModel mQuestionnairesViewModel;
    private List<Question> mQuestions;

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemQuestionBinding binding;
        private final LinearLayout questionLayout;
        private final ViewStub viewStub;
        private boolean isInflated = false;

        public QuestionViewHolder(@NonNull ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.questionLayout = binding.getRoot().findViewById(R.id.question_layout);
            this.viewStub = binding.getRoot().findViewById(R.id.layout_stub);
        }

        public void bind(Question question) {
            this.binding.setQuestion(question);
            this.binding.executePendingBindings();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = this.mQuestions.get(position);
        holder.bind(question);

        // Long click on question
        holder.questionLayout.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(100, 50));

            Toast.makeText(view.getContext(), "Hello there", Toast.LENGTH_LONG).show();
            return false;
        });

        // Answer of question
        switch (question.getQuestionType()) {
            case YES_NO:
                holder.viewStub.setLayoutResource(R.layout.item_question_yes_no);
                holder.viewStub.setOnInflateListener((viewStub, view) -> {
                    holder.isInflated = true;
                    RadioGroup radioGroup = holder.binding.getRoot().findViewById(R.id.group_yes_no);
                    radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> question.setResult((Math.round(i))));
                });
                break;
            case YES_NO_SOMETIMES:
                holder.viewStub.setLayoutResource(R.layout.item_question_yes_no_sometimes);
                holder.viewStub.setOnInflateListener((viewStub, view) -> {
                    holder.isInflated = true;
                    RadioGroup radioGroup = holder.binding.getRoot().findViewById(R.id.group_yes_no_sometimes);
                    radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
                        switch (i) {
                            case R.id.yes:
                                question.setResult(YES);
                                break;
                            case R.id.sometimes:
                                question.setResult(SOMETIMES);
                                break;
                            case R.id.no:
                                question.setResult(NO);
                                break;
                        }
                        mQuestionnairesViewModel.updateQuestion(question);
                    });
                });
                break;
            case SLIDER_0_10:
                holder.viewStub.setLayoutResource(R.layout.item_question_slider_0_10);
                holder.viewStub.setOnInflateListener((viewStub, view) -> {
                    holder.isInflated = true;
                    Slider slider = holder.binding.getRoot().findViewById(R.id.slider_0_10);
                    slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                        @Override
                        public void onStartTrackingTouch(@NonNull Slider slider) {

                        }

                        @Override
                        public void onStopTrackingTouch(@NonNull Slider slider) {
                            question.setResult((Math.round(slider.getValue())));
                        }
                    });
                });
                break;
            case SLIDER_0_100:
                holder.viewStub.setLayoutResource(R.layout.item_question_slider_0_100);
                holder.viewStub.setOnInflateListener((viewStub, view) -> {
                    holder.isInflated = true;
                    Slider slider = holder.binding.getRoot().findViewById(R.id.slider_0_100);
                    slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                        @Override
                        public void onStartTrackingTouch(@NonNull Slider slider) {

                        }

                        @Override
                        public void onStopTrackingTouch(@NonNull Slider slider) {
                            question.setResult((Math.round(slider.getValue())));
                        }
                    });
                });
                break;
        }

        if (!holder.isInflated) {
            holder.viewStub.inflate();
        }
    }

    @Override
    public int getItemCount() {
        return this.mQuestions != null ? this.mQuestions.size() : 0;
    }
}
