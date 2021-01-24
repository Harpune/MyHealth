package de.dbis.myhealth.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.dbis.myhealth.R;

public class SpotifyLoginDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Login to Spotify")
                .setMessage("You are not logged in you Spotify app.")
                .setPositiveButton("Login", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, i) ->
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit()
                                .putBoolean(getString(R.string.spotify_key), false)
                                .apply())
                .create();
    }
}
