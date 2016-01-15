package com.subinkrishna.curah.data.feed;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Subinkrishna Gopi
 */
public class FeedItem implements Comparable<FeedItem>, Serializable {

    public String mTitle;
    public String mDescription;
    public String mAuthor;
    public String mLink;
    public long mPubDate;
    public Set<String> mTags; // Ignore now

    public FeedItem addTag(final String tag) {
        if (!TextUtils.isEmpty(tag)) {
            mTags = (null == mTags) ? new HashSet<String>() : mTags;
            mTags.add(tag);
        }
        return this;
    }

    @Override
    public int compareTo(FeedItem another) {
        if (null == another) return 1;
        int r = (this != another) ? ((mPubDate >= another.mPubDate) ? -1 : 1) : 0;
        return r;
    }

    @Override
    public String toString() {
        return String.format("{ %s, %s }", mTitle, mAuthor);
    }
}
