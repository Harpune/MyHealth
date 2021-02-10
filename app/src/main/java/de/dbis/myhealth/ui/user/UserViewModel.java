package de.dbis.myhealth.ui.user;

import android.app.Application;
import android.content.Context;

import androidx.databinding.Bindable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.User;
import de.dbis.myhealth.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository mUserRepository;

    private final MutableLiveData<User> mUser;
    private final MutableLiveData<String> mDeviceId;

    public UserViewModel(Application application) {
        super(application);
        this.mUserRepository = new UserRepository(application);
        this.mUser = new MutableLiveData<>();
        this.mDeviceId = new MutableLiveData<>();

        String deviceId = getApplication().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                .getString(getApplication().getString(R.string.device_id), null);

        if (deviceId != null) {
            this.mDeviceId.setValue(deviceId);

            User user = this.getCurrentUser(deviceId);
            if (user == null) {
                user = new User(deviceId);
                this.mUser.setValue(user);
                this.save();
            }

            this.mUser.setValue(user);

        }

    }

    public LiveData<User> getUser() {
        return this.mUser;
    }

    public void setUser(User user) {
        this.mUser.setValue(user);
    }

    public User getCurrentUser(String deviceId) {
        return this.mUserRepository.getUser(deviceId);
    }

    public void save() {
        this.mUserRepository.setUser(this.mUser.getValue());
    }
}