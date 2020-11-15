package de.dbis.myhealth.util;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import de.dbis.myhealth.R;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class GoogleFitConnector {

    private final static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123;
    private final static String[] SLEEP_STAGE_NAMES = {"Unused", "Awake (during sleep)", "Sleep", "Out-of-bed", "Light sleep", "Deep sleep", "REM sleep"};

    private final Activity mActivity;
    private final SettingsViewModel mSettingsViewModel;

    public GoogleFitConnector(Activity activity) {
        this.mActivity = activity;
        this.mSettingsViewModel = new ViewModelProvider((ViewModelStoreOwner) activity).get(SettingsViewModel.class);
    }

    public LiveData<SessionsClient> getSessionClient(){
        return this.mSettingsViewModel.getSessionClient();
    }

    public void connect() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this.mActivity, this.mFitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, this.mFitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this.mActivity, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    account,
                    this.mFitnessOptions);
        } else {
            SessionsClient sessionsClient = Fitness.getSessionsClient(this.mActivity, account);
            this.mSettingsViewModel.setSessionsClient(sessionsClient);
        }
    }

    public void disconnect(){
        Fitness.getConfigClient(this.mActivity, GoogleSignIn.getAccountForExtension(this.mActivity, this.mFitnessOptions))
                .disableFit()
                .addOnSuccessListener(success -> Log.d("GoogleFitConnector", "Successfully disabled Fit"))
                .addOnFailureListener(error -> Log.d("GoogleFitConnector", "Could not disconnect", error));
    }

    public boolean isEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this.mActivity).getBoolean(this.mActivity.getString(R.string.google_fit_key), false);
    }

    public void getSleepingData() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        SessionReadRequest request = new SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                .includeSleepSessions()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeInterval(startSeconds, endSeconds, TimeUnit.MILLISECONDS)
                .build();

        this.mSettingsViewModel.getSessionClient().getValue().readSession(request)
                .addOnSuccessListener(response -> {
                    Log.d("Google Fitness", "Success");
                    response.getSessions().forEach(session -> {
                        long sessionStart = session.getStartTime(TimeUnit.MILLISECONDS);
                        long sessionEnd = session.getEndTime(TimeUnit.MILLISECONDS);
                        Log.d("GoogleFit", "Sleep between " + sessionStart + "  and " + sessionEnd);
                        response.getDataSet(session).forEach(dataSet -> {
                            dataSet.getDataPoints().forEach(dataPoint -> {
                                int sleepStageVal = dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt();
                                String sleepStage = SLEEP_STAGE_NAMES[sleepStageVal];
                                long segmentStart = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                                long segmentEnd = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
                                Log.d("GoogleFit", "Type " + sleepStage + " between " + segmentStart + " and " + segmentEnd);
                            });
                        });
                    });
                })
                .addOnFailureListener(error -> Log.d("GoogleFit", "OnFailure()", error));
    }

    private final FitnessOptions mFitnessOptions = FitnessOptions.builder()
            .accessSleepSessions(FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build();
}
