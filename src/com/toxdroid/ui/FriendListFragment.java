
package com.toxdroid.ui;

import im.tox.jtoxcore.ToxUserStatus;
import im.tox.jtoxcore.callbacks.CallbackHandler;
import im.tox.jtoxcore.callbacks.OnConnectionStatusCallback;
import im.tox.jtoxcore.callbacks.OnNameChangeCallback;
import im.tox.jtoxcore.callbacks.OnStatusMessageCallback;
import im.tox.jtoxcore.callbacks.OnUserStatusCallback;

import java.util.Observable;
import java.util.Observer;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.tox.FriendList;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.adapter.ToxFriendAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A fragment which displays a list of friends.
 * 
 */
public class FriendListFragment extends Fragment {
    private ListView friendsList;
    private Listener listener = new Listener();
    private ArrayAdapter<ToxFriend> adapter;
    
    /**
     * Boilerplate to ensure the adapter updates whenever its backing array is changed.
     */
    private class Listener implements Observer, OnNameChangeCallback<ToxFriend>, OnConnectionStatusCallback<ToxFriend>,
            OnStatusMessageCallback<ToxFriend>, OnUserStatusCallback<ToxFriend> {
        private Handler uiHandler = new Handler(Looper.getMainLooper());
        private Runnable notify = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        };
        
        @Override
        public void update(Observable observable, Object data) {
            notify.run(); // This will already be running on the UI thread
        }
        
        @Override
        public void execute(ToxFriend friend, ToxUserStatus status) {
            uiHandler.post(notify);
        }
        
        @Override
        public void execute(ToxFriend friend, boolean online) {
            uiHandler.post(notify);
        }
        
        @Override
        public void execute(ToxFriend friend, String something) {
            uiHandler.post(notify);
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        App app = App.get(getActivity());
        
        View v = inflater.inflate(R.layout.fragment_friend_list, container);
        friendsList = (ListView) v.findViewById(R.id.friends_list);
        
        // Setup the list adapter to update properly when the friend list changes
        FriendList data = app.getTox().getFriendList();
        adapter = new ToxFriendAdapter(this.getActivity(), data.allUi());
        
        friendsList.setAdapter(adapter);
        data.addObserver(listener);
        
        // Additionally notify the adapter when a friend's data changes
        CallbackHandler<ToxFriend> callbacks = app.getTox().getCallbacks();
        callbacks.registerOnNameChangeCallback(listener);
        callbacks.registerOnConnectionStatusCallback(listener);
        callbacks.registerOnStatusMessageCallback(listener);
        callbacks.registerOnUserStatusCallback(listener);
        
        return v;
    }
    
    public ListView getFriendList() {
        return friendsList;
    }
}
