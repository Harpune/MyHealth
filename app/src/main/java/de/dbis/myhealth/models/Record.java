package de.dbis.myhealth.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "record_table")
public class Record {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "questionnaire_id")
    public String questionnaireId;
    @ColumnInfo(name = "answered_questionnaires")
    public int answeredQuestionnaires;
    @ColumnInfo(name = "answered_questions")
    public int answeredQuestions;
    @ColumnInfo(name = "average_answer_time")
    public int averageAnswerTime;
}
