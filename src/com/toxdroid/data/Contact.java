
package com.toxdroid.data;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * A person on the Tox network (that is not a local user). The user may be a friend; if they are not (i.e. they are a group chat member seen
 * talking, but not added) friendn will be null.
 * 
 * 
 */
public class Contact implements DatabaseRecord {
    private Long id;
    private Integer friendn;
    private String name;
    
    public Contact() {
    }
    
    public Contact(Cursor c) {
        setupFromCursor(c);
    }
    
    @Override
    public ContentValues asValues() {
        ContentValues v = new ContentValues();
        v.put("_id", id);
        v.put("friendn", friendn);
        v.put("name", name);
        
        return v;
    }
    
    @Override
    public DatabaseRecord setupFromCursor(Cursor c) {
        int id = c.getColumnIndex("_id");
        int friendn = c.getColumnIndex("friendn");
        int name = c.getColumnIndex("name");
        
        this.id = c.getLong(id);
        this.friendn = c.isNull(friendn) ? null : c.getInt(friendn);
        this.name = c.getString(name);
        
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getFriendn() {
        return friendn;
    }
    
    public void setFriendn(Integer friendn) {
        this.friendn = friendn;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isFriend() {
        return friendn != null;
    }
    
    @Override
    public String getTable() {
        return DatabaseHelper.TABLE_CONTACT;
    }
}