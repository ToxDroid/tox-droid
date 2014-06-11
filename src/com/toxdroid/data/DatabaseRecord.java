
package com.toxdroid.data;

import android.content.ContentValues;
import android.database.Cursor;

public interface DatabaseRecord {
    public ContentValues asValues();
    
    public DatabaseRecord setupFromCursor(Cursor c);
    
    public String getTable();
}
