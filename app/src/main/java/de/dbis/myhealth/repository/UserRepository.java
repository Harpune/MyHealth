package de.dbis.myhealth.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import de.dbis.myhealth.models.User;


public class UserRepository {
    private final static String FIREBASE_COLLECTION_USER = "users";
    private final FirebaseFirestore firestore;


    public UserRepository(Application application) {


        // NETWORK
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.firestore = FirebaseFirestore.getInstance();
        this.firestore.setFirestoreSettings(settings);
        // questionnaires
    }

    public void setUser(User user) {
        // save in db

        // save in firestore
        this.firestore.collection(FIREBASE_COLLECTION_USER)
                .document(user.getUserId())
                .set(user);
    }
}
