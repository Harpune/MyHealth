package de.dbis.myhealth.models;

import androidx.annotation.NonNull;


import java.util.Date;
import java.util.List;

public class Gamification {

    public String id;
    private String imageResource;
    private String description;
    private long goal;
    private long value;

    public Gamification() {

    }

    public Gamification(String id, String imageResource, String description, long goal, long value) {
        this.id = id;
        this.imageResource = imageResource;
        this.description = description;
        this.goal = goal;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getImageResource() {
        return imageResource;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goals) {
        this.goal = goals;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
