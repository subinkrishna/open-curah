package com.subinkrishna.curah.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by subin on 2/14/15.
 */
public class Config {

    /**
     * Checks if the device is in landscape mode.
     *
     * @param context
     * @return
     */
    public static boolean isLandscape(Context context) {
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Checks if the device is tablet or not.
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }


    /**
     * Checks if network available.
     *
     * @param context
     * @return
     */
    public static boolean isOnline(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return (null != activeNetwork) && activeNetwork.isConnectedOrConnecting();
    }
}
