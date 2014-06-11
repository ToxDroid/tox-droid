
package com.toxdroid.activity;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.Chat;
import com.toxdroid.task.AcceptMessageTask;
import com.toxdroid.task.LoadChatTask;
import com.toxdroid.task.SendMessageTask;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.TextChatFragment;

import im.tox.jtoxcore.callbacks.OnMessageCallback;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The chat activity which enables 1-1 messaging.
 * 
 * 
 */
public class ChatActivity extends ActionBarActivity implements OnMessageCallback<ToxFriend> {
    public static final String ARG_FRIEND = "friend";
    private ToxFriend friend;
    private Chat chat;
    private TextChatFragment textChat;
    private boolean ready;
    private long nextMessagePosition;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_chat);
        
        App app = App.get(this);
        
        textChat = (TextChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_text_chat);
        
        // Grab the friend we are to chat with
        int fn = getIntent().getIntExtra(ARG_FRIEND, -1);
        friend = App.get(this).getTox().getFriendList().getByFriendNumber(fn);
        
        if (friend == null) {
            throw new IllegalArgumentException("Friend number in extra field 'friend' invalid");
        }
        
        Log.d("ChatActivity", "Starting chat with " + friend);
        
        // Friend found - start the load task
        LoadChatTask loadTask = new LoadChatTask(this, App.get(this).getDatabase());
        loadTask.execute(app.getTox().getActiveIdentity().getId(), friend.getUserId()); // TODO Execute on async executor
        
        setup();
    }
    
    /**
     * Setup the view for the given friend.
     */
    protected void setup() {
        final TextView input = textChat.getMessageInput();
        Button submit = textChat.getSubmitBtn();
        
        input.setText("");
        submit.setOnClickListener(new OnClickListener() { // Message send listener
            @Override
            public void onClick(View v) {
                if (!friend.isOnline()) {
                    Toast.makeText(ChatActivity.this, R.string.friend_not_online, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String body = input.getText().toString();
                if (!ready || body.length() == 0)
                    return;
                
                SendMessageTask task = new SendMessageTask(ChatActivity.this, friend, body);
                task.execute();
                input.setText("");
            }
        });
        
        setTitle(friend.getName());
    }
    
    @Override
    public void execute(ToxFriend friend, String body) {
        new AcceptMessageTask(ChatActivity.this, friend, body).execute();
    }
    
    /**
     * The friend this chat was started for.
     * @return the friend
     */
    public ToxFriend getFriend() {
        return friend;
    }
    
    public TextChatFragment getFragment() {
        return textChat;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    public synchronized void setNextMessagePosition(long pos) {
        this.nextMessagePosition = pos;
    }
    
    public synchronized long getNextMessagePosition() {
        return nextMessagePosition;
    }
    
    public synchronized long getAndIncrementNextMessagePosition() {
        return nextMessagePosition++;
    }
    
    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
    public Chat getChat() {
        return chat;
    }
}
