package com.subinkrishna.curah.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author Subinkrishna Gopi
 */
public class Logger {

    /** Log Tag for all logs */
    private static final String TAG = "CURAH";

    /** Maximum length of the tag. (Android supports 23) */
    private static final int MAX_TAG_LENGTH = 20;

    private Logger() {

    }

    /**
     * Prints an DEBUG log.
     *
     * @param tag
     * @param message
     */
    public static void d(final String tag, final String message) {
        // TODO: Logging need to be enabled/disabled based on BuildConfig. Control it in build file.
        if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(message)) {
            Log.d(TAG, String.format("[%s] %s", tagify(tag), message));
        }
    }

    /**
     * Prints an ERROR log.
     *
     * @param tag
     * @param message
     */
    public static void e(final String tag, final String message) {
        e(tag, message, null);
    }

    /**
     * Prints an ERROR log.
     *
     * @param tag
     * @param message
     */
    public static void e(final String tag, final String message, final Throwable throwable) {
        // TODO: Logging need to be enabled/disabled based on BuildConfig. Control it in build file.
        if (!TextUtils.isEmpty(tag) && !TextUtils.isEmpty(message)) {
            Log.e(TAG, String.format("[%s] ERROR: %s", tagify(tag), message));
            // Print exception stack trace if available
            if (null != throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * Trims the log tag if it's length exceeds the limit.
     *
     * @param tag
     * @return
     */
    private static String tagify(final String tag) {
        return (tag.length() > MAX_TAG_LENGTH) ? tag.substring(0, MAX_TAG_LENGTH - 1) : tag;
    }

}
