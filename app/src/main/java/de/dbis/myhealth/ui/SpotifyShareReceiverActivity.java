package de.dbis.myhealth.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ActivitySpotifyReceiverBinding;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.settings.SpotifyViewModel;
import kaaes.spotify.webapi.android.SpotifyApi;

import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_CLIENT_ID;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REDIRECT_URI;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_REQUEST_CODE;
import static de.dbis.myhealth.ApplicationConstants.SPOTIFY_TRACKS_KEY;

public class SpotifyShareReceiverActivity extends AppCompatActivity {
    private static final String TAG = "SpotifyShareReceiverActivity";

    private ActivitySpotifyReceiverBinding mSpotifyReceiverBinding;
    private SpotifyViewModel mSpotifyViewModel;
    private SharedPreferences mSharedPreferences;
    private Preference mPreference;

    private LiveData<SpotifyApi> mSpotifyApiLiveData;
    private LiveData<SpotifyTrack> mSpotifyTrackLiveData;

    private String mTrackId;
    private List<String> mTrackIds;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSpotifyReceiverBinding = DataBindingUtil.setContentView(this, R.layout.activity_spotify_receiver);
        this.mSpotifyViewModel = new ViewModelProvider(this).get(SpotifyViewModel.class);
        this.mSharedPreferences = getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
        this.mPreference = PowerPreference.getDefaultFile();
    }

    @Override
    protected void onStart() {
        super.onStart();// get track and handle data
        this.mSpotifyApiLiveData = this.mSpotifyViewModel.getSpotifyApi();

        // saved ids
        String[] trackIds = this.mPreference.getObject(SPOTIFY_TRACKS_KEY, String[].class, new String[0]);
        this.mTrackIds = new ArrayList<>(Arrays.asList(trackIds));

        // new id
        this.mTrackId = this.handleIntent(getIntent());

        // check if new id is not null
        if (this.mTrackId != null) {

            // check if already added
            if (this.mTrackIds.contains(this.mTrackId)) {
                Log.d(TAG, "Track-ID " + this.mTrackId + " already exists");
            }

            // request spotify track with spotify api (observed after being set in connectToApiOrAuth())
            this.mSpotifyApiLiveData.observe(this, spotifyApi -> {
                this.mSpotifyTrackLiveData = this.mSpotifyViewModel.loadSpotifyTrack(this.mTrackId);
                this.mSpotifyTrackLiveData.observe(this, spotifyTrack -> this.mSpotifyReceiverBinding.setSpotifyTrack(spotifyTrack));
            });

            // check if spotify is enabled
            boolean enabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
            if (enabled) {
                // Connect is enabled. Disconnect otherwise.
                this.connectToApiOrAuth();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Enable Spotify in your app")
                        .setMessage("You have to allow MyHealth to access Spotify before continuing")
                        .setCancelable(false)
                        .setPositiveButton("Enable", (dialogInterface, i) -> {
                            this.mSharedPreferences
                                    .edit()
                                    .putBoolean(getString(R.string.spotify_key), true)
                                    .apply();
                            this.connectToApiOrAuth();
                        })
                        .setNegativeButton("No", (dialogInterface, i) ->
                        {
                            Toast.makeText(this, "Sorry. Could't connect to Spotify without your permission.", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .show();
            }
        } else {
            Toast.makeText(this, "Could not get necessary data to include Spotify track into the app.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Connect to the Spotify API or authenticate if no accesstoken is set.
     */
    public void connectToApiOrAuth() {
        String accessToken = this.mSharedPreferences.getString(getString(R.string.access_token), null);
        if (accessToken != null) {
            SpotifyApi spotifyApi = this.mSpotifyViewModel.getSpotifyApi().getValue();

            if (spotifyApi == null) {
                spotifyApi = new SpotifyApi();
                spotifyApi.setAccessToken(accessToken);
            }
            this.mSpotifyViewModel.setSpotifyApi(spotifyApi);
        } else {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);
            builder.setScopes(new String[]{"streaming", "app-remote-control"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mSpotifyApiLiveData != null) {
            this.mSpotifyApiLiveData.removeObservers(this);
        }

        if (this.mSpotifyTrackLiveData != null) {
            this.mSpotifyTrackLiveData.removeObservers(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Spotify
            if (requestCode == SPOTIFY_REQUEST_CODE) {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
                switch (response.getType()) {
                    case TOKEN:
                        this.mSharedPreferences.edit().putString(getString(R.string.access_token), response.getAccessToken()).apply();
                        this.connectToApiOrAuth();
                        break;
                    case ERROR:
                        Log.d(TAG, "Spotify-onActivityResult: " + response.getError());
                        break;
                    default:
                        Log.d(TAG, "Spotify-onActivityResult: No TOKEN nor ERROR");

                }
            }
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveSpotifyTrack(View view) {
        SpotifyTrack spotifyTrack = this.mSpotifyReceiverBinding.getSpotifyTrack();
        if (spotifyTrack != null) {
            // add track to track ids
            this.mTrackIds.add(this.mTrackId);

            // save in preference
            this.mPreference.setObject(SPOTIFY_TRACKS_KEY, this.mTrackIds);
            this.mPreference.setObject(this.mTrackId, spotifyTrack);

            Toast.makeText(this, "'" + spotifyTrack.getTrack().name + "' by '" + spotifyTrack.getTrack().artists.get(0).name + "' was added to MyHealth.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String handleIntent(Intent intent) {
        // Check if from other app
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    // get track Id
                    String[] lines = sharedText.split("\\r?\\n");
                    Uri uri = Uri.parse(lines[1]);
                    return uri.getLastPathSegment();
                }
            }
        }
        return null;
    }
}