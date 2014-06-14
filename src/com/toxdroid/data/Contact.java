
package com.toxdroid.data;

import java.util.Locale;

import com.toxdroid.App;

import im.tox.jtoxcore.ToxException;
import im.tox.jtoxcore.ToxUserStatus;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * A person on the Tox network (that is not a local user). The user may be a friend; 
 * if they are not (i.e. they are a group chat member seen talking, but not added) 
 * friendn will be null.
 * 
 * 
 */
public class Contact implements im.tox.jtoxcore.ToxFriend, DatabaseRecord, User {
    private Long id;
    private long identity;
    private Integer friendn;
    private String name;
    private String address;
    private String added;
    
    private String statusMessage;
    private ToxUserStatus status;
    private boolean online;
    private boolean typing;
    
    public Contact() {
    }
    
    public Contact(int fn) {
        this.friendn = fn;
    }
    
    public Contact(Cursor c) {
        setupFromCursor(c);
    }
    
    @Override
    public ContentValues asValues() {
        ContentValues v = new ContentValues();
        v.put("_id", id);
        v.put("identity", identity);
        v.put("friendn", friendn);
        v.put("name", name);
        v.put("address", address);
        v.put("added", added);
        
        return v;
    }
    
    @Override
    public DatabaseRecord setupFromCursor(Cursor c) {
        int id = c.getColumnIndex("_id");
        int identity = c.getColumnIndex("identity");
        int friendn = c.getColumnIndex("friendn");
        int name = c.getColumnIndex("name");
        int address = c.getColumnIndex("address");
        int added = c.getColumnIndex("added");
        
        this.id = c.getLong(id);
        this.identity = c.getInt(identity);
        this.friendn = c.isNull(friendn) ? null : c.getInt(friendn);
        this.name = c.getString(name);
        this.address = c.getString(address);
        this.added = c.getString(added);
        
        return this;
    }
    
    @Override
    public void delete(App app) {
        try {
            app.getTox().deleteContact(this);
        } catch (ToxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Long getDatabaseId() {
        return id;
    }
    
    public void setDatabaseId(Long id) {
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setDateAdded(String dateAdded) {
        this.added = dateAdded;
    }
    
    public String getDateAdded() {
        return added;
    }

    @Override
    public int getFriendnumber() {
        return friendn;
    }

    @Override
    public ToxUserStatus getStatus() {
        return status;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public boolean isOnline() {
        return online;
    }
    
    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean isTyping() {
        return typing;
    }
    
    @Override
    public void setTyping(boolean typing) {
        this.typing = typing;
    }
    
    @Override
    public String getId() {
        return address;
    }

    @Override
    public void setId(String toxId) {
        this.address = toxId;
    }
    
    @Override
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public void setStatus(ToxUserStatus status) {
        this.status = status;
    }
    
    public long getIdentity() {
        return identity;
    }

    public void setIdentity(long identity) {
        this.identity = identity;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public String getTable() {
        return DatabaseHelper.TABLE_CONTACT;
    }
}