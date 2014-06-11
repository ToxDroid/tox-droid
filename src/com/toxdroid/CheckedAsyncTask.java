
package com.toxdroid;

import im.tox.jtoxcore.ToxException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An extension of {@link AsyncTask} which provides checked execution for a task associated with an Activity.
 * 
 * @param <Params> the type of arguments given in {@link #execute(Object...)}
 * @param <Progress> the type of progress recieved by {@link #onProgressUpdate(Object...)}
 * @param <Result> the return type recieved by {@link #onPostExecute(Object)}
 * 
 */
public abstract class CheckedAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private Exception e;
    
    @Override
    protected Result doInBackground(Params... params) {
        try {
            return checkedDoInBackground(params);
        } catch (Exception e) {
            this.e = e;
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(Result result) {
        if (e != null)
            onFail(e);
        else
            onSuccess(result);
    }
    
    /**
     * Identical to {@link #doInBackground(Object...)}, except that any uncaught exception will cause {@link #onFail(Exception)} to be
     * called during {@link #onPostExecute(Object)}.
     * @param params
     * @return
     * @throws Exception
     */
    public abstract Result checkedDoInBackground(Params... params) throws Exception;
    
    /**
     * Called when {@link #DoInBackground(Object...)} is successful.
     * @param result the result of {@link #doInBackground(Object...)}
     */
    protected void onSuccess(Result result) {
        // Do nothing!
    }
    
    /**
     * Called when {@link #DoInBackground(Object...)} has failed due to an uncaught exception. The default behavior emits an error log
     * message.
     * @param exception the causing exception
     */
    protected void onFail(Exception e) {
        Log.e("CheckedAsyncTask", e instanceof ToxException ? "Tox library exception "
                + ((ToxException) e).getError().toString() : "Task failed unexpectedly", e);
    }
}
