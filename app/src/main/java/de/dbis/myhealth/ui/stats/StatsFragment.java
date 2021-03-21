package de.dbis.myhealth.ui.stats;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.FragmentHomeBinding;
import de.dbis.myhealth.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding mFragmentStatsBinding;
    private StatsViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        this.mFragmentStatsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stats, container, false);
        this.mFragmentStatsBinding.setStatsViewModel(this.mViewModel);
        this.mFragmentStatsBinding.setLifecycleOwner(getViewLifecycleOwner());

        return this.mFragmentStatsBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}