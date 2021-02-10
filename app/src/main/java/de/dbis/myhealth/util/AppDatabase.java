package de.dbis.myhealth.util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dbis.myhealth.dao.QuestionnaireDao;
import de.dbis.myhealth.dao.QuestionnaireResultDao;
import de.dbis.myhealth.dao.QuestionnaireSettingDao;
import de.dbis.myhealth.dao.SpotifyTrackDao;
import de.dbis.myhealth.dao.UserDao;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireResult;
import de.dbis.myhealth.models.QuestionnaireSetting;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.models.User;
import de.dbis.myhealth.util.converter.AudioFeaturesTrackConverter;
import de.dbis.myhealth.util.converter.TrackConverter;

@Database(entities = {Questionnaire.class, QuestionnaireResult.class, SpotifyTrack.class, QuestionnaireSetting.class, User.class}, version = 1, exportSchema = false)
@TypeConverters({TrackConverter.class, AudioFeaturesTrackConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract QuestionnaireDao questionnaireDao();

    public abstract QuestionnaireResultDao resultDao();

    public abstract QuestionnaireSettingDao questionnaireSettingDao();

    public abstract SpotifyTrackDao spotifyTrackDao();

    public abstract UserDao userDao();

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
