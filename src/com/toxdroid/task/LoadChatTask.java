
package com.toxdroid.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.toxdroid.CheckedAsyncTask;
import com.toxdroid.R;
import com.toxdroid.Util;
import com.toxdroid.activity.ChatActivity;
import com.toxdroid.data.Chat;
import com.toxdroid.data.Database;
import com.toxdroid.data.DatabaseHelper;
import com.toxdroid.data.Message;
import com.toxdroid.ui.TextChatFragment;

import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class LoadChatTask extends CheckedAsyncTask<Long, Void, List<Message>> {
    private static final String TAG = "LoadChatTask";
    private Database db;
    private ChatActivity activity;
    
    public LoadChatTask(ChatActivity activity, Database db) {
        this.activity = activity;
        this.db = db;
    }
    
    @Override
    public List<Message> checkedDoInBackground(Long... params) throws Exception {
        long me = params[0];
        long them = params[1];
        
        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = null;
        try {
            // Find the chat record for this conversation
            Chat chat;
            cursor = db.select(DatabaseHelper.TABLE_CHAT, "me = ? AND them = ?", Util.asStringArray(me, them), null,
                    null);
            if (!cursor.moveToNext()) {
                // No chat found; we must create it
                chat = new Chat();
                chat.setMe(me);
                chat.setThem(them);
                chat.setId(db.insert(chat).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
            } else {
                chat = new Chat(cursor);
            }
            cursor.close();
            
            // Grab all the associated messages
            cursor = db.select(DatabaseHelper.TABLE_MESSAGE, "chat = ?", Util.asStringArray(chat.getId()), null, null);
            while (cursor.moveToNext())
                messages.add(new Message(cursor));
            
            activity.setChat(chat);
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        
        return messages;
    }
    
    @Override
    public void onSuccess(List<Message> messageHistory) {
        TextChatFragment frag = activity.getFragment();
        
        ArrayAdapter<Message> adapter = frag.getMessageListAdapter();
        adapter.clear();
        
        Log.v(TAG, Arrays.toString(messageHistory.toArray()));
        
        for (Message m : messageHistory)
            adapter.add(m);
        
        frag.getSubmitBtn().setEnabled(true);
        
        if (!messageHistory.isEmpty())
            activity.setNextMessagePosition(messageHistory.get(messageHistory.size() - 1).getPosition());
        else
            activity.setNextMessagePosition(0);
        
        activity.setReady(true);
        
        Log.d(TAG, "Message history loaded successfully");
    }
    
    @Override
    public void onFail(Exception e) {
        super.onFail(e);
        
        Toast.makeText(activity, R.string.activity_chat_load_fail, Toast.LENGTH_SHORT).show();
        activity.finish();
    }
};