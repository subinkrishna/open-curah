package com.subinkrishna.curah.data.feed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by subin on 8/8/14.
 */
public class FeedMeta {

    public String mFeedTitle;
    public long mPubDate;
    public List<FeedItem> mFeedItems;

    /**
     * Add a FeedItem to the list.
     *
     * @param item
     * @return
     */
    public FeedMeta addItem(final FeedItem item) {
        if (null != item) {
            mFeedItems = (null == mFeedItems) ? new ArrayList<FeedItem>() : mFeedItems;
            mFeedItems.add(item);
        }
        return this;
    }

    /**
     * Returns the number of FeedItems.
     *
     * @return
     */
    public int size() {
        return (null != mFeedItems) ? mFeedItems.size() : 0;
    }

    /**
     * Returns the item at the specified position.
     *
     * @param position
     * @return
     */
    public FeedItem getItemAt(final int position) {
        return (null != mFeedItems) ? mFeedItems.get(position) : null;
    }

    /**
     * Sort the items based on the publish date.
     */
    public void sort() {
        if (null != mFeedItems) {
            Collections.sort(mFeedItems);
        }
    }

    /**
     * Get the feed publish date.
     *
     * @return
     */
    public long getPublishDate() {
        long timestamp = mPubDate;
        if (timestamp <= 0) {
            final FeedItem item = getItemAt(0);
            timestamp = (null != item) ? item.mPubDate : 1;
        }
        return timestamp;
    }

}
