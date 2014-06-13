
package com.toxdroid.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.Chat;
import com.toxdroid.data.Database;
import com.toxdroid.data.DatabaseHelper;
import com.toxdroid.data.Message;
import com.toxdroid.ui.ChatFragment;
import com.toxdroid.util.CheckedAsyncTask;
import com.toxdroid.util.Util;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class LoadChatTask extends CheckedAsyncTask<Long, Void, List<Message>> {
    private static final String TAG = "LoadChatTask";
    private ChatFragment fragment;
    private Database db;
    
    public LoadChatTask(ChatFragment fragment) {
        this.fragment = fragment;
        this.db = App.get(fragment.getActivity()).getDatabase();
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
            
            fragment.setChat(chat);
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        
        return messages;
    }
    
    @Override
    public void onSuccess(List<Message> messageHistory) {
        fragment.clearHistory();
        
        for (Message m : messageHistory)
            fragment.appendToHistory(m);
        
        Log.d(TAG, "Message history loaded successfully");
    }
    
    @Override
    public void onFailure(Exception e) {
        super.onFailure(e);
        Toast.makeText(fragment.getActivity(), R.string.activity_chat_load_fail, Toast.LENGTH_SHORT).show();
    }
};