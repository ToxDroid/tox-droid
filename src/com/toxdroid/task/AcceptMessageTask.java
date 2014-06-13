
package com.toxdroid.task;

import java.util.concurrent.TimeUnit;

import com.toxdroid.App;
import com.toxdroid.activity.ChatActivity;
import com.toxdroid.data.Database;
import com.toxdroid.data.Message;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.util.CheckedAsyncTask;
import com.toxdroid.util.Util;

import android.widget.ListView;

public class AcceptMessageTask extends CheckedAsyncTask<Void, Void, Message> {
    private ChatActivity activity;
    private ToxFriend friend;
    private String body;
    
    public AcceptMessageTask(ChatActivity activity, ToxFriend friend, String body) {
        this.activity = activity;
        this.friend = friend;
        this.body = body;
    }
    
    @Override
    public Message checkedDoInBackground(Void... params) throws Exception {
        App app = App.get(activity);
        Database db = app.getDatabase();
        
        Message message = new Message();
        message.setBody(body);
        message.setChat(activity.getChat().getId());
        message.setSender(friend.getUserId());
        message.setPosition(activity.getAndIncrementNextMessagePosition());
        message.setTimestamp(Util.timeAsISO8601());
        message.setId(db.insert(message).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
        
        // TODO Send read receipt
        return message;
    }
    
    @Override
    protected void onSuccess(Message result) {
        activity.getFragment().getMessageListAdapter().add(result);
    }
}
