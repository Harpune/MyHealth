package de.dbis.myhealth.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.SessionsClient;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<SessionsClient> mSessionsClient;
    private MutableLiveData<SpotifyAppRemote> mSpotify;

    public SettingsViewModel() {
        this.mSessionsClient = new MutableLiveData<>();
        this.mSpotify = new MutableLiveData<>();
    }

    public LiveData<SessionsClient> getSessionClient() {
        return this.mSessionsClient;
    }

    public void setSessionsClient(SessionsClient sessionsClient) {
        this.mSessionsClient.setValue(sessionsClient);
    }

    public LiveData<SpotifyAppRemote> getSpotify() {
        return this.mSpotify;
    }

    public void setSpotify(SpotifyAppRemote spotify) {
        this.mSpotify.setValue(spotify);
    }
}