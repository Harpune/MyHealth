package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionnairesAdapter extends RecyclerView.Adapter<QuestionnairesAdapter.QuestionnairesViewHolder> {

    private QuestionnairesViewModel mViewModel;
    private List<Questionnaire> questionnaireList;

    public static class QuestionnairesViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout root;
        TextView title;
        TextView description;

        public QuestionnairesViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.questionnaireRoot);
            title = itemView.findViewById(R.id.questionnaireTitle);
            description = itemView.findViewById(R.id.questionnaireDescription);
        }
    }

    public QuestionnairesAdapter(Activity activity, List<Questionnaire> questionnaireList) {
        this.questionnaireList = questionnaireList;
        this.mViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(QuestionnairesViewModel.class);

    }

    public void setData(List<Questionnaire> questionnaireList) {
        this.questionnaireList = questionnaireList;
    }

    @NonNull
    @Override
    public QuestionnairesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_questionnaires, parent, false);
        return new QuestionnairesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionnairesViewHolder holder, int position) {
        Questionnaire questionnaire = this.questionnaireList.get(position);
        holder.title.setText(questionnaire.getTitle());
        holder.description.setText(questionnaire.getDescription());

        holder.root.setOnClickListener(view -> {
            this.mViewModel.select(questionnaire);
            Navigation.findNavController(view).navigate(R.id.action_nav_questionnaires_to_nav_questionnaire);
            //Navigation.findNavController(view).navigate(R.id.nav_questionnaire);
        });
    }

    @Override
    public int getItemCount() {
        return this.questionnaireList.size();
    }
}
