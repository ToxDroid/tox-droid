
package com.toxdroid.data;

import java.util.concurrent.Callable;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * A helper class which grabs and closes a database connection. It also logs all exceptions thrown during execution.
 * 
 * 
 * @param <V> the Callable return type
 */
public abstract class DatabaseCallable<V> implements Callable<V> {
    private Database db;
    private boolean write;
    
    public DatabaseCallable(Database db, boolean write) {
        this.db = db;
        this.write = write;
    }
    
    public abstract V doCall(SQLiteDatabase conn) throws Exception;
    
    @Override
    public V call() throws Exception {
        SQLiteDatabase conn = null;
        try {
            conn = db.getConnection(write);
            return doCall(conn);
        } catch (Exception e) {
            Log.e("DatabaseCallable", "Exception in database task", e);
            throw e;
        } finally {
            if (conn != null)
                conn.close();
        }
    }
    
}
