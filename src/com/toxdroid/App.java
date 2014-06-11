
package com.toxdroid;

import im.tox.jtoxcore.ToxException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.toxdroid.data.Database;
import com.toxdroid.tox.IdentityManager;
import com.toxdroid.tox.ToxCore;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Shared application state. Services such as Tox, the database, and user identities may be accessed here.
 * 
 */
public class App extends Application {
    private static final String TAG = "App";
    private ToxCore tox;
    private Database db;
    private IdentityManager identityManager;
    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    
    public static final String NODEFILE = "Nodefile.json";
    public static final String NODEFILE_DEFAULT = "Nodefile-default.json";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            copyAssets();
            
            db = new Database(this);
            identityManager = new IdentityManager(this);
            identityManager.load();
            
            tox = new ToxCore(this);
        } catch (ToxException e) {
            fail("Tox failure", e);
        }
    }
    
    /**
     * Copies files from <code>assets</code> to the <code>data</code> folder for easy access
     */
    private void copyAssets() {
        // Copy a default nodefile over incase we cannot access the internet
        File nf = new File(Environment.getDataDirectory(), NODEFILE_DEFAULT); // data/Nodefile exists?
        if (nf.exists())
            return;
        
        InputStream in = null;
        try {
            try {
                in = getAssets().open(NODEFILE_DEFAULT);
                Util.writeInternalStorage(in, NODEFILE_DEFAULT, this);
            } finally {
                if (in != null)
                    in.close();
            }
            Log.i(TAG, "Stored: " + Arrays.toString(fileList()));
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy assets", e);
        }
    }
    
    /**
     * Queue a task for async execution.
     * @param callable the task
     * @return a ListenableFuture
     */
    public <T> ListenableFuture<T> queueTask(Callable<T> callable) {
        return executor.submit(callable);
    }
    
    public ToxCore getTox() {
        return tox;
    }
    
    public Database getDatabase() {
        return db;
    }
    
    public IdentityManager getIdentityManager() {
        return identityManager;
    }
    
    private void fail(String message, Exception e) {
        Toast.makeText(this, "Oops! " + message + ". Please try again later.", Toast.LENGTH_SHORT).show(); // TODO Replace this with popup
        Log.e(TAG, message, e);
    }
    
    /**
     * Convenience method to grab the App instance from a context.
     * @param ctx the context
     * @return the App
     */
    public static App get(Context ctx) {
        // This method will probably go in later version, to be replaced by a no-args getter.
        return (App) ctx.getApplicationContext();
    }
}
