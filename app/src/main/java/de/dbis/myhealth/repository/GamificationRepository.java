package de.dbis.myhealth.repository;

import android.app.Application;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Gamification;

@Deprecated
public class GamificationRepository {
    private final static String TAG = "GamificationRepository";

    private final FirebaseFirestore firestore;

    public GamificationRepository(Application application) {

        // NETWORK
        this.firestore = FirebaseFirestore.getInstance();

        this.subscribeToGamification();
        this.generateGamification(application);
    }

    public void subscribeToGamification() {
        this.firestore.collection(ApplicationConstants.FIREBASE_COLLECTION_GAMIFICATION)
                .addSnapshotListener((task, error) -> {
                    Log.d(TAG, "Gamification changed!");
                    if (error != null) {
                        Log.w(TAG, "Listen failed", error);
                        return;
                    }

                    if (task != null && !task.isEmpty()) {
                        task.getDocuments().stream().map(documentSnapshot -> documentSnapshot.toObject(Gamification.class));
                    } else {
                        Log.d(TAG, "No results found");
                    }
                });

    }

    public void generateGamification(Application application) {

        List<Gamification> data = new ArrayList<>();
        data.add(new Gamification(application.getString(R.string.gamePaperKey), R.drawable.icon_paper, application.getString(R.string.gamePaper), new long[]{10}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameDiamondKey), R.drawable.icon_diamond, application.getString(R.string.gameDiamond), new long[]{7}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameOneKey), R.drawable.icon_one, application.getString(R.string.gameOne), new long[]{1}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameBookKey), R.drawable.icon_book, application.getString(R.string.gameBook), new long[]{50}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameGlassesKey), R.drawable.icon_glasses, application.getString(R.string.gameGlasses), new long[]{3600}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameCheckKey), R.drawable.icon_check, application.getString(R.string.gameCheck), new long[]{1}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameMedalKey), R.drawable.icon_medal, application.getString(R.string.gameMedal), new long[]{25}, new Date()));
        data.add(new Gamification(application.getString(R.string.gameCarKey), R.drawable.icon_car, application.getString(R.string.gameCar), new long[]{7}, new Date()));
        data.add(new Gamification(application.getString(R.string.gamePaperKey), R.drawable.icon_star, application.getString(R.string.gameStar), new long[]{25}, new Date()));

        for (Gamification gamification : data) {
            this.firestore.collection(ApplicationConstants.FIREBASE_COLLECTION_GAMIFICATION)
                    .document(gamification.getId())
                    .set(gamification);

        }
    }
}
