
package com.toxdroid.activity;

import com.toxdroid.R;
import com.toxdroid.ui.ChatFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * An activity which contains only a ChatFragment. This activity is used
 * for screens which cannot support the dual panel view in the home activity.
 * 
 * 
 */
public class ChatActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.fragment_text_chat);
    }
    
    @Override
    public void onAttachFragment(Fragment fragment) {
        ((ChatFragment) fragment).setFriend(getIntent().getIntExtra(ChatFragment.ARG_FRIEND, -1));
    }
}
