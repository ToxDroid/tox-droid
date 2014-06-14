
package com.toxdroid.activity;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.task.AddFriendTask;
import com.toxdroid.ui.ChatFragment;
import com.toxdroid.ui.CreateUserDialog;
import com.toxdroid.ui.CreateUserDialog.OnCreateFriendListener;
import com.toxdroid.ui.UserCardFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * A fragment which displays the user's home page. Its appearance varies depending
 * on the device screen size; a phone sized screen will display only the friends list,
 * while a tablet will also display the currently active chat. The user can select a
 * friend to chat with, at which point the chat is shown (in the second built-in fragment
 * or in a separate activity).
 * 
 */
public class HomeActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_home);
        
        UserCardFragment card = (UserCardFragment) getSupportFragmentManager().findFragmentById(R.id.user_card);
    	card.setUser(App.get(this).getTox().getActiveIdentity());
    }
    
    /*
     * Button callbacks
     */
    public void onAddFriendClick(View v) {
        Bundle b = new Bundle();
        b.putInt(CreateUserDialog.ARG_USERTYPE, CreateUserDialog.CREATE_FRIEND);
        
        CreateUserDialog dialog = new CreateUserDialog();
        dialog.setArguments(b);
        dialog.setOnCreateFriendListener(new OnCreateFriendListener() {
            @Override
            public void call(DialogFragment dialog, String address, String message) {
                new AddFriendTask(App.get(HomeActivity.this)).execute(address, message);
            }
        });
        dialog.show(this.getSupportFragmentManager(), "add_friend_dialog");
    }
    
    public void onSettingsClick(View v) {
        // TODO
    }
}
