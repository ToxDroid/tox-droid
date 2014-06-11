
package com.toxdroid.activity;

import com.toxdroid.App;
import com.toxdroid.CheckedAsyncTask;
import com.toxdroid.R;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.CreateUserDialog;
import com.toxdroid.ui.FriendListFragment;
import com.toxdroid.ui.UserCardFragment;
import com.toxdroid.ui.CreateUserDialog.OnCreateFriendListener;

import im.tox.jtoxcore.ToxError;
import im.tox.jtoxcore.ToxException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * An activity which shows the friend list.
 * 
 */
public class FriendListActivity extends ActionBarActivity implements UserCardFragment.OnAttachListener {
    private static final String TAG = "FriendListActivity";
    private FriendListFragment friendList;
    private UserCardFragment identityCard;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_friend_list);
        
        friendList = (FriendListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_friend_list);
        
        // Add an item listener to launch ChatActivity
        friendList.getFriendList().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivity((ToxFriend) parent.getItemAtPosition(position));
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        App.get(this).getTox().save();
    }
    
    @Override
    public void onAttach(Fragment fragment) {
        // Setup the fragment before its view is created
        identityCard = (UserCardFragment) fragment;
        identityCard.setUser(App.get(this).getTox().getActiveIdentity());
        identityCard.setDarkTheme(true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friend_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_friend)
            showAddFriendDialog();
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showAddFriendDialog() {
        Bundle b = new Bundle();
        b.putInt(CreateUserDialog.ARG_USERTYPE, CreateUserDialog.CREATE_FRIEND);
        
        CreateUserDialog dialog = new CreateUserDialog();
        dialog.setArguments(b);
        dialog.setOnCreateFriendListener(new OnCreateFriendListener() {
            @Override
            public void call(DialogFragment dialog, String address, String message) {
                newAddFriendTask().execute(address, message); // TODO Don't close dialog until add success
            }
        });
        dialog.show(getSupportFragmentManager(), "add_friend_dialog");
    }
    
    private void startChatActivity(ToxFriend friend) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.ARG_FRIEND, friend.getFriendnumber());
        startActivity(intent);
    }
    
    private CheckedAsyncTask<Object, Void, ToxFriend> newAddFriendTask() {
        return new CheckedAsyncTask<Object, Void, ToxFriend>() {
            @Override
            public ToxFriend checkedDoInBackground(Object... params) throws Exception {
                String address = (String) params[0];
                String message = (String) params[1];
                
                return App.get(FriendListActivity.this).getTox().addFriend(address, message);
            }
            
            @Override
            protected void onSuccess(ToxFriend result) {
                if (result == null)
                    Toast.makeText(FriendListActivity.this, R.string.friend_already_added, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            protected void onFail(Exception e) {
                if (e instanceof ToxException) {
                    int message = getErrorMessageId(((ToxException) e).getError());
                    if (message != -1) {
                        Toast.makeText(FriendListActivity.this, message, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                super.onFail(e);
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
        };
    }
}
