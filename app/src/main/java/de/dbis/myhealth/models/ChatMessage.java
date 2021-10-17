package de.dbis.myhealth.models;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class ChatMessage<T> implements IMessage, MessageContentType {

    private final String id;
    private final String text;
    private final ChatUser user;
    private final Date date;
    private final int position;
    private final Question question;

    public ChatMessage(String id, String text, ChatUser user, Date date, int position, Question question) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.date = date;
        this.position = position;
        this.question = question;
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

    public int getPosition() {
        return position;
    }

    public Question getQuestion() {
        return question;
    }

}
