package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.Questionnaire;

@Dao
public interface QuestionnaireDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Questionnaire questionnaire);

    @Query("SELECT * FROM questionnaire_table ORDER BY title ASC")
    LiveData<List<Questionnaire>> getAll();

    @Query("SELECT * FROM questionnaire_table WHERE id = :id")
    LiveData<Questionnaire> get(String id);

    @Query("DELETE FROM questionnaire_table")
    void deleteAll();
}
