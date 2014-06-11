
package com.toxdroid.data;

import android.content.ContentValues;
import android.database.Cursor;

public class Chat implements DatabaseRecord {
    private Long id;
    private long me;
    private Long them;
    private Long groupn;
    
    public Chat() {
    }
    
    public Chat(Cursor c) {
        setupFromCursor(c);
    }
    
    @Override
    public ContentValues asValues() {
        ContentValues v = new ContentValues();
        v.put("_id", id);
        v.put("me", me);
        v.put("them", them);
        v.put("groupn", groupn);
        
        return v;
    }
    
    @Override
    public DatabaseRecord setupFromCursor(Cursor c) {
        int id = c.getColumnIndex("_id");
        int me = c.getColumnIndex("me");
        int them = c.getColumnIndex("them");
        int groupn = c.getColumnIndex("groupn");
        
        this.id = c.getLong(id);
        this.me = c.getLong(me);
        this.them = c.isNull(them) ? null : c.getLong(them);
        this.groupn = c.isNull(groupn) ? null : c.getLong(groupn);
        
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public long getMe() {
        return me;
    }
    
    public void setMe(long me) {
        this.me = me;
    }
    
    public Long getThem() {
        return them;
    }
    
    public void setThem(Long them) {
        this.them = them;
    }
    
    public Long getGroupn() {
        return groupn;
    }
    
    public void setGroupn(Long groupn) {
        this.groupn = groupn;
    }
    
    @Override
    public String getTable() {
        return DatabaseHelper.TABLE_CHAT;
    }
}
