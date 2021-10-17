package de.dbis.myhealth.models;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import de.dbis.myhealth.R;

public class IncomingVoiceMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<ChatMessage> {

    private TextView textView;

    public IncomingVoiceMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        textView = itemView.findViewById(R.id.messageTime);
    }

    @Override
    public void onBind(ChatMessage message) {
        super.onBind(message);
        textView.setText(message.getText());
    }
}
