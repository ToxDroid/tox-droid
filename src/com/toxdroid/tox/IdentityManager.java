
package com.toxdroid.tox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;
import com.toxdroid.App;
import com.toxdroid.data.Database;
import com.toxdroid.data.DatabaseHelper;
import com.toxdroid.data.Identity;
import com.toxdroid.util.Util;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

/**
 * Controls local user accounts (identities) and provides methods to control their state in the database and on the disk. These methods will
 * all block for an indeterminate time and should preferably be called in a separate thread / task.
 * 
 * 
 */
public class IdentityManager {
    private App app;
    private Database db;
    private List<Identity> identities = new ArrayList<Identity>();
    
    public IdentityManager(App app) {
        this.app = app;
        this.db = app.getDatabase();
    }
    
    /**
     * Loads all known identities from the database.
     */
    public void load() {
        Cursor cursor = null;
        try {
            identities.clear();
            
            cursor = db.select(DatabaseHelper.TABLE_IDENTITY, null, null, null, null);
            while (cursor.moveToNext()) {
                identities.add(new Identity(cursor));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
    
    /**
     * Creates a new identity and additionally stores it in the database.
     * @param name the identity's username
     * @return a future new identity
     */
    public Identity createIdentity(String name) {
        // Add user to database
        try {
            Identity identity = new Identity();
            identity.setName(name);
            identity.setDateAdded(Util.timeAsISO8601());
            identity.setId(db.insert(identity).get(db.getDefaultTimeout(), TimeUnit.MILLISECONDS));
            
            identities.add(identity);
            return identity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates a new identity and additionally stores it in the database. The given data array is also stored.
     * @param name the identity's name
     * @param data the identity data
     * @return the new identity
     * @throws IOException if the identity data could not be written to disk
     */
    public Identity createIdentity(String name, byte[] data) throws IOException {
        Identity identity = createIdentity(name);
        saveIdentityData(identity, data);
        
        return identity;
    }
    
    /**
     * Deletes the given identity and its data file. This will also remove all associated messages and conversation logs for this identity.
     * @param identity the identity
     */
    public void deleteIdentity(long identityId) {
        try {
            db.delete(DatabaseHelper.TABLE_IDENTITY, "_id = ?", Util.asStringArray(identityId));
            
            File f = new File(Environment.getDataDirectory() + "/" + getDataFilePath(identityId));
            if (f.exists() && !f.delete())
                throw new IOException("Unable to delete data file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void saveIdentityData(final Identity identity, final byte[] data) throws IOException {
        Log.d("IdentityManager", "Saving " + data.length + " bytes of data for " + identity);
        Util.writeInternalStorage(data, getDataFilePath(identity.getId()), app);
    }
    
    public byte[] loadIdentityData(final Identity identity) throws IOException {
        InputStream in = null;
        try {
            in = Util.readInternalStorage(getDataFilePath(identity.getId()), app);
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            return null; // Data does not exist (probably)
        } finally {
            if (in != null)
                in.close();
        }
    }
    
    public static String getDataFilePath(long identityId) {
        return "user" + identityId + ".tox";
    }
    
    public List<Identity> getIdentities() {
        return Collections.unmodifiableList(identities);
    }
}
