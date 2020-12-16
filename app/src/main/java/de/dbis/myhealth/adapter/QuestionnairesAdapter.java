package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionnairesBinding;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionnairesAdapter extends RecyclerView.Adapter<QuestionnairesAdapter.QuestionnairesViewHolder> {

    private List<Questionnaire> questionnaireList;
    private QuestionnairesViewModel mViewHolder;

    public static class QuestionnairesViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionnairesBinding binding;

        public QuestionnairesViewHolder(@NonNull ItemQuestionnairesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Questionnaire questionnaire) {
            this.binding.setQuestionnaires(questionnaire);
            this.binding.executePendingBindings();
        }
    }

    public QuestionnairesAdapter(Activity activity) {
        this.mViewHolder = new ViewModelProvider((ViewModelStoreOwner) activity).get(QuestionnairesViewModel.class);
        this.mViewHolder.getQuestionnaires().observe((LifecycleOwner) activity, this::setData);
    }

    public void setData(List<Questionnaire> questionnaireList) {
        this.questionnaireList = questionnaireList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionnairesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemQuestionnairesBinding itemBinding = ItemQuestionnairesBinding.inflate(layoutInflater, parent, false);

        itemBinding.questionnaireRoot.setOnClickListener(view -> {
            this.mViewHolder.select(itemBinding.getQuestionnaires());
            Navigation.findNavController(view).navigate(R.id.action_nav_questionnaires_to_nav_questionnaire);
        });
        return new QuestionnairesViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionnairesViewHolder holder, int position) {
        Questionnaire questionnaire = this.questionnaireList.get(position);
        holder.bind(questionnaire);
    }

    @Override
    public int getItemCount() {
        return this.questionnaireList != null ? this.questionnaireList.size() : 0;
    }
}
