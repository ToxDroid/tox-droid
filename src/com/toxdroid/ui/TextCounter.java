
package com.toxdroid.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * A TextWatcher that counts the length of an input and displays it in a label.
 * 
 *
 */
public class TextCounter implements TextWatcher {
    private String format;
    private TextView counter;
    
    /**
     * Creates a new instance.
     * @param counter the label to adjust
     */
    public TextCounter(TextView counter) {
        this.counter = counter;
    }
    
    /**
     * Creates a new instance with a format string.
     * @param format a format string where %s is replaced by the count
     * @param counter the label to adjust
     */
    public TextCounter(String format, TextView counter) {
        this.format = format;
        this.counter = counter;
    }
    
    protected void update(int len) {
        String count = Integer.toString(len);
        counter.setText(format == null ? count : String.format(format, count));
    }
    
    @Override
    public void afterTextChanged(Editable s) {
        update(s.length());
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
