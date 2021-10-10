package de.dbis.myhealth.models;

import com.stfalcon.chatkit.commons.models.IUser;


public class ChatUser implements IUser {
    private String id;
    private String name;
    private String avatar;

    public ChatUser(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getAvatar() {
        return this.avatar;
    }
}
