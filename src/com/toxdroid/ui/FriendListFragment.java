package com.toxdroid.ui;

import java.util.Observable;
import java.util.Observer;

import im.tox.jtoxcore.ToxUserStatus;
import im.tox.jtoxcore.callbacks.CallbackHandler;
import im.tox.jtoxcore.callbacks.OnConnectionStatusCallback;
import im.tox.jtoxcore.callbacks.OnNameChangeCallback;
import im.tox.jtoxcore.callbacks.OnStatusMessageCallback;
import im.tox.jtoxcore.callbacks.OnUserStatusCallback;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.activity.ChatActivity;
import com.toxdroid.task.AddFriendTask;
import com.toxdroid.tox.FriendList;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.CreateUserDialog.OnCreateFriendListener;
import com.toxdroid.ui.adapter.ToxFriendAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FriendListFragment extends Fragment {
    private ListView friendList;
    private ToxFriendAdapter adapter;
    private Listener listener = new Listener();
    
    /**
     * Boilerplate to update the friend list as soon as an update is found.
     */
    private class Listener implements Observer, OnConnectionStatusCallback<ToxFriend>,
        OnNameChangeCallback<ToxFriend>, OnStatusMessageCallback<ToxFriend>,
        OnUserStatusCallback<ToxFriend> {
        
        private Handler handler = new Handler(Looper.getMainLooper());
        private Runnable update = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        };

        @Override
        public void execute(ToxFriend friend, ToxUserStatus status) {
            handler.post(update);
        }

        @Override
        public void execute(ToxFriend friend, String something) {
            handler.post(update);
        }

        @Override
        public void execute(ToxFriend friend, boolean online) {
            handler.post(update);
        }

        @Override
        public void update(Observable friend, Object o) {
            update.run(); // This is already on the UI thread
        }
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list, container);
        
        App app = App.get(getActivity());
        friendList = (ListView) v.findViewById(R.id.friend_list);
        
        // Setup the list adapter to update properly when the friend list changes
        FriendList data = app.getTox().getFriendList();
        adapter = new ToxFriendAdapter(app, data.allUi());
        
        friendList.setAdapter(adapter);
        data.addObserver(listener);
        
        CallbackHandler<ToxFriend> callbacks = app.getTox().getCallbacks();
        callbacks.registerOnNameChangeCallback(listener);
        callbacks.registerOnConnectionStatusCallback(listener);
        callbacks.registerOnStatusMessageCallback(listener);
        callbacks.registerOnUserStatusCallback(listener);
        
        // Add an item listener to launch ChatActivity
        friendList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivity((ToxFriend) parent.getItemAtPosition(position));
            }
        });
        
        return v;
    }
    
    private void startChatActivity(ToxFriend friend) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatFragment.ARG_FRIEND, friend.getFriendnumber());
        startActivity(intent);
    }
}
