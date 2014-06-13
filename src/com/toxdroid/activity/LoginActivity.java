
package com.toxdroid.activity;

import java.net.ConnectException;
import java.util.ArrayList;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.Identity;
import com.toxdroid.ui.CreateUserDialog;
import com.toxdroid.ui.CreateUserDialog.OnCreateIdentityListener;
import com.toxdroid.util.CheckedAsyncTask;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The main activity (first activity to start) which allows users to select an identity.
 * 
 * 
 */
public class LoginActivity extends FragmentActivity implements OnItemClickListener, OnClickListener {
    private ListView identities;
    private Button addIdentityBtn;
    private ArrayAdapter<Identity> adapter;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_login);
        
        adapter = new ArrayAdapter<Identity>(this, android.R.layout.simple_list_item_1, new ArrayList<Identity>(App
                .get(this).getIdentityManager().getIdentities()));
        
        identities = (ListView) findViewById(R.id.identities);
        identities.setAdapter(adapter);
        
        addIdentityBtn = (Button) findViewById(R.id.add_identity);
        
        addIdentityBtn.setOnClickListener(this);
        identities.setOnItemClickListener(this);
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
    
    @Override
    public void onClick(View v) {
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
    
    /**
     * Creates a new task which logs the user into the Tox network as the given identity.
     */
    private AsyncTask<Identity, Void, Void> newLoginTask() {
        return new CheckedAsyncTask<Identity, Void, Void>() {
            @Override
            public Void checkedDoInBackground(Identity... args) throws Exception {
                App.get(LoginActivity.this).getTox().loginAsIdentity(LoginActivity.this, args[0]);
                return null;
            }
            
            @Override
            protected void onSuccess(Void result) {
                startActivity(new Intent(LoginActivity.this, FriendListActivity.class));
            }
            
            @Override
            protected void onFailure(Exception e) {
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
}
