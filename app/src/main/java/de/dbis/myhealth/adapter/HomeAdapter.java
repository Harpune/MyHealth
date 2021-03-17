package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.dbis.myhealth.databinding.ItemHomeBinding;
import de.dbis.myhealth.models.Gamification;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private List<Gamification> gamifications;
    private final Activity activity;
    private final LifecycleOwner lifecycleOwner;

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeBinding binding;

        public HomeViewHolder(@NonNull ItemHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Gamification gamification) {
            this.binding.setGamification(gamification);
            this.binding.executePendingBindings();
        }
    }

    public HomeAdapter(Activity activity, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
    }


    public void setData(List<Gamification> gamifications) {
        this.gamifications = gamifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemHomeBinding itemHomeBinding = ItemHomeBinding.inflate(layoutInflater, parent, false);

        return new HomeViewHolder(itemHomeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Gamification gamification = this.gamifications.get(position);
        holder.bind(gamification);
    }

    @Override
    public int getItemCount() {
        return this.gamifications != null ? this.gamifications.size() : 0;
    }
}
