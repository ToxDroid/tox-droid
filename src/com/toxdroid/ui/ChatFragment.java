package com.toxdroid.ui;

import java.util.concurrent.atomic.AtomicLong;

import im.tox.jtoxcore.callbacks.OnMessageCallback;

import com.google.common.base.Preconditions;
import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.Chat;
import com.toxdroid.data.Message;
import com.toxdroid.task.AcceptMessageTask;
import com.toxdroid.task.LoadChatTask;
import com.toxdroid.task.SendMessageTask;
import com.toxdroid.tox.ToxFriend;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatFragment extends Fragment implements OnMessageCallback<ToxFriend> {
    public static final String ARG_FRIEND = "friend";
    
    private ToxFriend friend;
    private Chat chat;
    private AtomicLong nextMessagePos = new AtomicLong(0);
    
    private ListView messageHistory;
    private TextView messageInput;
    private Button submit;
    private ArrayAdapter<Message> adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Preconditions.checkNotNull(friend, "Must set friend before onCreate");
    
        LoadChatTask task = new LoadChatTask(this);
        task.execute(friend.getUserId()); // TODO Execute on async executor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text_chat, container);
        messageHistory = (ListView) v.findViewById(R.id.messages);
        messageInput = (TextView) v.findViewById(R.id.message_input);
        submit = (Button) v.findViewById(R.id.send_message);
        
        // Set the adapter - this will update the message history when a message is sent / received
        adapter = new ArrayAdapter<Message>(getActivity(), R.layout.message);
        messageHistory.setAdapter(adapter);
        
        submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!friend.isOnline()) {
		            Toast.makeText(getActivity(), R.string.friend_not_online, Toast.LENGTH_SHORT).show();
		            return;
		        }
		        
		        String body = messageInput.getText().toString();
		        if (body.length() == 0)
		            return;
		        
		        new SendMessageTask(ChatFragment.this).execute();
		        messageInput.setText("");
			}
		});
        
        return v;
    }
    
    /**
     * Places a message the message history view.
     * @param message the message
     */
    public void appendToHistory(Message message) {
        adapter.add(message);
        nextMessagePos.incrementAndGet();
    }
    
    /**
     * Clears all messages from the history view.
     */
    public void clearHistory() {
        adapter.clear();
        nextMessagePos.set(0);
    }
    
    /**
     * Gets the next message position and increments the counter.
     * @return the next message position
     */
    public long nextMessagePos() {
        return nextMessagePos.getAndIncrement();
    }
    
    @Override
    public void execute(ToxFriend friend, String body) {
        new AcceptMessageTask(this).execute(friend, body);
    }
    
    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
    public Chat getChat() {
        return chat;
    }
    
    public void setFriend(ToxFriend friend) {
        this.friend = friend;
    }
    
    public void setFriend(int fn) {
        this.friend = App.get(getActivity()).getTox().getFriendList().getByFriendNumber(fn);
    }
}
