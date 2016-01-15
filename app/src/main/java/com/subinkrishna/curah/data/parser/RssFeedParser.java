package com.subinkrishna.curah.data.parser;

import android.text.TextUtils;

import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.data.feed.FeedMeta;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * RSS Feed Parser.
 *
 * @author Subinkrishna Gopi
 */
public class RssFeedParser {

    /** Log Tag */
    private static final String TAG = RssFeedParser.class.getSimpleName();

    /** Date format */
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    public static FeedMeta parse(final String feed) {
        // Abort if feed XML is invalid.
        if (TextUtils.isEmpty(feed)) {
            e(TAG, "Abort parsing. Empty feed XML.");
            return null;
        }

        FeedMeta feedMeta = null;

        try {
            final XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            final XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(new ByteArrayInputStream(feed.getBytes()), "utf-8");

            // Abort if the start-tag is not "rss"
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, parser.getNamespace(), "rss");

            boolean isInsideItem = false;
            String tagName = null;
            FeedItem item = null;
            feedMeta = new FeedMeta();

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        isInsideItem = tagName.equalsIgnoreCase("item");
                        // Parse item if inside "item" tag
                        if (isInsideItem) {
                            item = parseItem(parser);
                            feedMeta.addItem(item);
                            //d(TAG, "Item: " + item);
                        }
                        // Else look for the meta data
                        else {
                            if ("title".equalsIgnoreCase(tagName))
                                feedMeta.mFeedTitle = getText(parser, tagName);
                            else if ("lastBuildDate".equalsIgnoreCase(tagName))
                                feedMeta.mPubDate = dateStringToTimestamp(getText(parser, tagName));
                        }
                        break;
                }

            }
        } catch (Exception e) {
            e(TAG, "XMLPullParser exception", e);
            feedMeta = null;
        }

        return feedMeta;
    }

    private static FeedItem parseItem(final XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Check if start-tag matches "item"
        final String ns = parser.getNamespace();
        parser.require(XmlPullParser.START_TAG, ns, "item");

        String tagName = null;
        FeedItem item = new FeedItem();

        try {
            // Iterate until "item" ends
            while (! ((parser.next() == XmlPullParser.END_TAG) && "item".equalsIgnoreCase(parser.getName()))) {
                //parser.nextTag();
                tagName = parser.getName();

                switch (parser.getEventType()) {
                    case XmlPullParser.START_TAG:
                        // Title
                        if ("title".equalsIgnoreCase(tagName))
                            item.mTitle = getText(parser, tagName);
                        // Link
                        else if ("link".equalsIgnoreCase(tagName))
                            item.mLink = getText(parser, tagName);
                        // Description
                        else if ("description".equalsIgnoreCase(tagName))
                            item.mDescription = getText(parser, tagName);
                        // Author
                        else if ("author".equalsIgnoreCase(tagName) ||
                                 "creator".equalsIgnoreCase(tagName))
                            item.mAuthor = getText(parser, tagName);
                        // PubDate
                        else if ("pubDate".equalsIgnoreCase(tagName))
                            item.mPubDate = dateStringToTimestamp(getText(parser, tagName));

                        break;
                }
            }
        } catch (Exception e) {
            e(TAG, "Exception while parsing item!", e);
            item = null;
        }

        return item;
    }

    /**
     * Read the text from the parser. Assumes the current tag is not a group.
     *
     * @param parser
     * @param tagName
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getText(final XmlPullParser parser, final String tagName)
            throws XmlPullParserException, IOException {
        // Check if start-tag matches tagName
        final String ns = parser.getNamespace();
        parser.require(XmlPullParser.START_TAG, ns, tagName);

        String text = null;
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            // Move to next. Assuming it's the end-tag
            parser.nextTag();
        }

        // Check if next tag is end-tag and it matches start-tag
        parser.require(XmlPullParser.END_TAG, ns, tagName);

        return text;
    }

    /**
     * Convert date string to timestamp.
     *
     * @param date
     * @return
     */
    private static long dateStringToTimestamp(final String date) {
        // Abort if incoming date string is invalid
        if (TextUtils.isEmpty(date))
            return -1;

        long timestamp = -1;

        try {
            // Hack to fix the "z" issue in Curah feeds ("Mon, 04 Aug 2014 20:37:43 z")
            String modifiedDateStr = date;
            final int length = date.length();
            final char lastChar = date.charAt(length - 1);
            if ((lastChar == 'z') || (lastChar == 'Z')) {
                modifiedDateStr = date.substring(0, length - 1) + "+0000";
            }

            final Date aDate = sDateFormat.parse(modifiedDateStr);
            timestamp = aDate.getTime();
        } catch (Exception e) {
            e(TAG, "Exception while converting date string to timestamp", e);
        }

        return timestamp;
    }

}
