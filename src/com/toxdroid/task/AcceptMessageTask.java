
package com.toxdroid.task;

import java.util.concurrent.TimeUnit;

import com.toxdroid.App;
import com.toxdroid.data.Database;
import com.toxdroid.data.Message;
import com.toxdroid.data.Contact;
import com.toxdroid.ui.ChatFragment;
import com.toxdroid.util.CheckedAsyncTask;
import com.toxdroid.util.Util;

public class AcceptMessageTask extends CheckedAsyncTask<Object, Void, Message> {
    private ChatFragment fragment;
    
    public AcceptMessageTask(ChatFragment fragment) {
        this.fragment = fragment;
    }
    
    @Override
    public Message checkedDoInBackground(Object... params) throws Exception {
        Contact friend = (Contact) params[0];
        String body = (String) params[1];
        Database db = App.get(fragment.getActivity()).getDatabase();
        
        Message message = new Message();
        message.setBody(body);
        message.setChat(fragment.getChat().getId());
        message.setSender(friend.getDatabaseId());
        message.setPosition(fragment.nextMessagePos());
        message.setTimestamp(Util.timeAsISO8601());
        message.setId(db.insert(message).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
        
        // TODO Send read receipt
        return message;
    }
    
    @Override
    protected void onSuccess(Message result) {
        fragment.appendToHistory(result);
    }
}
