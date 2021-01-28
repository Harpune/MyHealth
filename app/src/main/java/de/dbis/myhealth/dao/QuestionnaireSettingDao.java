package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.QuestionnaireSetting;

@Dao
public interface QuestionnaireSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QuestionnaireSetting setting);

    @Query("SELECT * FROM questionnaire_setting_table")
    LiveData<List<QuestionnaireSetting>> getAll();

    @Query("SELECT * FROM questionnaire_setting_table WHERE questionnaireId = :questionnaireId")
    LiveData<QuestionnaireSetting> getQuestionnaireSettingById(String questionnaireId);

}
