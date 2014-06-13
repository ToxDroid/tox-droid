
package com.toxdroid.ui;

import com.toxdroid.R;
import com.toxdroid.data.User;
import com.toxdroid.util.Util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A user card which automatically updates itself every few seconds.
 * 
 */
public class UserCardFragment extends Fragment {
    private User user;
    private TextView name;
    private TextView status;
    private ImageView icon;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    public interface OnAttachedListener {
        public User onAttach(UserCardFragment fragment);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Update details every so often
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshUserDetails();
                handler.postDelayed(this, getUpdatePeriod());
            }
        };
        handler.post(updateRunnable);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_card, container);
        Util.newUserCard(v, user);
        
        return v;
    }
    
    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof OnAttachedListener) {
            ((OnAttachedListener) activity).onAttach(this);
        }
        super.onAttach(activity);
    }
    
    /**
     * Sets up all the child views to according to the user's details.
     */
    public void refreshUserDetails() {
    	View v = getView();
    	if (v != null)
    		Util.newUserCard(v, user);
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
    
    public int getUpdatePeriod() {
        return 2500;
    }
}
