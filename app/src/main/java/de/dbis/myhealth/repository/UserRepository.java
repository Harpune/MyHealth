package de.dbis.myhealth.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import de.dbis.myhealth.dao.UserDao;
import de.dbis.myhealth.models.User;
import de.dbis.myhealth.util.AppDatabase;

public class UserRepository {

    private UserDao mUserDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.mUserDao = db.userDao();
    }

    public void setUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> mUserDao.insert(user));
    }

    public User getUser(String deviceId) {
        return this.mUserDao.get(deviceId);
    }
}
