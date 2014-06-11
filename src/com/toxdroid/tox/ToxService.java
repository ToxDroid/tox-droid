
package com.toxdroid.tox;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.toxdroid.App;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * A service which runs Tox. Calling the method from a service allows it to run even if the application is paused.
 * 
 */
public class ToxService extends Service {
    private static final String TAG = "ToxService";
    private ToxRunner runner;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final App app = App.get(getApplication());
        runner = new ToxRunner(app.getTox(), this);
        
        // Attempt to start up Tox by bootstrapping
        app.queueTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // Try to update the node directory
                ToxCore tox = app.getTox();
                NodeDirectory directory = tox.getDirectory();
                try {
                    directory.updateList(ToxService.this, false);
                    directory.populate(ToxService.this, App.NODEFILE);
                } catch (IOException e) {
                    Log.d(TAG, "Unable to download latest nodefile (" + e.getMessage() + "), using default");
                    directory.populate(ToxService.this, App.NODEFILE_DEFAULT);
                }
                
                runner.start();
                return null;
            }
        });
        
        return Service.START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        runner.shutdown();
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Disallow IPC binding
    }
}
