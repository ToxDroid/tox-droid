
package com.toxdroid.data;

import java.util.Locale;

import com.toxdroid.App;
import com.toxdroid.tox.ToxCore;

import im.tox.jtoxcore.ToxException;
import im.tox.jtoxcore.ToxUserStatus;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * A local user account.
 * 
 * 
 */
public class Identity implements DatabaseRecord, User {
    private ToxCore tox; // Needed to get self status, online status etc. Will be null if not logged in
    private Long id;
    private String name;
    private String address;
    private String added;
    
    public Identity() {
    }
    
    public Identity(Cursor c) {
        setupFromCursor(c);
    }
    
    @Override
    public ContentValues asValues() {
        ContentValues v = new ContentValues();
        v.put("_id", id);
        v.put("name", name);
        v.put("address", address);
        v.put("added", added);
        
        return v;
    }
    
    @Override
    public DatabaseRecord setupFromCursor(Cursor c) {
        int id = c.getColumnIndex("_id");
        int name = c.getColumnIndex("name");
        int address = c.getColumnIndex("address");
        int added = c.getColumnIndex("added");
        
        this.id = c.getLong(id);
        this.name = c.getString(name);
        this.address = c.getString(address);
        this.added = c.getString(added);
        
        return this;
    }
    
    @Override
    public void delete(App app) {
        app.getIdentityManager().deleteIdentity(id);
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getStatusMessage() {
        return ""; // TODO Find a way to grab this
    }
    
    @Override
    public ToxUserStatus getStatus() {
        try {
            return tox != null ? tox.getSelfUserStatus() : ToxUserStatus.TOX_USERSTATUS_NONE;
        } catch (ToxException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean isOnline() {
        try {
            return tox.isConnected();
        } catch (ToxException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getDateAdded() {
        return added;
    }
    
    public void setDateAdded(String added) {
        this.added = added;
    }
    
    public void setTox(ToxCore tox) {
        this.tox = tox;
    }
    
    @Override
    public String getTable() {
        return DatabaseHelper.TABLE_IDENTITY;
    }
    
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
    
    @Override
    public String toString() {
        return name;
    }
}
