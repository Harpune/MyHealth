package de.dbis.myhealth.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.Record;

@Dao
public interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Record record);

    @Query("SELECT * FROM record_table")
    List<Record> getAll();

    @Query("SELECT * FROM record_table WHERE questionnaire_id = :name")
    Record getByName(String name);
}
