
package com.toxdroid.tox;

import im.tox.jtoxcore.FriendExistsException;
import im.tox.jtoxcore.JTox;
import im.tox.jtoxcore.ToxException;
import im.tox.jtoxcore.ToxUserStatus;
import im.tox.jtoxcore.callbacks.CallbackHandler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Preconditions;
import com.toxdroid.App;
import com.toxdroid.Util;
import com.toxdroid.data.Contact;
import com.toxdroid.data.Database;
import com.toxdroid.data.Identity;
import com.toxdroid.data.Message;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Helper class for interacting with the Tox library.
 * 
 */
public class ToxCore {
    public static final int TOX_TOXID_LENGTH = 76;
    public static final int TOX_PUBKEY_LENGTH = 64;
    private static final String TAG = "ToxCore";
    private App app;
    private Database db;
    private JTox<ToxFriend> jtox;
    private FriendList friends;
    private CallbackHandler<ToxFriend> callbacks;
    private NodeDirectory directory = new NodeDirectory();
    private Identity activeIdentity;
    private boolean serviceRunning;
    
    static {
        try {
            System.loadLibrary("sodium");
            System.loadLibrary("toxcore");
        } catch (UnsatisfiedLinkError e) {
            Log.e("ToxCore", "Unable to load tox libraries", e);
        }
    }
    
    /**
     * Creates a new ToxCore instance.
     * @throws ToxException if the Tox library indicates a problem
     */
    public ToxCore(App app) throws ToxException {
        this.app = app;
        this.db = app.getDatabase();
        this.friends = new FriendList();
        this.callbacks = new CallbackHandler<ToxFriend>(friends);
    }
    
    /**
     * Attempts to log into the Tox network as the given user.
     * @param ctx the context
     * @param identity the identity
     * @throws ToxException if the Tox library indicates a problem
     * @throws IOException if the identity's data could not be loaded
     * @throws ConnectException if there is no internet connection
     */
    public void loginAsIdentity(Context ctx, Identity identity) throws ToxException, IOException, ConnectException {
        if (identity == activeIdentity)
            return;
        
        if (!Util.isInternetConnected(ctx))
            throw new ConnectException("Internet is not connected");
        
        if (serviceRunning) {
            Log.i(TAG, "Logging " + identity + " out");
            save();
            stopService(ctx); // Log off from an existing session
        }
        
        Log.d(TAG, "Logging in as " + identity);
        this.activeIdentity = identity;
        
        byte[] data;
        try {
            data = app.getIdentityManager().loadIdentityData(identity);
            Log.i(TAG, identity + " has existing data = " + (data != null));
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        
        friends.clear();
        
        if (data == null)
            // This will only happen for new identities which have not yet been used
            jtox = new JTox<ToxFriend>(friends, callbacks);
        else
            // Load last DHT, friend list, key details etc...
            jtox = new JTox<ToxFriend>(data, friends, callbacks);
        
        Log.i(TAG, identity + "'s tox ID " + jtox.getAddress());
        
        jtox.setName(identity.getName());
        startService(ctx); // Log back in as the new identity
    }
    
    /**
     * Starts the Tox service.
     * @param ctx the context
     * @throws ToxException when the native call indicates an error
     * @throws ConnectException if there is no internet connection
     * @return the started service's {@link ComponentName} or null
     */
    public synchronized ComponentName startService(Context ctx) throws ToxException, ConnectException {
        Preconditions.checkNotNull(jtox, "Call loginAsIdentity before starting the service");
        
        if (!Util.isInternetConnected(ctx))
            throw new ConnectException("Internet is not connected");
        
        activeIdentity.setTox(this);
        
        serviceRunning = true;
        return ctx.startService(new Intent(ctx, ToxService.class));
    }
    
    /**
     * Stops the Tox service.
     * @param ctx the context
     * @return true if found and stopped, false otherwise
     * @throws IOException if the identity's data could not be saved
     * @throws ToxException if the Tox library indicates a problem
     */
    public synchronized boolean stopService(Context ctx) throws ToxException, IOException {
        activeIdentity.setTox(null);
        
        serviceRunning = false;
        return ctx.stopService(new Intent(ctx, ToxService.class));
    }
    
    /**
     * Cleans up Tox's data structures. This should only be called by {@link ToxService}.
     * @throws ToxException when the native call fails to cleanup Tox
     */
    public synchronized void killTox() throws ToxException {
        jtox.killTox();
        jtox = null;
        
        Log.d(TAG, "Killed tox");
    }
    
    /**
     * Calls the Tox main loop. This should be called at least 20 times a second.
     * @throws ToxException if the Tox library indicates a problem
     */
    public synchronized void doTox() throws ToxException {
        jtox.doTox();
    }
    
    /**
     * Saves a Tox data file for the current identity.
     */
    public void save() {
        try {
            byte[] bytes = jtox.save();
            app.getIdentityManager().saveIdentityData(activeIdentity, bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds a friend to the current identity's friend list.
     * @param address the friend's address
     * @param message an optional request message
     * @return the newly added friend, or null if already added
     * @throws ToxException if the Tox library indicates a problem
     * @throws FriendExistsException
     */
    public ToxFriend addFriend(String address, String message) throws ToxException, FriendExistsException {
        try {
            if (friends.exists(address))
                return null;
            
            ToxFriend friend = jtox.addFriend(address, message);
            Log.i(TAG, "Added friend: " + friend);
            
            // Add the new friend into the database
            Contact user = new Contact();
            user.setFriendn(friend.getFriendnumber());
            user.setId(db.insert(user).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
            
            friend.setUserId(user.getId()); // Store database ID on friend for easy access later
            return friend;
        } catch (FriendExistsException e) {
            throw new AssertionError(e); // This should never happen
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void deleteFriend(int friendNo) throws ToxException {
        jtox.deleteFriend(friendNo);
    }
    
    public void sendMessage(ToxFriend friend, Message message) throws ToxException {
        jtox.sendMessage(friend, message.getBody());
        
        // If that worked, store the message in the database
        try {
            message.setId(db.insert(message).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public ToxFriend confirmRequest(String address) throws ToxException, FriendExistsException {
        return jtox.confirmRequest(address);
    }
    
    public void refreshFriend(ToxFriend friend) throws ToxException {
        int n = friend.getFriendnumber();
        jtox.refreshClientId(n);
        jtox.refreshFriendName(n);
        jtox.refreshStatusMessage(n);
        jtox.refreshUserStatus(n);
        jtox.refreshFriendConnectionStatus(n);
    }
    
    public static String getPublicKey(String toxId) {
        return new String(toxId.substring(0, TOX_PUBKEY_LENGTH));
    }
    
    public NodeDirectory getDirectory() {
        return directory;
    }
    
    public Identity getActiveIdentity() {
        return activeIdentity;
    }
    
    /*
     * Delegates TODO JavaDoc
     */
    
    public void bootstrap(String address, int port, String pubkey) throws ToxException, UnknownHostException {
        jtox.bootstrap(address, port, pubkey);
    }
    
    public String getAddress() throws ToxException {
        return jtox.getAddress();
    }
    
    public FriendList getFriendList() {
        return (FriendList) jtox.getFriendList();
    }
    
    public int getInstanceNumber() {
        return jtox.getInstanceNumber();
    }
    
    public String getSelfName() throws ToxException {
        return jtox.getSelfName();
    }
    
    public ToxUserStatus getSelfUserStatus() throws ToxException {
        return jtox.getSelfUserStatus();
    }
    
    public CallbackHandler<ToxFriend> getCallbacks() {
        return callbacks;
    }
    
    public boolean isConnected() throws ToxException {
        return jtox.isConnected();
    }
    
    public void refreshList() {
        try {
            jtox.refreshList();
        } catch (ToxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void sendAction(ToxFriend friend, String action) throws ToxException {
        jtox.sendAction(friend, action);
    }
    
    public void sendIsTyping(int friendNo, boolean isTyping) throws ToxException {
        jtox.sendIsTyping(friendNo, isTyping);
    }
    
    public void setName(String name) throws ToxException {
        jtox.setName(name);
    }
    
    public void setSendReceipts(int friendNo, boolean sendReceipts) throws ToxException {
        jtox.setSendReceipts(friendNo, sendReceipts);
    }
    
    public void setStatusMessage(String statusMessage) throws ToxException {
        jtox.setStatusMessage(statusMessage);
    }
    
    public void setUserStatus(ToxUserStatus status) throws ToxException {
        jtox.setUserStatus(status);
    }
    
    public boolean toxFriendExists(int friendNo) throws ToxException {
        return jtox.toxFriendExists(friendNo);
    }
}
