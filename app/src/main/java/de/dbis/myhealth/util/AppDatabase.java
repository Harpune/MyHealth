package de.dbis.myhealth.util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dbis.myhealth.dao.QuestionnaireDao;
import de.dbis.myhealth.dao.RecordDao;
import de.dbis.myhealth.dao.ResultDao;
import de.dbis.myhealth.dao.SpotifyDataDao;
import de.dbis.myhealth.dao.SpotifyTrackDao;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.Record;
import de.dbis.myhealth.models.Result;
import de.dbis.myhealth.models.SpotifyData;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.util.converter.AudioFeaturesTrackConverter;
import de.dbis.myhealth.util.converter.TrackConverter;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;

@Database(entities = {Questionnaire.class, Record.class, Result.class, SpotifyData.class, SpotifyTrack.class}, version = 1, exportSchema = false)
@TypeConverters({TrackConverter.class, AudioFeaturesTrackConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract QuestionnaireDao questionnaireDao();

    public abstract RecordDao recordDao();

    public abstract ResultDao resultDao();

    public abstract SpotifyDataDao spotifyDataDao();

    public abstract SpotifyTrackDao spotifyTrackDao();

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database").build();
                }
            }
        }

        return INSTANCE;
    }
}
