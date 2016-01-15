package com.subinkrishna.curah.util;

import android.content.Context;
import android.text.TextUtils;

import com.subinkrishna.curah.eunmeration.Feed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * File system utility.
 *
 * @author Subinkrishna Gopi
 */
public class FSUtil {

    /** Log tag */
    private static final String TAG = FSUtil.class.getSimpleName();

    private FSUtil() {

    }

    /**
     * Save the incoming byte stream to the mentioned file.
     *
     * @param context
     * @param feed
     * @param contents
     * @return
     */
    public static final boolean save(final Context context,
                                     final Feed feed,
                                     final byte[] contents) {
        boolean isSavedLocally = false;
        File file = null;
        FileOutputStream fileOutputStream = null;

        try {
            file = new File(context.getFilesDir(), feed.mLocalFilename);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(contents);
            isSavedLocally = true;
            d(TAG, String.format("File saved locally - %s (%d bytes)", file.getAbsolutePath(), contents.length));
        } catch (Exception e) {
            e(TAG, "Exception file saving feed contents locally: " + feed, e);
            isSavedLocally = false;
        } finally {
            // Cleanup
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {}
                fileOutputStream = null;
            }
        }

        return isSavedLocally;
    }

    /**
     * Checks if a local copy of the specified feed exists.
     *
     * @param context
     * @param feed
     * @return
     */
    public static boolean fileExists(final Context context,
                                      final Feed feed) {
        final File file = getFile(context, feed);
        return (null != file) && file.exists();
    }

    /**
     * Read the file contents
     *
     * @param context
     * @param feed
     * @return
     */
    public static byte[] read(final Context context,
                              final Feed feed) {
        // Abort if a local copy of the mentioned feed doesn't exist
        final File file = getFile(context, feed);
        if ((null == file) || !file.exists()) {
            e(TAG, "File not found: " + feed.mLocalFilename);
            return null;
        }

        byte[] contents = null;
        FileInputStream fileIn = null;
        ByteArrayOutputStream out = null;

        try {
            fileIn = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            NetworkUtil.copyStream(fileIn, out);
            contents = out.toByteArray();
        } catch (Exception e) {
            e(TAG, "Exception while reading file contents: " + feed.mLocalFilename, e);
            contents = null;
        } finally {
            // Clean up
            if (null != fileIn) {
                try {
                    fileIn.close();
                } catch (Exception e) {}
                fileIn = null;
            }
            out = null;
        }

        return contents;
    }

    /**
     * Returns the File instance of the mentioned feed.
     *
     * @param context
     * @param feed
     * @return
     */
    public static File getFile(final Context context,
                               final Feed feed) {
        // Abort if the input is invalid
        if ((null == context) ||
            (null == feed) ||
            TextUtils.isEmpty(feed.mLocalFilename)) {
            return null;
        }

        return new File(context.getFilesDir(), feed.mLocalFilename);
    }

}
