
package com.toxdroid.tox;

import com.toxdroid.data.User;

import im.tox.jtoxcore.ToxUserStatus;

public class ToxFriend implements im.tox.jtoxcore.ToxFriend, User {
    public static final int NAME_LENGTH_MIN = 3;
    private long userId;
    private int friendNo;
    private String id;
    private String name;
    private String statusMessage;
    private ToxUserStatus status;
    private boolean online;
    private boolean typing;
    
    /**
     * Creates a new friend.
     * @param friendNo the friend number
     */
    public ToxFriend(int friendNo) {
        this.friendNo = friendNo;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
    
    @Override
    public ToxUserStatus getStatus() {
        return status;
    }
    
    @Override
    public boolean isOnline() {
        return online;
    }
    
    @Override
    public int getFriendnumber() {
        return friendNo;
    }
    
    @Override
    public boolean isTyping() {
        return typing;
    }
    
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    @Override
    public void setStatus(ToxUserStatus status) {
        this.status = status;
    }
    
    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    @Override
    public void setTyping(boolean typing) {
        this.typing = typing;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    @Override
    public String toString() {
        return "ToxFriend [userId=" + userId + ", friendNo=" + friendNo + ", id=" + id + ", name=" + name
                + ", statusMessage=" + statusMessage + ", status=" + status + ", online=" + online + ", typing="
                + typing + "]";
    }
}
