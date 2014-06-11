
package com.toxdroid.ui;

import com.toxdroid.R;
import com.toxdroid.data.Message;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment which can be used to chat textually with someone.
 * 
 */
public class TextChatFragment extends Fragment implements TextWatcher {
    private ListView messageHistory;
    private TextView messageInput;
    private Button submitBtn;
    private ArrayAdapter<Message> adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text_chat, container);
        
        messageHistory = (ListView) v.findViewById(R.id.messages);
        messageInput = (TextView) v.findViewById(R.id.message_input);
        submitBtn = (Button) v.findViewById(R.id.send_message);
        adapter = new ArrayAdapter<Message>(this.getActivity(), android.R.layout.simple_list_item_1);
        
        messageHistory.setAdapter(adapter);
        
        submitBtn.addTextChangedListener(this);
        
        return v;
    }
    
    public ListView getMessageHistory() {
        return messageHistory;
    }
    
    public TextView getMessageInput() {
        return messageInput;
    }
    
    public Button getSubmitBtn() {
        return submitBtn;
    }
    
    public ArrayAdapter<Message> getMessageListAdapter() {
        return adapter;
    }
    
    /*
     * Generic callbacks
     */
    @Override
    public void afterTextChanged(Editable s) {
        submitBtn.setEnabled(s.length() > 0);
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing
    }
}
