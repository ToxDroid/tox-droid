
package com.toxdroid.activity;

import java.net.ConnectException;
import java.util.ArrayList;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.Identity;
import com.toxdroid.data.User;
import com.toxdroid.ui.CreateUserDialog;
import com.toxdroid.ui.UserDetailsDialog;
import com.toxdroid.ui.CreateUserDialog.OnCreateIdentityListener;
import com.toxdroid.util.CheckedAsyncTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * The main activity (first activity to start) which allows users to manage identities and login
 * to the Tox network. 
 * 
 */
public class LoginActivity extends FragmentActivity implements OnItemClickListener, OnItemLongClickListener {
    private ListView identities;
    private ArrayAdapter<Identity> adapter;
    private View progress;
    private View controls;
    private ObjectAnimator animation;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.fragment_login);
        
        progress = findViewById(R.id.load_indicator);
        controls = findViewById(R.id.login_controls);
        
        adapter = new ArrayAdapter<Identity>(this, android.R.layout.simple_list_item_1, new ArrayList<Identity>(App
                .get(this).getIdentityManager().getIdentities()));
        
        identities = (ListView) findViewById(R.id.identities);
        identities.setAdapter(adapter);
        
        identities.setOnItemClickListener(this);
        identities.setOnItemLongClickListener(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        stopAnimation();
    }
    
    public void startAnimation() {
        stopAnimation();
        animation = createAnimation(controls, "alpha", 1.0f, 0.0f, 1000, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                controls.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            }
        });
        animation.start();
    }
    
    public void stopAnimation() {
        if (animation == null)
            return;
        
        animation.cancel();
        ViewHelper.setAlpha(controls, 1.0f);
        
        progress.setVisibility(View.GONE);
        controls.setVisibility(View.VISIBLE);
        animation = null;
    }
    
    private ObjectAnimator createAnimation(View v, String property, float start, float end, int duration, AnimatorListener listener) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, property, start, end);
        animation.setDuration(duration);
        animation.addListener(listener);
        return animation;
    }
    
    /**
     * Creates a new task which logs the user into the Tox network as the given identity.
     */
    private AsyncTask<Identity, Void, Void> newLoginTask() {
        return new CheckedAsyncTask<Identity, Void, Void>() {
            @Override
            protected void onPreExecute() {
                startAnimation();
            }
            
            @Override
            public Void checkedDoInBackground(Identity... args) throws Exception {
                App.get(LoginActivity.this).getTox().loginAsIdentity(LoginActivity.this, args[0]);
                return null;
            }
            
            @Override
            protected void onSuccess(Void result) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
            
            @Override
            protected void onFailure(Exception e) {
                stopAnimation();
                if (e instanceof ConnectException) {
                    Toast.makeText(LoginActivity.this, R.string.internet_unavailable, Toast.LENGTH_SHORT).show();
                } else {
                    super.onFailure(e);
                    Toast.makeText(LoginActivity.this, R.string.login_failed_generic, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
    
    /**
     * Creates a new tasks which creates an identity.
     */
    private AsyncTask<String, Void, Identity> newCreateIdentityTask() {
        return new CheckedAsyncTask<String, Void, Identity>() {
            @Override
            public Identity checkedDoInBackground(String... args) throws Exception {
                return App.get(LoginActivity.this).getIdentityManager().createIdentity(args[0]);
            }
            
            @Override
            protected void onSuccess(Identity result) {
                adapter.add(result);
            }
            
            @Override
            protected void onFailure(Exception e) {
                super.onFailure(e);
                Toast.makeText(LoginActivity.this, R.string.create_identity_failed, Toast.LENGTH_SHORT).show();
            }
        };
    }
    
    /*
     * Listener callbacks
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // On list item tapped, login
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        newLoginTask().execute(identity);
    }
    
    public void onAddIdentityClick(View v) {
        // On create new identity button pressed, show dialog
        Bundle b = new Bundle();
        b.putInt(CreateUserDialog.ARG_USERTYPE, CreateUserDialog.CREATE_LOCAL_IDENTITY);
        
        CreateUserDialog dialog = new CreateUserDialog();
        dialog.setArguments(b);
        dialog.setOnCreateIdentityListener(new OnCreateIdentityListener() {
            
            @Override
            public void call(DialogFragment dialog, String name) {
                newCreateIdentityTask().execute(name);
            }
        });
        dialog.show(getSupportFragmentManager(), "add_friend_dialog");
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long id) {
        UserDetailsDialog dialog = new UserDetailsDialog();
        dialog.setUser((Identity) adapter.getItemAtPosition(pos));
        dialog.setOnDeleteUserListener(new UserDetailsDialog.OnDeleteUserListener() {
            @Override
            public void call(UserDetailsDialog fragment, User user) {
                LoginActivity.this.adapter.remove((Identity) user);
            }
        });
        dialog.show(getSupportFragmentManager(), "identity_details_dialog");
        
        return true;
    }
}
