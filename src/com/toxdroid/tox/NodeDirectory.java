
package com.toxdroid.tox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.toxdroid.App;
import com.toxdroid.util.Util;

/**
 * A directory of known Tox addresses.
 * 
 */
public class NodeDirectory {
    private static final String TAG = "NodeDirectory";
    private URL url;
    private List<Node> nodes = new ArrayList<Node>();
    
    public class Node {
        public final String ip6;
        public final String ip4;
        public final String publicKey;
        public final int port;
        public final String owner;
        
        public Node(String ip6, String ip4, String publicKey, int port, String owner) {
            this.ip6 = ip6;
            this.ip4 = ip4;
            this.publicKey = publicKey;
            this.port = port;
            this.owner = owner;
        }
    }
    
    /**
     * Creates a new NodeList instance.
     */
    public NodeDirectory() {
        try {
            url = new URL("https://kirara.ca/poison/Nodefile.json");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Fills the list with known Tox nodes.
     * @param forceDownload if true, force a redownload of the nodelist data
     * @throws IOException if the nodelist cannot be accessed
     */
    public void populate(Context ctx, String nodeFile) throws IOException {
        Preconditions.checkArgument(nodeFile.equals(App.NODEFILE) || nodeFile.equals(App.NODEFILE_DEFAULT),
                "Invalid argument");
        
        InputStream in = null;
        try {
            in = Util.readInternalStorage(nodeFile, ctx);
            loadFromJson(in);
        } finally {
            if (in != null)
                in.close();
        }
    }
    
    /**
     * Attempts to update the nodelist from the online source. If the nodelist already exists on disk and <code>force</code> is false, this
     * method will do nothing.
     * @param ctx the context
     * @param force true to force download
     * @throws IOException if the nodelist cannot be accessed
     */
    public void updateList(Context ctx, boolean force) throws IOException {
        if (!force) {
            File file = new File(Environment.getDataDirectory() + "/" + App.NODEFILE);
            if (file.exists())
                return;
        }
        
        // Download the file
        InputStream in = null;
        try {
            URLConnection conn = url.openConnection();
            in = new BufferedInputStream(conn.getInputStream());
            Util.writeInternalStorage(in, App.NODEFILE, ctx);
            
            Log.d(TAG, "Successfully downloaded nodelist from " + url.toString());
        } finally {
            if (in != null)
                in.close();
        }
    }
    
    public void remove(Node n) {
        nodes.remove(n);
    }
    
    private void loadFromJson(InputStream json) throws IOException {
        nodes.clear();
        
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(json, Charsets.UTF_8);
            
            int i = 0;
            JSONObject all = new JSONObject(CharStreams.toString(reader));
            JSONArray servers = all.getJSONArray("servers");
            
            while (!servers.isNull(i)) {
                JSONObject entry = servers.getJSONObject(i);
                String ipv6 = checkIfNull(entry.optString("ipv6", null));
                String ipv4 = checkIfNull(entry.optString("ipv4", null));
                String owner = checkIfNull(entry.optString("owner", null));
                
                nodes.add(new Node(ipv6, ipv4, entry.getString("pubkey"), entry.getInt("port"), owner));
                i++;
            }
            
            Log.i(TAG, "Added " + (i - 1) + " nodes from local file");
        } catch (JSONException e) {
            throw new IOException("JSON failure");
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    
    private String checkIfNull(String s) {
        return s.equals("null") ? null : s; // JSON null strings are coerced as the string 'null'; we don't want that
    }
    
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
}
