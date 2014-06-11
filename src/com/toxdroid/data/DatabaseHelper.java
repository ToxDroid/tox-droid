
package com.toxdroid.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Provides methods to setup and manage the SQLite database.
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "toxdroid.db";
    private static final int DATABASE_VERSION = 8;
    
    public static final String TABLE_CONTACT = "contact";
    public static final String TABLE_IDENTITY = "identity";
    public static final String TABLE_CHAT = "chat";
    public static final String TABLE_MESSAGE = "message";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contact (_id INTEGER PRIMARY KEY AUTOINCREMENT," // Known users
                + "friendn INTEGER DEFAULT NULL," // Their friend number (null if not a friend)
                + "name TEXT);"); // Their name (only applicable if they aren't a friend)
        
        db.execSQL("CREATE TABLE identity (_id INTEGER PRIMARY KEY AUTOINCREMENT," // A local user identity
                + "name TEXT NOT NULL);");
        
        db.execSQL("CREATE TABLE chat (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "me INTEGER NOT NULL REFERENCES identity (_id) ON DELETE CASCADE," // Our user details
                + "them INTEGER DEFAULT NULL REFERENCES user (_id) ON DELETE CASCADE," // The other person's details (null if group chat)
                + "groupn INTEGER DEFAULT NULL);"); // The group chat number (null if 1-1 chat)
        
        db.execSQL("CREATE TABLE message (_id INTEGER PRIMARY KEY,"
                + "chat INTEGER REFERENCES chat (_id) ON DELETE CASCADE,"
                + "sender INTEGER NOT NULL REFERENCES user (_id) ON DELETE CASCADE," + "body TEXT NOT NULL,"
                + "timestamp TEXT NOT NULL," // Time as ISO8601 string ("YYYY-MM-DD HH:MM:SS.SSS")
                + "pos INTEGER NOT NULL);");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Database upgrade (" + oldVersion + " to " + newVersion + "), dropping all data");
        db.execSQL("DROP TABLE IF EXISTS contact");
        db.execSQL("DROP TABLE IF EXISTS identity");
        db.execSQL("DROP TABLE IF EXISTS message");
        db.execSQL("DROP TABLE IF EXISTS chat");
        onCreate(db);
    }
}
