package de.dbis.myhealth.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.dbis.myhealth.ApplicationConstants;
import de.dbis.myhealth.R;
import de.dbis.myhealth.databinding.ItemHomeBinding;
import de.dbis.myhealth.models.Gamification;

import static de.dbis.myhealth.R.string.general_gamification_key;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private final static String TAG = "HomeAdapter";

    private final SharedPreferences mSharedPreferences;
    public List<Gamification> allGamifications;
    public List<Gamification> enabledGamifications;
    private Activity activity;

    public class HomeViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeBinding binding;
        private final ImageView imageView;

        public HomeViewHolder(@NonNull ItemHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageView = binding.getRoot().findViewById(R.id.imageView);

            binding.getRoot().findViewById(R.id.home_item_root_layout).setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(binding.getRoot().getContext())
                        .setTitle(binding.getRoot().getContext().getString(R.string.remove_gamification_title))
                        .setCancelable(false)
                        .setMessage(binding.getRoot().getContext().getString(R.string.remove_gamification_message))
                        .setPositiveButton(binding.getRoot().getContext().getString(R.string.remove), (dialogInterface, i) -> {
                            Gamification currentGamification = binding.getGamification();
                            Set<String> enabledGamifications = mSharedPreferences.getStringSet(activity.getString(general_gamification_key), new HashSet<>());
                            enabledGamifications.remove(currentGamification.getId());
                            mSharedPreferences.edit().putStringSet(activity.getString(general_gamification_key), enabledGamifications).apply();
                            removeAt(getAdapterPosition());
                        })
                        .setNegativeButton(binding.getRoot().getContext().getString(R.string.cancel), null)
                        .show();
                return false;
            });
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

        public void removeAt(int position) {
            enabledGamifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, enabledGamifications.size());

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

    public HomeAdapter(Activity activity) {
        this.activity = activity;
        this.mSharedPreferences = activity.getSharedPreferences(ApplicationConstants.PREFERENCES, Context.MODE_PRIVATE);
    }


    public void setData(List<Gamification> gamifications) {
        Set<String> enabledGamifications = this.mSharedPreferences.getStringSet(this.activity.getString(general_gamification_key), new HashSet<>());
        this.enabledGamifications = gamifications.stream().filter(gamification -> enabledGamifications.contains(gamification.getId())).collect(Collectors.toList());
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
        Gamification gamification = this.enabledGamifications.get(position);
        holder.bind(gamification);
    }

    @Override
    public int getItemCount() {
        return this.enabledGamifications != null ? this.enabledGamifications.size() : 0;
    }
}
