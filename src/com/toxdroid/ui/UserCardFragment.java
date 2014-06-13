
package com.toxdroid.ui;

import com.google.common.base.Preconditions;
import com.toxdroid.R;
import com.toxdroid.data.User;

import im.tox.jtoxcore.ToxUserStatus;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A fragment which displays a person's user details.
 * 
 * 
 */
public class UserCardFragment extends Fragment {
    private User user;
    private TextView name;
    private TextView status;
    private ImageView icon;
    
    public interface OnAttachListener {
        public void onAttach(Fragment fragment);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_card, container);
        refreshUserDetails();
        
        return v;
    }
    
    @Override
    public void onAttach(Activity activity) {
        // Call the attachment listener if the activity implements it
        try {
            ((OnAttachListener) activity).onAttach(this);
        } catch (ClassCastException e) {
            // Ignore
        }
        
        super.onAttach(activity);
    }
    
    /**
     * Sets up all the child views to according to the user's details.
     */
    public void refreshUserDetails() {
        UserCardFragment.refresh(getView(), user);
    }
    
    /**
     * Sets up a user card for a given user.
     * @param view the view (type must be {@link R.layout.fragment_user_card})
     * @param user the user
     * @return the setup view
     */
    public static View setupCard(View view, User user) {
        Preconditions.checkArgument(view.getId() == R.layout.fragment_user_card);
        
        refresh(view, user);
        return view;
    }
    
    private static void refresh(View v, User user) {
        TextView name = (TextView) v.findViewById(R.id.card_name);
        TextView statusMessage = (TextView) v.findViewById(R.id.card_status_message);
        ImageView statusIcon = (ImageView) v.findViewById(R.id.card_status_indicator);
        
        // Set status icon
        name.setText(user.getName());
        statusMessage.setText(user.getStatusMessage());
        
        int icon;
        ToxUserStatus status = user.getStatus();
        if (user.isOnline()) {
            if (status == ToxUserStatus.TOX_USERSTATUS_AWAY) {
                icon = R.drawable.ic_away;
            } else if (status == ToxUserStatus.TOX_USERSTATUS_BUSY) {
                icon = R.drawable.ic_busy;
            } else {
                icon = R.drawable.ic_online;
            }
        } else {
            icon = R.drawable.ic_offline;
        }
        statusIcon.setImageResource(icon);
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public TextView getName() {
        return name;
    }
    
    public TextView getStatus() {
        return status;
    }
    
    public ImageView getIcon() {
        return icon;
    }
}
