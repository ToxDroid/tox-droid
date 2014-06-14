
package com.toxdroid.ui.adapter;

import java.util.List;

import com.toxdroid.R;
import com.toxdroid.data.Contact;
import com.toxdroid.ui.UserCardFragment;
import com.toxdroid.util.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * A list adapter for ToxFriends.
 * 
 */
public class ToxFriendAdapter extends ArrayAdapter<Contact> {
    public ToxFriendAdapter(Context context, List<Contact> objects) {
        super(context, R.layout.user_card, objects);
    }
    
    public ToxFriendAdapter(Context context, Contact[] objects) {
        super(context, R.layout.user_card, objects);
    }
    
    public ToxFriendAdapter(Context context) {
        super(context, R.layout.user_card);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView != null && convertView.getId() == R.layout.user_card)
            v = convertView; // Reuse the supplied view if possible
        else
            v = Util.newUserCard(LayoutInflater.from(getContext()).inflate(R.layout.user_card, parent, false),
                    getItem(position));
        
        return v;
    }
}
