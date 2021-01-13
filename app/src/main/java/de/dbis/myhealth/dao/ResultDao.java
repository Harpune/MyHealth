package de.dbis.myhealth.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.dbis.myhealth.models.Result;

@Dao
public interface ResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Result result);

    @Query("SELECT * FROM result_table")
    LiveData<List<Result>> getAll();
}
