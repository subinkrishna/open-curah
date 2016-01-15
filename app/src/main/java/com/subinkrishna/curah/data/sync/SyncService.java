package com.subinkrishna.curah.data.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by subin on 8/7/14.
 */
public class SyncService extends Service {

    private static SyncAdapter sSyncAdapter;
    private static Object sLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        // Creates a singleton instance
        synchronized (sLock) {
            sSyncAdapter = (null == sSyncAdapter) ? new SyncAdapter(this, true) : sSyncAdapter;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
