package de.dbis.myhealth.models;

import androidx.annotation.NonNull;


import java.util.Date;
import java.util.List;

public class Gamification {

    public String id;
    private String imageResource;
    private String description;
    private List<Long> goals;

    public Gamification() {

    }

    public Gamification(String id, String imageResource, String description, List<Long> goals) {
        this.id = id;
        this.imageResource = imageResource;
        this.description = description;
        this.goals = goals;
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

    public List<Long> getGoals() {
        return goals;
    }

    public void setGoals(List<Long> goals) {
        this.goals = goals;
    }
}
