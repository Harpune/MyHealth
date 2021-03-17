package de.dbis.myhealth.ui.user;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.util.Date;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.models.User;

import static de.dbis.myhealth.ApplicationConstants.FIREBASE_COLLECTION_USERS;

public class UserViewModel extends AndroidViewModel {
    private Preference mPreference;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<User> mUser;
    private final MutableLiveData<FirebaseUser> mFirebaseUser;

    public UserViewModel(Application application) {
        super(application);
        this.mPreference = PowerPreference.getDefaultFile();

        // NETWORK
        this.mAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        // settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.firestore.setFirestoreSettings(settings);

        // live data
        this.mUser = new MutableLiveData<>();
        this.mFirebaseUser = new MutableLiveData<>();

        // Get user from firebase
        FirebaseUser firebaseUser = this.mAuth.getCurrentUser();
        if (firebaseUser != null) {
            this.setFirebaseUser(firebaseUser);
        } else {
            this.signIn();
        }

    }

    private void signIn() {
        this.mAuth.signInAnonymously()
                .addOnCompleteListener(ContextCompat.getMainExecutor(getApplication()), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Firebase Login", "signInAnonymously:success");
                        this.setFirebaseUser(this.mAuth.getCurrentUser());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Firebase Login", "signInAnonymously:failure", task.getException());
                        Toast.makeText(getApplication(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setFirebaseUser(FirebaseUser firebaseUser) {
        // update firebase user
        this.mFirebaseUser.setValue(firebaseUser);

        // get user from preference
        User user = this.mPreference.getObject(firebaseUser.getUid(), User.class, new User(firebaseUser.getUid()));
        this.setUser(user);
    }

    public LiveData<FirebaseUser> getFirebaseUser() {
        return mFirebaseUser;
    }

    public void setUser(User user) {
        this.mUser.setValue(user);
    }

    public LiveData<User> getUser() {
        return this.mUser;
    }

    public User getCurrentUser(String firebaseId) {
        return this.mPreference.getObject(firebaseId, User.class, new User(firebaseId));
    }

    public void save() {
        User user = this.mUser.getValue();
        if (user != null) {
            user.setUpdateDate(new Date());

            FirebaseUser firebaseUser = this.mFirebaseUser.getValue();
            if (firebaseUser != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(user.getName())
                        .build();

                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firebase Login", "User profile updated.");
                    }
                });

            }

            this.firestore.collection(FIREBASE_COLLECTION_USERS)
                    .document(user.getUserId())
                    .set(user);
        }
    }
}