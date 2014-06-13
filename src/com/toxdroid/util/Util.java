
package com.toxdroid.util;

import im.tox.jtoxcore.ToxUserStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.io.ByteStreams;
import com.toxdroid.R;
import com.toxdroid.data.User;

/**
 * Helpful and assorted methods.
 * 
 */
public class Util {
    /**
     * Compares two numbers, returning a value based on their relationship.
     * @param a the first number
     * @param b the second number
     * @return 0 if equal; 1 if a is greater than b; otherwise -1
     */
    public static int compare(int a, int b) {
        // Integer.compare is a Java 7 method
        if (a == b)
            return 0;
        if (a > b)
            return -1;
        return 1;
    }
    
    /**
     * Copies the contents of <code>in</code> to a file in the data folder. The input stream is not closed or flushed.
     * @param in the input stream
     * @param filename the destination file
     * @param ctx the context
     * @throws IOException if something went wrong
     */
    public static void writeInternalStorage(InputStream in, String filename, Context ctx) throws IOException {
        FileOutputStream out = null;
        try {
            // assets/Nodefile > data/Nodefile
            out = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            ByteStreams.copy(in, out);
        } finally {
            if (out != null)
                out.close();
        }
    }
    
    /**
     * Copies the full contents of <code>bytes</code> to a file in the data folder.
     * @param bytes the bytes
     * @param filename the destination file
     * @param ctx the context
     * @throws IOException if something went wrong
     */
    public static void writeInternalStorage(byte[] bytes, String filename, Context ctx) throws IOException {
        FileOutputStream out = null;
        try {
            // assets/Nodefile > data/Nodefile
            out = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            out.write(bytes);
        } finally {
            if (out != null)
                out.close();
        }
    }
    
    /**
     * Opens a file for reading from internal storage.
     * @param filename the filename
     * @param ctx the context
     * @return the input stream of the file
     * @throws IOException if something went wrong
     */
    public static InputStream readInternalStorage(String filename, Context ctx) throws IOException {
        return ctx.openFileInput(filename);
    }
    
    /**
     * Opens a file for reading from internal storage.
     * @param file the file
     * @param ctx the context
     * @return the input stream of the file
     * @throws IOException if something went wrong
     */
    public static InputStream readInternalStorage(File file, Context ctx) throws IOException {
        return readInternalStorage(file.getPath(), ctx);
    }
    
    /**
     * Checks if an Internet connection is available.
     * @param ctx the context
     * @return true if connected
     */
    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        
        return netInfo != null && netInfo.isConnected();
    }
    
    /**
     * Convenience method. Places all arguments into a new string array (conversion uses {@link Object#toString()}).
     * @param args the objects
     * @return the string array populated with the object's string representations
     */
    public static String[] asStringArray(Object... args) {
        String[] out = new String[args.length];
        for (int i = 0; i < args.length; i++)
            out[i] = args[i].toString();
        
        return out;
    }
    
    /**
     * Converts the given time into an ISO8601 representation.
     * @param date the date
     * @return the string
     */
    @SuppressLint("SimpleDateFormat")
    public static String timeAsISO8601(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }
    
    /**
     * Converts the current time into an ISO8601 representation.
     * @return the string
     */
    public static String timeAsISO8601() {
        return timeAsISO8601(new Date());
    }
    
    /**
     * Initializes a user card view.
     * @param v the view (type = {@link R.layout.user_card})
     * @param item the user
     * @return the view
     */
    public static View newUserCard(View v, User user) {
        if (user == null)
            return v;
        
        TextView name = (TextView) v.findViewById(R.id.card_name);
        TextView statusMessage = (TextView) v.findViewById(R.id.card_status_message);
        ImageView statusIcon = (ImageView) v.findViewById(R.id.card_status_indicator);
        
        name.setText(user.getName());
        statusMessage.setText(user.getStatusMessage());
        
        int icon;
        ToxUserStatus status = user.getStatus();
        if (user.isOnline()) {
            if (status == ToxUserStatus.TOX_USERSTATUS_AWAY)
                icon = R.drawable.ic_away;
            else if (status == ToxUserStatus.TOX_USERSTATUS_BUSY)
                icon = R.drawable.ic_busy;
            else
                icon = R.drawable.ic_online;
        } else {
            icon = R.drawable.ic_offline;
        }
        statusIcon.setImageResource(icon);
        
        return v;
    }
}
