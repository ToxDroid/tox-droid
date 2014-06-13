package com.toxdroid.task;

import im.tox.jtoxcore.ToxError;
import im.tox.jtoxcore.ToxException;
import android.widget.Toast;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.util.CheckedAsyncTask;

/**
 * A task which adds a friend.
 *
 *
 */
public class AddFriendTask extends CheckedAsyncTask<String, Void, ToxFriend> {
    private App app;
    
    public AddFriendTask(App app) {
        this.app = app;
    }
    
    @Override
    public ToxFriend checkedDoInBackground(String... params) throws Exception {
        return app.getTox().addFriend(params[0], params[1]);
    }
    
    @Override
    protected void onSuccess(ToxFriend result) {
        if (result == null)
            Toast.makeText(app, R.string.friend_already_added, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onFailure(Exception e) {
        if (e instanceof ToxException) {
            int message = getErrorMessageId(((ToxException) e).getError());
            if (message != -1) {
                Toast.makeText(app, message, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        super.onFailure(e);
    }
    
    private int getErrorMessageId(ToxError error) {
        switch (error) {
        case TOX_FAERR_ALREADYSENT:
            return R.string.friend_already_added;
        case TOX_FAERR_OWNKEY:
            return R.string.friend_own_key;
        case TOX_FAERR_SETNEWNOSPAM:
            return R.string.friend_new_nospam;
        case TOX_FAERR_BADCHECKSUM:
            return R.string.friend_bad_checksum;
        case TOX_FAERR_NOMESSAGE: // This can't happen
        default:
            return -1;
        }
    }
}
