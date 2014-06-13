
package com.toxdroid.task;

import com.toxdroid.App;
import com.toxdroid.data.Message;
import com.toxdroid.tox.ToxCore;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.ChatFragment;
import com.toxdroid.util.CheckedAsyncTask;
import com.toxdroid.util.Util;

public class SendMessageTask extends CheckedAsyncTask<Object, Void, Message> {
    private ChatFragment fragment;
    
    public SendMessageTask(ChatFragment fragment) {
        this.fragment = fragment;
    }
    
    @Override
    public Message checkedDoInBackground(Object... params) throws Exception {
        ToxFriend friend = (ToxFriend) params[0];
        String body = (String) params[1];
        
        ToxCore tox = App.get(fragment.getActivity()).getTox();
        Message message = new Message();
        message.setChat(fragment.getChat().getId());
        message.setBody(body);
        message.setPosition(fragment.nextMessagePos());
        message.setSender(tox.getActiveIdentity().getId());
        message.setTimestamp(Util.timeAsISO8601());
        
        tox.sendMessage(friend, message);
        return message;
    }
    
    @Override
    protected void onSuccess(Message result) {
        fragment.appendToHistory(result);
    }
    
    @Override
    protected void onFailure(Exception e) {
        super.onFailure(e);
        // TODO Show send failure
        // TODO Mark as failed in database
    }
}
