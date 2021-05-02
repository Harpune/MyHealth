package de.dbis.myhealth.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemResultBinding;
import de.dbis.myhealth.models.QuestionnaireResult;
import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;

import static android.content.Context.MODE_PRIVATE;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {
    private final static String TAG = "HomeAdapter";

    // Data
    private List<QuestionnaireResult> mQuestionnaireResults;

    // Activity
    private Activity mActivity;


    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        private ItemResultBinding itemResultBinding;
        private View root;
        private DataTable dataTable;

        public ResultViewHolder(@NonNull ItemResultBinding binding) {
            super(binding.getRoot());
            this.root = binding.getRoot();
            this.itemResultBinding = binding;
            this.dataTable = this.root.findViewById(R.id.result_data_table);
        }

        public void bind(QuestionnaireResult questionnaireResult, Context context) {
            this.itemResultBinding.setResult(questionnaireResult);

            DataTableHeader header = new DataTableHeader.Builder()
                    .item(context.getString(R.string.number_abbreviation), 1)
                    .item(context.getString(R.string.track_duration), 2)
                    .item(context.getString(R.string.removed), 2)
                    .item(context.getString(R.string.result), 3)
                    .build();

            ArrayList<DataTableRow> rows = new ArrayList<>();

            switch (questionnaireResult.getQuestionnaireId()) {
                case "TFI":
                    rows = (ArrayList<DataTableRow>) questionnaireResult.getQuestionResults().stream().map(questionResult -> {
                        String questionNumber = String.valueOf(questionResult.getQuestionNumber() + 1);
                        String questionDuration = questionResult.getDuration() > 0 ? getDurationFormat(questionResult.getDuration()) : "";
                        String questionValue = "";
                        if (questionResult.getValue() != null) {
                            if (0 <= questionResult.getQuestionNumber() && questionResult.getQuestionNumber() < 3) {
                                questionValue = String.valueOf(questionResult.getValue()).concat("%");
                            } else {
                                questionValue = String.valueOf(questionResult.getValue());
                            }
                        }

                        return new DataTableRow.Builder()
                                .value(questionNumber)
                                .value(questionDuration)
                                .value(questionResult.isRemoved() ? context.getString(R.string.removed) : "")
                                .value(questionValue)
                                .build();

                    }).collect(Collectors.toList());
                    break;
                case "THI":
                    rows = (ArrayList<DataTableRow>) questionnaireResult.getQuestionResults().stream().map(questionResult -> {
                        String questionNumber = String.valueOf(questionResult.getQuestionNumber() + 1);
                        String questionDuration = questionResult.getDuration() > 0 ? getDurationFormat(questionResult.getDuration()) : "";
                        String questionValue = "";
                        if (questionResult.getValue() != null) {
                            switch (questionResult.getValue()) {
                                case 0:
                                    questionValue = context.getString(R.string.sometimes);
                                    break;
                                case 1:
                                    questionValue = context.getString(R.string.yes);
                                    break;
                                case 2:
                                    questionValue = context.getString(R.string.no);
                                    break;
                            }
                        }

                        return new DataTableRow.Builder()
                                .value(questionNumber)
                                .value(questionDuration)
                                .value(questionResult.isRemoved() ? context.getString(R.string.removed) : "")
                                .value(questionValue)
                                .build();
                    }).collect(Collectors.toList());
                    break;
            }

            this.dataTable.setTypeface(Typeface.DEFAULT);
            this.dataTable.setHeader(header);
            this.dataTable.setRows(rows);

            boolean darkModeEnabled = context.getSharedPreferences(ApplicationConstants.PREFERENCES, MODE_PRIVATE)
                    .getBoolean(context.getString(R.string.dark_mode_key), false);

            this.dataTable.setBackgroundColor(context.getColor(android.R.color.transparent));
            if (darkModeEnabled) {
                this.dataTable.setDividerColor(context.getColor(R.color.white));
                this.dataTable.setHeaderTextColor(context.getColor(R.color.white));
                this.dataTable.setRowTextColor(context.getColor(R.color.white));
            } else {
                this.dataTable.setDividerColor(context.getColor(R.color.black));
                this.dataTable.setHeaderTextColor(context.getColor(R.color.black));
                this.dataTable.setRowTextColor(context.getColor(R.color.black));
            }


            this.dataTable.inflate(context);

        }


        @SuppressLint("DefaultLocale")
        private String getDurationFormat(long value) {
            int milliSeconds = (int) (value % 1000);
            int seconds = (int) (value / 1000) % 60;
            int minutes = (int) ((value / (1000 * 60)) % 60);
            int hours = (int) ((value / (1000 * 60 * 60)) % 24);

            if (hours > 0) {
                return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliSeconds);
            }
            return String.format("%02d:%02d:%02d", minutes, seconds, milliSeconds);
        }
    }

    public ResultAdapter(Activity activity) {
        this.mActivity = activity;
    }


    public void setData(List<QuestionnaireResult> questionnaireResult) {
        this.mQuestionnaireResults = questionnaireResult;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemResultBinding itemResultBinding = ItemResultBinding.inflate(layoutInflater, parent, false);

        return new ResultViewHolder(itemResultBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        QuestionnaireResult questionnaireResult = this.mQuestionnaireResults.get(position);
        holder.bind(questionnaireResult, this.mActivity);
    }

    @Override
    public int getItemCount() {
        return this.mQuestionnaireResults != null ? this.mQuestionnaireResults.size() : 0;
    }
}
