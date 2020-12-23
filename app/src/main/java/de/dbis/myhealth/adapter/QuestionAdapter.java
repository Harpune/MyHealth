package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ViewStubProxy;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionBinding;
import de.dbis.myhealth.databinding.ItemQuestionYesNoSometimesBinding;
import de.dbis.myhealth.models.Question;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private static final String TAG = "QuestionAdapter";
    private List<Question> mQuestions;

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final ItemQuestionBinding binding;
        private final LinearLayout questionLayout;
        private boolean isInflated;

        public QuestionViewHolder(@NonNull ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.questionLayout = binding.getRoot().findViewById(R.id.question_layout);
            this.binding = binding;
            this.binding.viewStub.setOnInflateListener((viewStub, view) -> {

            });
        }

        public void bind(Question question) {
            this.binding.setQuestion(question);
            this.binding.executePendingBindings();
        }

        public void inflateResult(Question.QuestionType questionType) {
            ViewStub viewStub = this.binding.getRoot().findViewById(R.id.view_stub);
            if (viewStub != null) {
                switch (questionType){
                    case YES_NO:
                        viewStub.setLayoutResource(R.layout.item_question_yes_no);
                        break;
                    case YES_NO_SOMETIMES:
                        viewStub.setLayoutResource(R.layout.item_question_yes_no_sometimes);
                        break;
                    case SLIDER_0_10:
                        viewStub.setLayoutResource(R.layout.item_question_slider_0_10);
                        break;
                    case SLIDER_0_100:
                        viewStub.setLayoutResource(R.layout.item_question_slider_0_100);
                        break;
                }

                ItemQuestionYesNoSometimesBinding yesNoSometimesBinding = ItemQuestionYesNoSometimesBinding.inflate(LayoutInflater.from(this.binding.getRoot().getContext()));
                yesNoSometimesBinding.setQuestion(this.binding.getQuestion());

                ViewStubProxy viewStubProxy = new ViewStubProxy(viewStub);
                viewStubProxy.setContainingBinding(yesNoSometimesBinding);

                if (!viewStubProxy.isInflated()) {
                    viewStubProxy.getViewStub().inflate();
                }
            }
        }
    }

    public QuestionAdapter(Activity activity) {
        QuestionnairesViewModel mQuestionnairesViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(QuestionnairesViewModel.class);
        mQuestionnairesViewModel.getSelected().observe((LifecycleOwner) activity, questionnaire -> this.setData(questionnaire.getQuestions()));
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
}
