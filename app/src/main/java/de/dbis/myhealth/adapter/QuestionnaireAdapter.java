package de.dbis.myhealth.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.ArrayList;
import java.util.List;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemQuestionnaireBinding;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.ui.questionnaires.QuestionnairesViewModel;

public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionnairesViewHolder> {

    private Preference mPreference;

    private final MainActivity mActivity;
    private List<Questionnaire> mQuestionnaires;
    private final QuestionnairesViewModel mQuestionnairesViewModel;

    public class QuestionnairesViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionnaireBinding binding;

        public QuestionnairesViewHolder(@NonNull ItemQuestionnaireBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Questionnaire questionnaire) {
            this.binding.setQuestionnaire(questionnaire);
            this.binding.executePendingBindings();


            this.binding.getRoot().setOnLongClickListener(view -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                new MaterialAlertDialogBuilder(view.getContext())
                        .setTitle("Save as Favourite")
                        .setMessage("You can select this questionnaire as favourite to fast start on the home page.")
                        .setPositiveButton("Save", (dialogInterface, i) -> {
                            saveFavourite(getAdapterPosition());
                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            });
        }
    }

    public QuestionnaireAdapter(MainActivity activity) {
        this.mActivity = activity;
        this.mQuestionnairesViewModel = new ViewModelProvider(activity).get(QuestionnairesViewModel.class);
        this.mQuestionnairesViewModel.getAllQuestionnaires().observe(activity, this::setData);
        this.mPreference = PowerPreference.getDefaultFile();
    }

    public void setData(List<Questionnaire> questionnaires) {
        this.mQuestionnaires = questionnaires;
        notifyDataSetChanged();
    }

    public void saveFavourite(int position) {
        Questionnaire questionnaire = this.mQuestionnaires.get(position);
        this.mActivity.getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(this.mActivity.getString(R.string.questionnaire_fast_start_key), questionnaire.getId())
                .apply();
        Toast.makeText(this.mActivity, "Your new favourite questionnaire is " + questionnaire.getTitle() + " now.", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public QuestionnairesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemQuestionnaireBinding itemBinding = ItemQuestionnaireBinding.inflate(layoutInflater, parent, false);

        // on Click
        itemBinding.questionnaireRoot.setOnClickListener(view -> {
            // Get questionnaire and its setting
            Questionnaire questionnaire = itemBinding.getQuestionnaire();
            QuestionnaireSetting questionnaireSetting = this.mPreference.getObject(
                    questionnaire.getId(),
                    QuestionnaireSetting.class,
                    new QuestionnaireSetting(questionnaire.getId(), new ArrayList<>()));

            // save questiionnaire and its setting in liveData to observe
            this.mQuestionnairesViewModel.select(questionnaire);
            this.mQuestionnairesViewModel.setQuestionnaireSetting(questionnaireSetting);

            // Nav to questionnaire
            Bundle bundle = new Bundle();
            bundle.putString("questionnaire_id", questionnaire.getId());
            Navigation.findNavController(view).navigate(R.id.action_nav_questionnaires_to_nav_questionnaire, bundle);
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
