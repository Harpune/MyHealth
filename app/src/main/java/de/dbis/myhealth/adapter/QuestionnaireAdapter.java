package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionnaireBinding;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionnairesViewHolder> {

    private List<Questionnaire> mQuestionnaires;
    private final QuestionnairesViewModel mViewHolder;

    public static class QuestionnairesViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionnaireBinding binding;

        public QuestionnairesViewHolder(@NonNull ItemQuestionnaireBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Questionnaire questionnaire) {
            this.binding.setQuestionnaires(questionnaire);
            this.binding.executePendingBindings();
        }
    }

    public QuestionnaireAdapter(MainActivity activity) {
        this.mViewHolder = new ViewModelProvider(activity).get(QuestionnairesViewModel.class);
        this.mViewHolder.getQuestionnaires().observe(activity, this::setData);
    }

    public void setData(List<Questionnaire> questionnaires) {
        this.mQuestionnaires = questionnaires;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionnairesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemQuestionnaireBinding itemBinding = ItemQuestionnaireBinding.inflate(layoutInflater, parent, false);

        // on Click
        itemBinding.questionnaireRoot.setOnClickListener(view -> {
            this.mViewHolder.select(itemBinding.getQuestionnaires());
            Navigation.findNavController(view).navigate(R.id.action_nav_questionnaires_to_nav_questionnaire);
        });
        return new QuestionnairesViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionnairesViewHolder holder, int position) {
        Questionnaire questionnaire = this.mQuestionnaires.get(position);
        holder.bind(questionnaire);
    }

    @Override
    public int getItemCount() {
        return this.mQuestionnaires != null ? this.mQuestionnaires.size() : 0;
    }
}
