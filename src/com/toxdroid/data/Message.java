
package com.toxdroid.data;

import android.content.ContentValues;
import android.database.Cursor;

public class Message implements DatabaseRecord {
    private Long id;
    private long chat;
    private long sender;
    private String body;
    private String timestamp;
    private long pos;
    
    public Message() {
    }
    
    public Message(Cursor c) {
        setupFromCursor(c);
    }
    
    @Override
    public ContentValues asValues() {
        ContentValues v = new ContentValues();
        v.put("_id", id);
        v.put("chat", chat);
        v.put("sender", sender);
        v.put("body", body);
        v.put("timestamp", timestamp);
        v.put("pos", pos);
        
        return v;
    }
    
    @Override
    public DatabaseRecord setupFromCursor(Cursor c) {
        int id = c.getColumnIndex("_id");
        int chat = c.getColumnIndex("chat");
        int sender = c.getColumnIndex("sender");
        int body = c.getColumnIndex("body");
        int timestamp = c.getColumnIndex("timestamp");
        int pos = c.getColumnIndex("pos");
        
        this.id = c.getLong(id);
        this.chat = c.getLong(chat);
        this.sender = c.getLong(sender);
        this.body = c.getString(body);
        this.timestamp = c.getString(timestamp);
        this.pos = c.getLong(pos);
        
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public long getChat() {
        return chat;
    }
    
    public void setChat(long chat) {
        this.chat = chat;
    }
    
    public long getSender() {
        return sender;
    }
    
    public void setSender(long sender) {
        this.sender = sender;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getTimestampISO() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getPosition() {
        return pos;
    }
    
    public void setPosition(long pos) {
        this.pos = pos;
    }
    
    @Override
    public String getTable() {
        return DatabaseHelper.TABLE_MESSAGE;
    }
    
    @Override
    public String toString() {
        return body;
    }
}