package de.dbis.myhealth.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ActivitySpotifyReceiverBinding;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class SpotifyShareReceiverActivity extends AppCompatActivity {
    private static final String TAG = "SpotifyShareReceiverActivity";

    private SettingsViewModel mSettingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_spotify_receiver);

        ActivitySpotifyReceiverBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_spotify_receiver);
        this.mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                this.handleSendText(intent);
            }
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // get track Id
            String[] lines = sharedText.split("\\r?\\n");
            Uri uri = Uri.parse(lines[1]);
            String trackId = uri.getLastPathSegment();

            String accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.access_token), null);
            if (accessToken != null) {
                this.mSettingsViewModel.getSpotifyApi().observe(this, spotifyApi -> {
                    this.mSettingsViewModel.loadSpotifyTrack(trackId).observe(this, spotifyTrack -> {
                        Toast.makeText(this, "yeah", Toast.LENGTH_SHORT).show();
                    });
                });

                this.mSettingsViewModel.connect(accessToken);

            } else {
                Toast.makeText(this, "No Access Token", Toast.LENGTH_SHORT).show();
            }
        }
    }
}