package com.subinkrishna.curah.data.fetcher;

import android.content.Context;
import android.text.TextUtils;

import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.util.FSUtil;
import com.subinkrishna.curah.util.NetworkUtil;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * Feed fetcher implementation. Fetches the feed contents from the remote URL mentioned
 * in the Feed enumeration and saves the file in the local filesystem (at least for now,
 * this implementation may change in the future)
 *
 * @author Subinkrishna Gopi
 */
public class FeedFetcher {

    /** Log Tag */
    private static final String TAG = FeedFetcher.class.getSimpleName();

    /**
     * Fetches the feed (using the URL mentioned in the enum) and saves the contents locally
     * using the file name mentioned in the enum (again!)
     *
     * @param context
     * @param feed
     * @return
     */
    public static FetchStatus fetch(final Context context,
                                    final Feed feed) {
        final FetchStatus status = new FetchStatus();

        // Abort if input is invalid
        if ((null == context) || (null == feed) || TextUtils.isEmpty(feed.mUrl)) {
            e(TAG, "Aborting feed fetch. Invalid feed info.");
            status.isSuccess = false;
            status.mErrorState = FetchStatus.ErrorState.InvalidInput;
            return status;
        }

        // Get feed contents as bytes
        // TODO: May use the DownloadManager to do this
        final byte[] contents = NetworkUtil.fetch(feed.mUrl);
        if ((null == contents) || (0 == contents.length)) {
            e(TAG, "No contents retrieved from URL: " + feed.mUrl);
            status.isSuccess = false;
            status.mErrorState = FetchStatus.ErrorState.NetworkError;
            return status;
        }

        // Save the file in local file system
        final boolean isSavedLocally = FSUtil.save(context, feed, contents);

        status.isSuccess = isSavedLocally;
        status.mErrorState = isSavedLocally
                ? FetchStatus.ErrorState.FileWriteError
                : FetchStatus.ErrorState.None;

        return status;
    }

    public static class FetchStatus {
        /** Error state */
        public enum ErrorState { None, NetworkError, InvalidInput, FileWriteError, Unknown }

        public boolean isSuccess = false;
        public ErrorState mErrorState = ErrorState.Unknown;

        @Override
        public String toString() {
            return "FetchStatus { isSuccess: " + isSuccess + (isSuccess ? "" : ", ErrorState: " + mErrorState ) + " }";
        }
    }
}
