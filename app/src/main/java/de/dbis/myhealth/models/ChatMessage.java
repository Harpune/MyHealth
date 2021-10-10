package de.dbis.myhealth.models;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class ChatMessage<T> implements IMessage, MessageContentType {

    private String id;
    private String text;
    private ChatUser user;
    private Date date;

    public ChatMessage(String id, String text, ChatUser user, Date date) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.date = date;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public Date getCreatedAt() {
        return this.date;
    }
}
