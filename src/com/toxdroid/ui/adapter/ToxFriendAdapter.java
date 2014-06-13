
package com.toxdroid.ui.adapter;

import java.util.List;

import com.toxdroid.R;
import com.toxdroid.tox.ToxFriend;
import com.toxdroid.ui.UserCardFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * A list adapter for ToxFriends.
 * 
 */
public class ToxFriendAdapter extends ArrayAdapter<ToxFriend> {
    public ToxFriendAdapter(Context context, List<ToxFriend> objects) {
        super(context, R.layout.fragment_user_card, objects);
    }
    
    public ToxFriendAdapter(Context context, ToxFriend[] objects) {
        super(context, R.layout.fragment_user_card, objects);
    }
    
    public ToxFriendAdapter(Context context) {
        super(context, R.layout.fragment_user_card);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView != null && convertView.getId() == R.layout.fragment_user_card) {
            v = convertView; // Reuse the supplied view if possible
        } else {
            LayoutInflater infl = LayoutInflater.from(getContext());
            v = infl.inflate(R.layout.fragment_user_card, parent, false);
        }
        
        return UserCardFragment.setupCard(v, getItem(position));
    }
}
