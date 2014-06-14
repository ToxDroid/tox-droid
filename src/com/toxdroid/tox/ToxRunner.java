
package com.toxdroid.tox;

import im.tox.jtoxcore.ToxException;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.toxdroid.App;
import com.toxdroid.tox.NodeDirectory.Node;

import android.util.Log;

/**
 * A thread which runs Tox.
 * 
 */
public class ToxRunner {
    private static final String TAG = "ToxRunner";
    private boolean running;
    private ToxService service;
    private ToxCore tox;
    private Timer timer;
    
    private long ticks;
    private long lastConnection;
    private long lastStatus;
    private long lastBootstrap;
    private long cycleStart;
    
    private TimerTask toxDoTask = new TimerTask() {
        @Override
        public void run() {
            try {
                monitorStatus();
                
                tox.doTox();
                ticks++;
            } catch (Exception e) {
                fail("Exception in doToxThread", e);
                service.stopSelf(); // Kill the service
            }
        }
        
        public void monitorStatus() throws ToxException {
            long now = System.currentTimeMillis();
            long downtime = (now - lastConnection);
            
            if (tox.isConnected()) {
                lastConnection = now;
                writeStatus("Tox is connected", 300000);
            } else {
                String last = lastConnection == 0 ? "(never)" : downtime + "ms ago";
                writeStatus("Tox is not connected; last connect was " + last, 5000);
                
                if (downtime > 10000 && (System.currentTimeMillis() - lastBootstrap) > 5000) {
                    bootstrap(tox); // Try a different node
                }
            }
            
            // Debug tick rate
            if (ticks % 20 == 0) {
                long delta = now - cycleStart;
                float rate = 1000.f / delta;
                
                if (delta > 1000)
                    Log.w(TAG, "Tox thread is running too slow (" + rate + " of required)");
                else if (delta < 500)
                    Log.w(TAG, "Tox thread is running too fast (" + rate + " of required)");
                
                cycleStart = now;
            }
        }
        
        private void writeStatus(String status, long period) {
            long now = System.currentTimeMillis();
            if (now - lastStatus > period) {
                lastStatus = System.currentTimeMillis();
                Log.v(TAG, status);
            }
        }
    };
    
    public ToxRunner(ToxCore tox, ToxService service) {
        this.tox = tox;
        this.service = service;
        this.timer = new Timer("ToxDo", false);
    }
    
    public void start() {
        ticks = 0;
        lastConnection = 0;
        running = true;
        
        try {
            bootstrap(tox);
            
            cycleStart = lastStatus = System.currentTimeMillis();
            timer.scheduleAtFixedRate(toxDoTask, 0, 1000 / 25);
        } catch (Exception e) {
            fail("Tox startup fail!", e);
        }
    }
    
    /**
     * Connects to a known Tox client and joins the network.
     * @param tox the ToxCore
     * @throws ToxException if the Tox library indicates an error
     */
    private void bootstrap(ToxCore tox) throws ToxException {
        NodeDirectory directory = tox.getDirectory();
        
        // Use a random node to bootstrap with
        List<Node> nodes = directory.getNodes();
        Node n;
        while (true) {
            n = nodes.get((int) (Math.random() * nodes.size()));
            String addr = n.ip4; // n.ip6 == null ? n.ip4 : n.ip6;
            
            try {
                Log.i(TAG, String.format("Bootstrapping: addr=%s, port=%d, key=%s", addr, n.port, n.publicKey));
                tox.bootstrap(addr, n.port, n.publicKey);
                break; // Finish when we've bootstrapped with one valid address
            } catch (UnknownHostException e) {
                Log.w(TAG, "Invalid bootstrap node address " + addr + ", removing from directory");
                directory.remove(n);
            }
        }
        
        lastBootstrap = System.currentTimeMillis();
    }
    
    /**
     * Call this to cleanly shutdown Tox.
     */
    public void shutdown() {
        timer.cancel();
        running = false;
        
        tox.save();
        try {
            tox.killTox();
        } catch (ToxException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void fail(String message, Exception e) {
        Log.e(TAG, message, e);
        service.stopSelf();
    }
    
    public boolean isRunning() {
        return running;
    }
}
