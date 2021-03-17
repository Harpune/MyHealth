package de.dbis.myhealth.ui.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseUser;
import com.preference.PowerPreference;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentUserBinding;
import de.dbis.myhealth.models.User;

public class UserFragment extends Fragment {

    // user relevant
    private UserViewModel mUserViewModel;
    private LiveData<User> mUserLiveData;
    private LiveData<FirebaseUser> mFirebaseUserLiveData;

    // Views
    private View root;

    // click listener on FAB
    private final View.OnClickListener mFabClickListener = this::save;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // get view model
        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // setup binding
        FragmentUserBinding mFragmentUserBinding = FragmentUserBinding.inflate(inflater, container, false);
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);
        mFragmentUserBinding.setUserViewModel(this.mUserViewModel);
        mFragmentUserBinding.setLifecycleOwner(getViewLifecycleOwner());

        // get root view
        this.root = mFragmentUserBinding.getRoot();

        // setup gender dropdown
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.gender, R.layout.item_gender);
        MaterialAutoCompleteTextView materialAutoCompleteTextView = root.findViewById(R.id.filled_exposed_dropdown);
        materialAutoCompleteTextView.setAdapter(arrayAdapter);

        this.mUserLiveData = this.mUserViewModel.getUser();
        this.mUserLiveData.observe(getViewLifecycleOwner(), user ->
                materialAutoCompleteTextView.setText(user.getGender(), false));

        return root;
    }

    private void save(View view) {
        this.mUserViewModel.save();
        Toast.makeText(requireContext(), "Your personal data has been saved successfully", Toast.LENGTH_LONG).show();
        this.hideKeyboardFrom(requireContext(), view);
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
        if (this.mFirebaseUserLiveData != null) {
            this.mFirebaseUserLiveData.removeObservers(getViewLifecycleOwner());
        }
        if (this.mUserLiveData != null) {
            this.mUserLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}