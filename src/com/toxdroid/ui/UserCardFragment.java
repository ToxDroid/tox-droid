
package com.toxdroid.ui;

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
    private boolean dark;
    
    public interface OnAttachListener {
        public void onAttach(Fragment fragment);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = createCard(getActivity(), inflater, container, user, dark);
        name = (TextView) v.findViewById(R.id.card_name);
        status = (TextView) v.findViewById(R.id.card_status_message);
        icon = (ImageView) v.findViewById(R.id.card_status_indicator);
        
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
    
    public static View createCard(Context ctx, View v, User user, boolean dark) {
        TextView name = (TextView) v.findViewById(R.id.card_name);
        TextView statusMessage = (TextView) v.findViewById(R.id.card_status_message);
        ImageView statusIcon = (ImageView) v.findViewById(R.id.card_status_indicator);
        
        if (user != null) {
            name.setText(user.getName());
            statusMessage.setText(user.getStatusMessage());
            
            // Set status icon
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
        
        /*
         * if (dark) { // Set special colors for identity card Resources r = ctx.getResources();
         * v.setBackgroundColor(r.getColor(R.color.bg_identity_card)); name.setTextColor(r.getColor(R.color.white));
         * statusMessage.setTextColor(r.getColor(R.color.white)); }
         */
        
        return v;
    }
    
    public static View createCard(Context ctx, LayoutInflater infl, ViewGroup parent, User user, boolean dark) {
        return createCard(ctx, infl.inflate(R.layout.fragment_user_card, parent), user, dark);
    }
    
    public void setDarkTheme(boolean dark) {
        this.dark = dark;
    }
    
    public boolean isDarkTheme() {
        return dark;
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
