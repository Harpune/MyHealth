package de.dbis.myhealth.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.ui.settings.SettingsViewModel;

public class SpotifyReceiverActivity extends MainActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_receiver);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        this.mSettingsViewModel.isConnected().observe(this, connected -> {

        });

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String trackId = this.handleSendText(intent);

            }
        }
    }

    private String handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            String[] lines = sharedText.split("\\r?\\n");
            Uri uri = Uri.parse(lines[1]);
            return uri.getLastPathSegment();
        }
        return null;
    }
}