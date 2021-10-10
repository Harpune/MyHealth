package de.dbis.myhealth.ui.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentUserBinding;
import de.dbis.myhealth.models.User;

public class UserFragment extends Fragment {

    // user relevant
    private UserViewModel mUserViewModel;
    private LiveData<User> mUserLiveData;


    private SharedPreferences mSharedPreferences;

    // Views
    private View root;
    private ImageView imageView;

    // click listener on FAB
    private final View.OnClickListener mFabClickListener = this::save;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // get view model
        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);


        this.mSharedPreferences = requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

        // setup binding
        FragmentUserBinding mFragmentUserBinding = FragmentUserBinding.inflate(inflater, container, false);
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);
        mFragmentUserBinding.setUserViewModel(this.mUserViewModel);
        mFragmentUserBinding.setLifecycleOwner(getViewLifecycleOwner());
        
        // get root view
        this.root = mFragmentUserBinding.getRoot();
        this.imageView = this.root.findViewById(R.id.user_image_view);

        // set current color theme
        String currentTheme = this.mSharedPreferences.getString(getString(R.string.theme_key), getString(R.string.green_theme_key));
        if (currentTheme.equalsIgnoreCase(getString(R.string.green_theme_key))) {
            ImageViewCompat.setImageTintList(this.imageView, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_900)));
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.blue_theme_key))) {
            ImageViewCompat.setImageTintList(this.imageView, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_900)));
        } else if (currentTheme.equalsIgnoreCase(getString(R.string.red_theme_key))) {
            ImageViewCompat.setImageTintList(this.imageView, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_900)));
        }

        // setup gender dropdown
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.gender, R.layout.item_gender);
        MaterialAutoCompleteTextView materialAutoCompleteTextView = root.findViewById(R.id.filled_exposed_dropdown);
        materialAutoCompleteTextView.setAdapter(arrayAdapter);

        this.mUserLiveData = this.mUserViewModel.getUser();
        this.mUserLiveData.observe(getViewLifecycleOwner(), user ->
                materialAutoCompleteTextView.setText(user.getGender(), false));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void save(View view) {
        this.mUserViewModel.save();
        this.hideKeyboardFrom(requireContext(), view);
        requireActivity().getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(getString(R.string.personal_information_added), true)
                .apply();
    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.hideKeyboardFrom(requireContext(), this.root);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.mUserLiveData != null) {
            this.mUserLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}