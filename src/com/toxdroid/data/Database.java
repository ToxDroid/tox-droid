
package com.toxdroid.data;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.toxdroid.DatabaseCallable;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
    private DatabaseHelper helper;
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    public Database(Context ctx) {
        this.helper = new DatabaseHelper(ctx);
    }
    
    public Future<Long> insert(final DatabaseRecord record) {
        return submit(new DatabaseCallable<Long>(this, true) {
            @Override
            public Long doCall(SQLiteDatabase conn) throws Exception {
                long rowId = conn.insert(record.getTable(), null, record.asValues());
                if (rowId == -1)
                    throw new SQLException();
                
                return rowId;
            }
        });
    }
    
    public Cursor select(final String table, final String where, final String[] whereArgs, final String[] columns,
            final String orderBy) throws TimeoutException {
        try {
            return submit(new Callable<Cursor>() {
                @Override
                public Cursor call() throws Exception {
                    try {
                        SQLiteDatabase conn = getConnection(false);
                        return conn.query(table, columns, where, whereArgs, null, null, orderBy);
                    } catch (Exception e) {
                        Log.e("DatabaseCallable", "Exception in database task", e); // hack
                        throw e;
                    }
                }
            }).get(getDefaultTimeout(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Future<Integer> delete(final String table, final String where, final String[] whereArgs) {
        return submit(new DatabaseCallable<Integer>(this, true) {
            @Override
            public Integer doCall(SQLiteDatabase conn) throws Exception {
                return conn.delete(table, where, whereArgs);
            }
        });
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }
    
    public SQLiteDatabase getConnection(boolean write) {
        return write ? helper.getWritableDatabase() : helper.getReadableDatabase();
    }
    
    public long getDefaultTimeout() {
        return 2000;
    }
}
