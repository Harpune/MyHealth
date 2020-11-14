package de.dbis.myhealth.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Questionnaire;

public class QuestionnairesAdapter extends RecyclerView.Adapter<QuestionnairesAdapter.QuestionnairesViewHolder> {

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

            root.setOnClickListener(view -> Toast.makeText(itemView.getContext(), "asdasd", Toast.LENGTH_SHORT).show());
        }
    }

    public QuestionnairesAdapter(List<Questionnaire> questionnaireList) {
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
        holder.description.setText("Clicked");
    }

    @Override
    public int getItemCount() {
        return this.questionnaireList.size();
    }
}
