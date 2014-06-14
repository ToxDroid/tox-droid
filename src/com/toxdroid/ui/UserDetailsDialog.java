
package com.toxdroid.ui;

import java.text.SimpleDateFormat;

import com.toxdroid.App;
import com.toxdroid.R;
import com.toxdroid.data.User;
import com.toxdroid.util.CheckedAsyncTask;
import com.toxdroid.util.Util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UserDetailsDialog extends DialogFragment {
    private User user;
    private OnDeleteUserListener deleteListener;
    
    public interface OnDeleteUserListener {
        /**
         * Called after the user is deleted.
         * @param fragment the dialog
         * @param user the user
         */
        public void call(UserDetailsDialog fragment, User user);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return buildDialog();
    }
    
    private Dialog buildDialog() {
        // Setup the content
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_user_details, null);
        TextView name = (TextView) v.findViewById(R.id.details_name);
        TextView address = (TextView) v.findViewById(R.id.details_address);
        TextView dateAdded = (TextView) v.findViewById(R.id.details_date_added);
        Button delete = (Button) v.findViewById(R.id.details_delete);
        Button back = (Button) v.findViewById(R.id.details_back);
        
        name.setText(getResources().getString(R.string.field_name, user.getName()));
        address.setText(getResources().getString(R.string.field_address, user.getAddress()));
        dateAdded.setText(getResources().getString(R.string.field_date_added, 
                Util.timeFromISO8601(user.getDateAdded(), SimpleDateFormat.SHORT, user.getLocale())));
        
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDeleteUserTask().execute(user); // TODO Require confirmation
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.user_details);
        
        return builder.create();
    }
    
    private CheckedAsyncTask<User, Void, User> newDeleteUserTask() {
        return new CheckedAsyncTask<User, Void, User>() {
            @Override
            public User checkedDoInBackground(User... params) throws Exception {
                params[0].delete(App.get(getActivity()));
                return params[0];
            }
            
            @Override
            protected void onSuccess(User user) {
                if (deleteListener != null)
                    deleteListener.call(UserDetailsDialog.this, user);
                
                Toast.makeText(getActivity(), R.string.user_deleted, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        };
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setOnDeleteUserListener(OnDeleteUserListener listener) {
        this.deleteListener = listener;
    }
    
    public OnDeleteUserListener getOnDeleteUserListener() {
        return deleteListener;
    }
}
