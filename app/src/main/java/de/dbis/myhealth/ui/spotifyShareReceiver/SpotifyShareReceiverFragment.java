package de.dbis.myhealth.ui.spotifyShareReceiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentSpotifyReceiverBinding;
import de.dbis.myhealth.models.SpotifyTrack;
import de.dbis.myhealth.ui.settings.SettingsViewModel;
import kaaes.spotify.webapi.android.SpotifyApi;

public class SpotifyShareReceiverFragment extends Fragment {
    private static final String TAG = "SpotifyShareReceiverFragment";

    private FragmentSpotifyReceiverBinding mSpotifyReceiverBinding;
    private SettingsViewModel mSettingsViewModel;
    private SharedPreferences mSharedPreferences;

    private LiveData<SpotifyApi> mSpotifyApiLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // bindings, storage and viewmodels
        this.mSpotifyReceiverBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_spotify_receiver, container, false);
        this.mSettingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // handle activitys fab
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        // get track and handle data
        String trackId = getArguments().getString(ApplicationConstants.TRACK_ID);
        if (trackId != null) {
            // Enabled
            boolean enabled = this.mSharedPreferences.getBoolean(getString(R.string.spotify_key), false);
            if (enabled) {
                this.subscribeForSpotifyApi(trackId);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Enable Spotify in your app")
                        .setMessage("You have to allow MyHealth to access Spotify before continuing")
                        .setCancelable(false)
                        .setPositiveButton("Enable", (dialogInterface, i) -> {
                            this.mSharedPreferences
                                    .edit()
                                    .putBoolean(getString(R.string.spotify_key), true)
                                    .apply();
                            this.subscribeForSpotifyApi(trackId);
                        })
                        .setNegativeButton("No", (dialogInterface, i) ->
                                Toast.makeText(requireContext(), "Sorry. Could't connect to Spotify without your permission.", Toast.LENGTH_LONG).show())
                        .show();
            }
        }

        return this.mSpotifyReceiverBinding.getRoot();
    }


    private void subscribeForSpotifyApi(String trackId) {
        this.mSpotifyApiLiveData = this.mSettingsViewModel.getSpotifyApi();
        this.mSpotifyApiLiveData.observe(requireActivity(), spotifyApi ->
                this.mSettingsViewModel.loadSpotifyTrack(trackId).observe(requireActivity(),
                        spotifyTrack -> {
                            Log.d(TAG, "SpotifyTrack" + spotifyTrack.getTrackId());
                            this.mSpotifyReceiverBinding.setSpotifyTrack(spotifyTrack);
                        }));
    }

    private final View.OnClickListener mFabClickListener = view -> {
        SpotifyTrack spotifyTrack = this.mSpotifyReceiverBinding.getSpotifyTrack();
        if (spotifyTrack != null) {
            this.mSettingsViewModel.save(spotifyTrack);
            Toast.makeText(getContext(), spotifyTrack.getTrack().name + " was successfully saved. You can now select it in Settings", Toast.LENGTH_LONG).show();
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        if (this.mSpotifyApiLiveData != null) {
            this.mSpotifyApiLiveData.removeObservers(requireActivity());
        }
    }
}