package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemHomeBinding;
import de.dbis.myhealth.models.Gamification;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private final static String TAG = "HomeAdapter";

    private SharedPreferences mSharedPreferences;
    private List<Gamification> gamifications;
    private Map<Gamification, Boolean> gamificationsSelection;
    private boolean[] mGamificationSelections;
    private final Activity activity;
    private final LifecycleOwner lifecycleOwner;

    public static class HomeViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeBinding binding;
        private final ImageView imageView;
        private final ProgressBar progressBar;
        private final TextView statText;

        public HomeViewHolder(@NonNull ItemHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageView = binding.getRoot().findViewById(R.id.imageView);
            this.progressBar = binding.getRoot().findViewById(R.id.progress);
            this.statText = binding.getRoot().findViewById(R.id.stat_text);
        }

        public void bind(Gamification gamification) {
            this.binding.setGamification(gamification);
            this.binding.executePendingBindings();

            // set image
            byte[] decodedString = Base64.decode(gamification.getImageResource(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            this.imageView.setImageBitmap(decodedByte);

            if (gamification.getGoal() <= gamification.getValue()) {
                setUnlocked(this.imageView);
            } else {
                setLocked(this.imageView);
            }
        }

        private void setLocked(ImageView v) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
            v.setColorFilter(cf);
            v.setImageAlpha(128);
        }

        private void setUnlocked(ImageView v) {
            v.setColorFilter(null);
            v.setImageAlpha(255);
        }

    }

    public HomeAdapter(Activity activity, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        this.mSharedPreferences = activity.getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);

    }

    public void setData(List<Gamification> gamifications) {
        Set<String> enabledGamifications = this.mSharedPreferences.getStringSet(this.activity.getString(R.string.general_gamification_key), new HashSet<>());
        this.gamifications = gamifications.stream().filter(gamification -> enabledGamifications.contains(gamification.getId())).collect(Collectors.toList());
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
