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

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {

    private UserViewModel mUserViewModel;
    private LiveData<FirebaseUser> mFirebaseUserLiveData;

    private final View.OnClickListener mFabClickListener = this::save;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        this.mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        FragmentUserBinding mFragmentUserBinding = FragmentUserBinding.inflate(inflater, container, false);
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);
        mFragmentUserBinding.setUserViewModel(this.mUserViewModel);
        mFragmentUserBinding.setLifecycleOwner(getViewLifecycleOwner());

        View root = mFragmentUserBinding.getRoot();

        // gender dropdown
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.gender, R.layout.item_gender);
        MaterialAutoCompleteTextView materialAutoCompleteTextView = root.findViewById(R.id.filled_exposed_dropdown);
        materialAutoCompleteTextView.setAdapter(arrayAdapter);

        this.mFirebaseUserLiveData = this.mUserViewModel.getFirebaseUser();
        mFirebaseUserLiveData.observe(getViewLifecycleOwner(), firebaseUser -> {
            String gender = this.mUserViewModel.getCurrentUser(firebaseUser.getUid()).getGender();
            materialAutoCompleteTextView.setText(gender, false);
        });

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
        this.hideKeyboardFrom(requireContext(), getView());
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mFirebaseUserLiveData.removeObservers(getViewLifecycleOwner());
    }
}