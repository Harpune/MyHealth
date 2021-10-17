package de.dbis.myhealth.models;

import android.view.View;

import com.google.android.material.slider.Slider;
import com.stfalcon.chatkit.messages.MessageHolders;

import de.dbis.myhealth.R;

public class OutcomingTHIMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<ChatMessage> {

    private Slider slider;

    public OutcomingTHIMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.slider = itemView.findViewById(R.id.slider_0_100_chat);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            this.slider.resetPivot();
//        } else {
//            this.slider.setValue(0);
//        }
    }

    @Override
    public void onBind(ChatMessage message) {
        super.onBind(message);
    }
}
