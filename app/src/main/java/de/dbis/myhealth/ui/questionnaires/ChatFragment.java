package de.dbis.myhealth.ui.questionnaires;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import de.dbis.myhealth.MainActivity;
import de.dbis.myhealth.R;
import de.dbis.myhealth.models.ChatMessage;
import de.dbis.myhealth.models.ChatUser;
import de.dbis.myhealth.models.OutcomingTHIMessageViewHolder;
import de.dbis.myhealth.models.Questionnaire;
import de.dbis.myhealth.models.QuestionnaireSetting;

public class ChatFragment extends Fragment implements MessageHolders.ContentChecker<ChatMessage> {

    // TODO add Question to the Holder
    private static final byte CONTENT_TYPE_QUESTIONNAIRE = 1;

    private QuestionnairesViewModel mQuestionnairesViewModel;

    private LiveData<Questionnaire> mQuestionnaireLiveData;
    private LiveData<QuestionnaireSetting> mQuestionnaireSettingLiveData;

    private Questionnaire mQuestionnaire;
    private QuestionnaireSetting mQuestionnaireSetting;

    // views
    private View root;
    private MessagesList mMessagesList;
    private FloatingActionButton fab;


    private final View.OnClickListener mFabClickListener = this::startQuestionnaire;

    private final ChatUser mMyHealthChatUser = new ChatUser("MyHealth", "MyHealth", "");
    private final ChatUser mChatUser = new ChatUser("user", "user", "");


    private List<ChatMessage> chat;

    private MessagesListAdapter<ChatMessage> mMessagesListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mQuestionnairesViewModel = new ViewModelProvider(requireActivity()).get(QuestionnairesViewModel.class);
        this.mQuestionnaireLiveData = this.mQuestionnairesViewModel.getSelectedQuestionnaire();
        this.mQuestionnaireSettingLiveData = this.mQuestionnairesViewModel.getQuestionnaireSetting();
    }

    public static <T> ArrayList<T> merge(Collection<T> a, Collection<T> b) {
        Iterator<T> itA = a.iterator();
        Iterator<T> itB = b.iterator();
        ArrayList<T> result = new ArrayList<T>();

        while (itA.hasNext() || itB.hasNext()) {
            if (itA.hasNext()) result.add(itA.next());
            if (itB.hasNext()) result.add(itB.next());
        }

        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // views
        this.root = inflater.inflate(R.layout.fragment_chat, container, false);
        this.mMessagesList = this.root.findViewById(R.id.messagesList);

        // set fab action in activity
        ((MainActivity) requireActivity()).setFabClickListener(this.mFabClickListener);

        this.mQuestionnaire = this.mQuestionnaireLiveData.getValue();
        this.mQuestionnaireSetting = this.mQuestionnaireSettingLiveData.getValue();

        if (this.mQuestionnaire.getId().equalsIgnoreCase("THI")) {
            this.setupTHI();
            this.initTHIAdapter();
        } else {
            this.setupTFI();
            this.initTFIAdapter();
        }

        this.mMessagesListAdapter.addToStart(this.chat.remove(0), true);
        this.mMessagesListAdapter.addToStart(this.chat.remove(0), true);

        return this.root;
    }

    private void setupTHI() {
        String[] thi = getActivity().getResources().getStringArray(R.array.thi_survey_questions);
        List<ChatMessage> thiQuestions = Arrays.stream(thi).map(question -> new ChatMessage(UUID.randomUUID().toString(), question, mMyHealthChatUser, new Date())).collect(Collectors.toList());
        List<ChatMessage> thiAnswers = Arrays.stream(thi).map(question -> new ChatMessage(UUID.randomUUID().toString(), "test", mChatUser, new Date())).collect(Collectors.toList());
        this.chat = merge(thiQuestions, thiAnswers);
    }

    private void initTHIAdapter() {
        MessageHolders messageHolders = new MessageHolders()
                .setOutcomingTextHolder(OutcomingTHIMessageViewHolder.class)
                .setOutcomingTextLayout(R.layout.item_outcoming_thi);

        ImageLoader mImageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
        this.mMessagesListAdapter = new MessagesListAdapter<>(this.mChatUser.getId(), messageHolders, mImageLoader);
        this.mMessagesList.setAdapter(this.mMessagesListAdapter);
    }

    private void setupTFI() {
        String[] tfi = getActivity().getResources().getStringArray(R.array.tfi_survey_questions);
        List<ChatMessage> tfiQuestions = Arrays.stream(tfi).map(question -> new ChatMessage(UUID.randomUUID().toString(), question, mMyHealthChatUser, new Date())).collect(Collectors.toList());
        List<ChatMessage> tfiAnswers = Arrays.stream(tfi).map(question -> new ChatMessage(UUID.randomUUID().toString(), "test", mChatUser, new Date())).collect(Collectors.toList());
        this.chat = merge(tfiQuestions, tfiAnswers);
    }

    private void initTFIAdapter() {
        MessageHolders messageHolders = new MessageHolders()
                .setOutcomingTextHolder(OutcomingTFIMessageViewHolder.class)
                .setOutcomingTextLayout(R.layout.item_outcoming_tfi);

        ImageLoader mImageLoader = (imageView, url, payload) -> Picasso.get().load(url).into(imageView);
        this.mMessagesListAdapter = new MessagesListAdapter<>(this.mChatUser.getId(), messageHolders, mImageLoader);
        this.mMessagesList.setAdapter(this.mMessagesListAdapter);
    }

    private void startQuestionnaire(View view) {
        this.fab = (FloatingActionButton) view;
        startQuestionnaire();
    }

    public void startQuestionnaire() {
        if (this.chat.isEmpty()) {
            // update fab
            this.fab.setImageResource(R.drawable.ic_baseline_check_24);

            // show dialog
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Done")
                    .setMessage("Are you done!")
                    .setNegativeButton("No", (dialog, which) -> {
                        Toast.makeText(requireContext(), "Alright then", Toast.LENGTH_LONG).show();
                    })
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(requireContext(), "FUCK YOU", Toast.LENGTH_LONG).show();
                    })
                    .show();
        } else {
            this.mMessagesListAdapter.addToStart(this.chat.remove(0), true);
            this.mMessagesListAdapter.addToStart(this.chat.remove(0), true);
        }
    }

    @Override
    public boolean hasContentFor(ChatMessage message, byte type) {
        return true;
    }

    static class OutcomingTHIMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<ChatMessage> {

        private Slider slider;

        public OutcomingTHIMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            this.slider = itemView.findViewById(R.id.slider_0_100_chat);
        }

        @Override
        public void onBind(ChatMessage message) {
            super.onBind(message);
            this.slider.setValue(0);
        }
    }

    static class OutcomingTFIMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<ChatMessage> {

        private RadioGroup radioGroup;

        public OutcomingTFIMessageViewHolder(View itemView, Object payload) {
            super(itemView, payload);
            this.radioGroup = itemView.findViewById(R.id.group_yes_no_sometimes_chat);
            this.radioGroup.clearCheck();
        }

        @Override
        public void onBind(ChatMessage message) {
            super.onBind(message);
            this.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            });
        }
    }
}