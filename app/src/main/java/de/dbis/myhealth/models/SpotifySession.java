package de.dbis.myhealth.models;

public class SpotifySession {

    private String id;
    private long time;
    private int volume;

    public SpotifySession() {

    }

    public SpotifySession(String id, long time, int volume) {
        this.id = id;
        this.time = time;
        this.volume = volume;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
