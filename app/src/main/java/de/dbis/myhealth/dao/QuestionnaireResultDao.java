package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.QuestionnaireResult;

@Dao
public interface QuestionnaireResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QuestionnaireResult result);

    @Query("SELECT * FROM questionnaire_result_table")
    LiveData<List<QuestionnaireResult>> getAll();

    @Query("SELECT * FROM questionnaire_result_table WHERE resultId = :questionnaireResultId")
    LiveData<QuestionnaireResult> get(String questionnaireResultId);
}
