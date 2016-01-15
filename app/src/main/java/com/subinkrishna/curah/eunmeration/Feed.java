package com.subinkrishna.curah.eunmeration;

import com.subinkrishna.curah.R;

/**
 * Everything about the feeds!
 *
 * @author Subinkrishna Gopi
 */
public enum Feed {

    CURAH_FEATURED("http://curah.microsoft.com/feeds/featured?lang=en",
            "curah_featured.xml",
            R.string.title_curah_featured),
    CURAH_LATEST ("http://curah.microsoft.com/feeds/latest?lang=en",
            "curah_latest.xml",
            R.string.title_curah_latest),
    MICROSOFT_OFFICIAL_BLOG ("http://blogs.microsoft.com/feed/",
            "ms_official_blog.xml",
            R.string.title_ms_news_official_blog),
    LATEST_IN_RESEARCH ("http://research.microsoft.com/rss/latestResearch.xml",
             "latest_in_research.xml",
             R.string.title_ms_latest_in_research),
    RESEARCH_DOWNLOADS ("http://research.microsoft.com/rss/downloads.xml",
             "research_downloads.xml",
             R.string.title_ms_research_downloads );

    /**
     * Find a Feed by id.
     *
     * @param id
     * @return
     */
    public static Feed byId(final int id) {
        final Feed[] feeds = Feed.values();
        for (Feed aFeed : feeds) {
            if (aFeed.mId == id) {
                return aFeed;
            }
        }
        return null;
    }

    public int mId;
    public String mUrl;             // Feed URL
    public String mLocalFilename;   // Filename under which the feed will be saved locally
    public int mTitleResId;         // Feed title 

    private Feed(final String url,
                 final String localFilename,
                 final int titleId) {
        mId = ordinal();
        mUrl = url;
        mLocalFilename = localFilename;
        mTitleResId = titleId;
    }
}
