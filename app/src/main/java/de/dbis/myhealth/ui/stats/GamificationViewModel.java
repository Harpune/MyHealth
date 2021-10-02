package de.dbis.myhealth.ui.stats;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.Gamification;
import de.dbis.myhealth.models.HealthSession;
import de.dbis.myhealth.models.SpotifySession;

public class GamificationViewModel extends AndroidViewModel {
    private final String TAG = getClass().getSimpleName();

    private SharedPreferences mSharedPreferences;

    private final MutableLiveData<List<Gamification>> mGamifications;

    public GamificationViewModel(@NonNull Application application) {
        super(application);

        this.mSharedPreferences = getApplication().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        this.mGamifications = new MutableLiveData<>();

        this.generateGamifications();
//        generateGamificationToFirestore();
    }

    public void setGamifications(List<Gamification> gamifications) {
        this.mGamifications.setValue(gamifications);
    }

    public LiveData<List<Gamification>> getGamifications() {
        return this.mGamifications;
    }

    public void matchWithHealthSessions(List<HealthSession> healthSessions) {
        List<Gamification> gamifications = getGamifications().getValue();
        if (gamifications != null) {
            long totalAmountSubmissions = this.getTotalAmountSubmissions(healthSessions);
            long tfiAmountSubmissions = this.getTotalTfiSubmissions(healthSessions);
            long thiAmountSubmissions = this.getTotalThiSubmissions(healthSessions);
            long amountConsecutiveDates = this.getAmountConsecutiveDates(healthSessions);
            long amountConsecutiveSubmissions = this.getAmountConsecutiveSubmissions(healthSessions);
            long totalAmountMusicListened = this.getAmountMusicListened(healthSessions);
            long appInstalled = 1L;

            List<Gamification> updatedGamification = new ArrayList<>();

            gamifications.forEach(gamification -> {
                if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameBookKey))) {
                    gamification.setValue(totalAmountSubmissions);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameCarKey))) {
                    gamification.setValue(amountConsecutiveDates);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameCheckKey))) {
                    gamification.setValue(this.mSharedPreferences.getBoolean(getApplication().getString(R.string.personal_information_added), false) ? 1 : 0);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameDiamondKey))) {
                    gamification.setValue(amountConsecutiveSubmissions);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameGlassesKey))) {
                    gamification.setValue(totalAmountMusicListened);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameMedalKey))) {
                    gamification.setValue(tfiAmountSubmissions);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameOneKey))) {
                    gamification.setValue(appInstalled);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gamePaperKey))) {
                    gamification.setValue(totalAmountSubmissions);
                } else if (gamification.getId().equalsIgnoreCase(getApplication().getResources().getString(R.string.gameStarKey))) {
                    gamification.setValue(thiAmountSubmissions);
                }

                updatedGamification.add(gamification);
            });

            updatedGamification.sort((a, b) -> Long.compare(b.getValue() / b.getGoal(), a.getValue() / a.getGoal()));

            this.setGamifications(updatedGamification);
        }
    }

    /**
     * - Submit 10 questionnaires (Paper)
     * - Submit 50 questionnaires (Book)
     */
    private long getTotalAmountSubmissions(List<HealthSession> healthSessions) {
        return healthSessions.stream()
                .mapToInt(healthSession -> healthSession.getQuestionnaireResults().size())
                .sum();
    }

    /**
     * Fill out 25 TFI questionnaires (Medal)
     */
    private long getTotalTfiSubmissions(List<HealthSession> healthSessions) {
        return healthSessions
                .stream()
                .mapToInt(healthSession -> (int) healthSession.getQuestionnaireResults()
                        .stream()
                        .filter(questionnaireResult -> questionnaireResult.getQuestionnaireId().equalsIgnoreCase("TFI"))
                        .count())
                .sum();
    }

    /**
     * Fill out 25 THI questionnaires (Star)
     */
    private long getTotalThiSubmissions(List<HealthSession> healthSessions) {
        return healthSessions.stream()
                .mapToInt(healthSession -> (int) healthSession.getQuestionnaireResults().stream()
                        .filter(questionnaireResult -> questionnaireResult.getQuestionnaireId().equalsIgnoreCase("THI"))
                        .count())
                .sum();
    }

    /**
     * Open the app 7 days in a row (Car)
     */
    private long getAmountConsecutiveDates(List<HealthSession> healthSessions) {
        List<LocalDate> appOpenedDates = healthSessions.stream()
                .map(HealthSession::getDate)
                .sorted(Date::compareTo)
                .map(StatsFragment::convertToLocalDate)
                .distinct()
                .limit(7)
                .collect(Collectors.toList());

        int consecutiveDatesAmount = 0;
        LocalDate localDate = LocalDate.now();
        for (int i = 0; i < appOpenedDates.size(); i++) {
            if (appOpenedDates.contains(localDate)) {
                consecutiveDatesAmount++;
                localDate.minusDays(1L);
            } else {
                break;
            }
        }

        return consecutiveDatesAmount;
    }

    /**
     * Listen to music for 1 hour (Glasses)
     */
    private long getAmountConsecutiveSubmissions(List<HealthSession> healthSessions) {
        List<LocalDate> localDates = healthSessions.stream()
                .filter(healthSession -> healthSession.getQuestionnaireResults().size() > 0)
                .map(HealthSession::getDate)
                .sorted(Date::compareTo)
                .map(StatsFragment::convertToLocalDate)
                .distinct()
                .limit(7)
                .collect(Collectors.toList());

        int consecutiveDatesAmount = 0;
        LocalDate localDate = LocalDate.now();
        for (int i = 0; i < localDates.size(); i++) {
            if (localDates.contains(localDate)) {
                consecutiveDatesAmount++;
                localDate.minusDays(1L);
            } else {
                break;
            }
        }

        return consecutiveDatesAmount;

    }

    /**
     * Listen to music for 1 hour (Glasses)
     */
    private long getAmountMusicListened(List<HealthSession> healthSessions) {
        return healthSessions.stream()
                .mapToLong(healthSession -> {
                    Map<String, SpotifySession> spotifySession = healthSession.getSpotifySession();
                    if (spotifySession != null) {
                        return spotifySession.values().stream().map(SpotifySession::getTime).mapToLong(Long::longValue).sum();
                    }
                    return 0;
                })
                .sum();
    }

    public void generateGamifications() {
        List<Gamification> gamifications = new ArrayList<>();
        String[] ids = getApplication().getResources().getStringArray(R.array.gamification_keys);
        String[] images = getApplication().getResources().getStringArray(R.array.gamification_images);
        String[] descriptions = getApplication().getResources().getStringArray(R.array.gamification_descriptions);
        long[] goals = this.generateGoals(ids);

        for (int i = 0; i < descriptions.length; i++) {
            Gamification gamification = new Gamification(ids[i], images[i], descriptions[i], goals[i], 0L);
            gamifications.add(gamification);
        }

        this.setGamifications(gamifications);
    }

    private long[] generateGoals(String[] ids) {
        long[] goals = new long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gamePaperKey))) {
                goals[i] = 10L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameDiamondKey))) {
                goals[i] = 7L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameOneKey))) {
                goals[i] = 1L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameBookKey))) {
                goals[i] = 50L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameGlassesKey))) {
                goals[i] = 3600000L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameCheckKey))) {
                goals[i] = 1L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameMedalKey))) {
                goals[i] = 25L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameCarKey))) {
                goals[i] = 7L;
            } else if (ids[i].equalsIgnoreCase(getApplication().getResources().getString(R.string.gameStarKey))) {
                goals[i] = 25L;
            }
        }

        return goals;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public void generateGamificationToFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(ApplicationConstants.FIREBASE_COLLECTION_GAMIFICATION + "_" + Locale.getDefault().getLanguage());

        String[] ids = getApplication().getResources().getStringArray(R.array.gamification_keys);
        String[] images = getApplication().getResources().getStringArray(R.array.gamification_images);
        String[] descriptions = getApplication().getResources().getStringArray(R.array.gamification_descriptions);
        long[] goals = this.generateGoals(ids);

        for (int i = 0; i < descriptions.length; i++) {
            Gamification gamification = new Gamification(ids[i], images[i], descriptions[i], goals[i], 0L);
            firestore.collection(ApplicationConstants.FIREBASE_COLLECTION_GAMIFICATION + "_" + Locale.getDefault().getLanguage()).document(gamification.getId()).set(gamification);
        }
    }

}
