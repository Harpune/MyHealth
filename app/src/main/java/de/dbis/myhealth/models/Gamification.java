package de.dbis.myhealth.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class Gamification {

    @NonNull
    public String id;
    private int imageResource;
    private String description;
    private long[] goals;
    private Date fulfilled;

    public Gamification(@NotNull String id, int imageResource, String description, long[] goals, Date fulfilled) {
        this.id = id;
        this.imageResource = imageResource;
        this.description = description;
        this.goals = goals;
        this.fulfilled = fulfilled;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long[] getGoals() {
        return goals;
    }

    public void setGoals(long[] goals) {
        this.goals = goals;
    }

    public Date getFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(Date fulfilled) {
        this.fulfilled = fulfilled;
    }
}
