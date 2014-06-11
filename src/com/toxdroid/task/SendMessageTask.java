
package com.toxdroid.task;

import java.util.concurrent.TimeUnit;

import com.toxdroid.App;
import com.toxdroid.CheckedAsyncTask;
import com.toxdroid.Util;
import com.toxdroid.activity.ChatActivity;
import com.toxdroid.data.Database;
import com.toxdroid.data.Message;
import com.toxdroid.tox.ToxFriend;

import android.widget.ListView;

public class SendMessageTask extends CheckedAsyncTask<Void, Void, Message> {
    private ChatActivity activity;
    private ToxFriend friend;
    private String body;
    
    public SendMessageTask(ChatActivity activity, ToxFriend friend, String body) {
        this.activity = activity;
        this.friend = friend;
        this.body = body;
    }
    
    @Override
    public Message checkedDoInBackground(Void... params) throws Exception {
        App app = App.get(activity);
        Database db = app.getDatabase();
        
        Message message = new Message();
        message.setChat(activity.getChat().getId());
        message.setBody(body);
        message.setPosition(activity.getAndIncrementNextMessagePosition());
        message.setSender(app.getTox().getActiveIdentity().getId());
        message.setTimestamp(Util.timeAsISO8601());
        message.setId(db.insert(message).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
        
        app.getTox().sendMessage(friend, message);
        return message;
    }
    
    @Override
    protected void onSuccess(Message result) {
        activity.getFragment().getMessageListAdapter().add(result);
    }
    
    @Override
    protected void onFail(Exception e) {
        super.onFail(e);
        // TODO Show send failure
        // TODO Mark as failed in database
    }
    
}
