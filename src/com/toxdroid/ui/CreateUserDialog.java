
package com.toxdroid.ui;

import com.google.common.base.Preconditions;
import com.toxdroid.R;
import com.toxdroid.tox.ToxCore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A dialog which allows users to add a friend or create a new identity for themselves.
 * 
 * 
 */
public class CreateUserDialog extends DialogFragment {
    public static final String ARG_USERTYPE = "usertype";
    public static final int CREATE_LOCAL_IDENTITY = 1;
    public static final int CREATE_FRIEND = 2;
    private OnCreateIdentityListener identityListener;
    private OnCreateFriendListener friendListener;
    private EditText nameField;
    private EditText addressField;
    private EditText messageField;
    private int intent;
    
    public interface OnCreateIdentityListener {
        /**
         * Called when the user successfully enters the details for an identity in {@link CreateUserDialog}.
         * @param dialog the dialog
         * @param name the new identity's name
         */
        public void call(DialogFragment dialog, String name);
    }
    
    public interface OnCreateFriendListener {
        /**
         * Called when the user successfully enters the details for an identity in {@link CreateUserDialog}.
         * @param dialog the dialog
         * @param address the new friend's Tox address
         * @param message the new friend request message (may be null)
         */
        public void call(DialogFragment dialog, String address, String message);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // A few sanity checks...
        Preconditions.checkNotNull(getArguments());
        Preconditions.checkArgument(identityListener != null || friendListener != null);
        
        // Decide which type of user we are creating
        intent = getArguments().getInt(ARG_USERTYPE, -1);
        Preconditions.checkArgument(intent == CREATE_FRIEND || intent == CREATE_LOCAL_IDENTITY);
        
        // Create the custom input view
        LayoutInflater infl = getActivity().getLayoutInflater();
        View inputView = infl.inflate(intent == CREATE_FRIEND ? R.layout.fragment_create_friend
                : R.layout.fragment_create_identity, null);
        
        nameField = (EditText) inputView.findViewById(R.id.identity_add_name);
        addressField = (EditText) inputView.findViewById(R.id.user_add_public_key);
        messageField = (EditText) inputView.findViewById(R.id.user_add_message);
        
        if (addressField != null)
            addressField.setText("56A1ADE4B65B86BCD51CC73E2CD4E542179F47959FE3E0E21B4B0ACDADE51855D34D34D37CB5"); // TODO Remove
            
        return buildDialog(inputView);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Define the submit button listener. We set it here so we can control the dismissal
        final AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent == CREATE_FRIEND) {
                    String address = addressField.getText().toString();
                    String message = messageField.getText().toString();
                    
                    // Validate the address
                    if (address.length() != ToxCore.TOX_TOXID_LENGTH) {
                        Toast.makeText(CreateUserDialog.this.getActivity(), R.string.user_create_invalid_params,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    friendListener.call(CreateUserDialog.this, address, message);
                } else {
                    String name = nameField.getText().toString();
                    identityListener.call(CreateUserDialog.this, name);
                }
                
                dialog.dismiss();
            }
        });
    }
    
    private Dialog buildDialog(View inputView) {
        // Create the dialog + setup the confirm button to fire the listener
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(intent == CREATE_LOCAL_IDENTITY ? R.string.add_identity : R.string.add_friend);
        builder.setView(inputView);
        
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CreateUserDialog.this.dismiss(); // Cancel the dialog
            }
        });
        
        return builder.create();
    }
    
    public void setOnCreateIdentityListener(OnCreateIdentityListener identityListener) {
        this.identityListener = identityListener;
    }
    
    public void setOnCreateFriendListener(OnCreateFriendListener friendListener) {
        this.friendListener = friendListener;
    }
}
