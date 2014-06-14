
package com.toxdroid.tox;

import im.tox.jtoxcore.FriendExistsException;
import im.tox.jtoxcore.JTox;
import im.tox.jtoxcore.ToxException;
import im.tox.jtoxcore.ToxUserStatus;
import im.tox.jtoxcore.callbacks.CallbackHandler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Preconditions;
import com.toxdroid.App;
import com.toxdroid.data.Contact;
import com.toxdroid.data.Database;
import com.toxdroid.data.DatabaseHelper;
import com.toxdroid.data.Identity;
import com.toxdroid.data.Message;
import com.toxdroid.util.Util;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;

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
    private JTox<Contact> jtox;
    private FriendList friends;
    private CallbackHandler<Contact> callbacks;
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
        this.callbacks = new CallbackHandler<Contact>(friends);
    }
    
    /**
     * Attempts to log into the Tox network as the given user.
     * @param ctx the context
     * @param identity the identity
     * @throws ToxException if the Tox library indicates a problem
     * @throws IOException if the user data could not be loaded
     * @throws ConnectException if there is no internet connection
     */
    public void loginAsIdentity(Context ctx, Identity identity) throws ToxException, IOException, ConnectException {
        if (!checkLoginPreconditions(ctx, identity))
            return;
        
        if (serviceRunning)
            stopService(ctx); // Log off from an existing session
        
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
            jtox = new JTox<Contact>(friends, callbacks);
        else
            // Load last DHT, friend list, key details etc...
            jtox = new JTox<Contact>(data, friends, callbacks);
        
        loadAdditionalFriendData(jtox);
        identity.setAddress(jtox.getAddress());
        
        jtox.setName(identity.getName());
        startService(ctx); // Log back in as the new identity
    }
    
    private boolean checkLoginPreconditions(Context ctx, Identity identity) throws ConnectException {
        if (identity == activeIdentity)
            return false;
        
        if (!Util.isInternetConnected(ctx)) {
            throw new ConnectException("Internet is not connected");
        }
        
        return true;
    }
    
    private void loadAdditionalFriendData(JTox<Contact> jtox) throws IOException {
        // Load data not stored by jtox.save (i.e. date added, blocked status)
        SparseArray<ContentValues> records = new SparseArray<ContentValues>();
        Cursor cursor = null;
        try {
            cursor = db.select(DatabaseHelper.TABLE_CONTACT, "identity = ?",
                    Util.asStringArray(activeIdentity.getId()),
                    Util.asStringArray("friendn", "added"),
                    "friendn");
            
            while (cursor.moveToNext()) {
                ContentValues vals = new ContentValues();
                vals.put("added", cursor.getString(1));
                
                records.append(cursor.getInt(0), vals);
            }
        } catch (TimeoutException e) {
            throw new IOException("Database timeout");
        } finally {
            if (cursor != null)
                cursor.close();
        }
        
        for (Contact c : jtox.getFriendList().all()) {
            ContentValues vals = records.get(c.getFriendnumber());
            if (vals == null) {
                Log.w(TAG, "Database out of sync with jtox - some friend data will be missing");
                continue;
            }
            
            c.setDateAdded((String) vals.get("added"));
        }
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
     * Saves a Tox data file for the current identity and the friends list.
     */
    public void save() {
        if (activeIdentity == null)
            return;
        
        try {
            byte[] bytes = jtox.save();
            app.getIdentityManager().saveIdentityData(activeIdentity, bytes);
            
            for (Contact friend : friends.all()) {
                Future<Integer> future = db.update(friend, "_id = ?", Util.asStringArray(friend.getDatabaseId()));
                future.get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS);
            }
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
    public Contact addFriend(String address, String message) throws ToxException, FriendExistsException {
        try {
            if (friends.exists(address))
                return null;
            
            Contact friend = jtox.addFriend(address, message);
            friend.setIdentity(activeIdentity.getId());
            friend.setDateAdded(Util.timeAsISO8601());
            friend.setDatabaseId(db.insert(friend).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
            
            Log.i(TAG, "Added friend: " + friend);
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
    
    public void deleteContact(Contact contact) throws ToxException {
        if (contact.isFriend()) {
            deleteFriend(contact.getFriendnumber());
        } else {
            db.delete(DatabaseHelper.TABLE_CONTACT, "_id = ?", Util.asStringArray(contact.getDatabaseId()));
        }
    }
    
    public void sendMessage(Contact friend, Message message) throws ToxException {
        jtox.sendMessage(friend, message.getBody());
        
        // If that worked, store the message in the database
        try {
            message.setId(db.insert(message).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Contact confirmRequest(String address) throws ToxException, FriendExistsException {
        return jtox.confirmRequest(address);
    }
    
    public void refreshFriend(Contact friend) throws ToxException {
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
    
    public CallbackHandler<Contact> getCallbacks() {
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
    
    public void sendAction(Contact friend, String action) throws ToxException {
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
