package com.subinkrishna.curah.data.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.subinkrishna.curah.data.feed.FeedMeta;
import com.subinkrishna.curah.data.parser.RssFeedParser;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.util.FSUtil;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * An AsyncTaskLoader implementation that loads feeds saved in the local file system.
 *
 * @author Subinkrishna Gopi
 */
public class FileSystemFeedLoader extends AsyncTaskLoader<FeedMeta> {

    /** Log tag */
    private static final String TAG = FileSystemFeedLoader.class.getSimpleName();

    /**
     * Create an instance of FileSystemFeedLoader.
     *
     * @param context
     * @param feed
     * @return
     */
    public static FileSystemFeedLoader newInstance(final Context context,
                                                   final Feed feed) {
        // Abort if input is invalid
        if ((null == context) || (null == feed)) {
            return null;
        }

        d(TAG, "Create loader instance for " + feed);

        // Create an instance of the loader
        FileSystemFeedLoader loader = new FileSystemFeedLoader(context);
        loader.mFeed = feed;
        return loader;
    }

    private Feed mFeed;

    public FileSystemFeedLoader(Context context) {
        super(context);
    }

    @Override
    public FeedMeta loadInBackground() {
        d(TAG, "loadInBackground");

        FeedMeta result = null;

        // Read the contents
        final byte[] feedContents = FSUtil.read(getContext(), mFeed);
        if (null != feedContents) {
            result = RssFeedParser.parse(new String(feedContents));
        }

        return result;
    }
}
