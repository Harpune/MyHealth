package de.dbis.myhealth.ui.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.Optional;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentSpotifyBinding;
import de.dbis.myhealth.models.SpotifyTrack;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class SpotifyFragment extends Fragment {
    private static final String TAG = SpotifyFragment.class.getSimpleName();

    private View mRoot;

    private FragmentSpotifyBinding mFragmentSpotifyBinding;
    private SpotifyViewModel mSpotifyViewModel;
    private SharedPreferences mSharedPreferences;
    private LiveData<SpotifyTrack> mSpotifyTrackLiveData;
    private LiveData<Bitmap> mTrackImageLiveData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // shared preferences
        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // view models
        this.mSpotifyViewModel = new ViewModelProvider(requireActivity()).get(SpotifyViewModel.class);

        // binding
        this.mFragmentSpotifyBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_spotify, container, false);
        this.mFragmentSpotifyBinding.setLifecycleOwner(getViewLifecycleOwner());
        this.mFragmentSpotifyBinding.setSpotifyViewModel(this.mSpotifyViewModel);

        this.mRoot = this.mFragmentSpotifyBinding.getRoot();
        this.mRoot.findViewById(R.id.no_track_set_button).setOnClickListener((view -> Navigation.findNavController(view).navigate(R.id.nav_settings_item)));

        return this.mRoot;
    }

    @Override
    public void onStart() {
        super.onStart();

        ShapeableImageView trackImageView = this.mRoot.findViewById(R.id.trackImageView);
        ImageView albumImage = this.mRoot.findViewById(R.id.albumImage);
        //this.mTrackImageLiveData = this.mSpotifyViewModel.getCurrentTrackImage();
        //this.mTrackImageLiveData.observe(getViewLifecycleOwner(), albumImage::setImageBitmap);
        LiveData<SpotifyTrack> mSpotifyTrackLiveData = this.mSpotifyViewModel.getCurrentSpotifyTrack();
        mSpotifyTrackLiveData.observe(getViewLifecycleOwner(), spotifyTrack -> {
            Image image = spotifyTrack.getImage();
            if (image != null) {
                Picasso.get().load(image.url).into(albumImage);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.mTrackImageLiveData != null) {
            this.mTrackImageLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}