package com.subinkrishna.curah.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.subinkrishna.curah.CurahApplication;
import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.data.feed.FeedMeta;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.eunmeration.Preferences;
import com.subinkrishna.curah.ui.view.FeedItemView;
import com.subinkrishna.curah.util.Config;
import com.subinkrishna.curah.util.DateUtil;

import java.util.List;

/**
 * BaseAdapter implementation for the feed list.
 *
 * @author Subinkrishna Gopi
 */
public class FeedListAdapter extends BaseAdapter implements View.OnClickListener {

    /** Log tag */
    private static final String TAG = FeedListAdapter.class.getSimpleName();

    private static final int TYPE_CARD = 0;
    private static final int TYPE_WARNING_CARD = 1;

    /**
     * Creates an instance of FeedListAdapter.
     *
     * @param context
     * @return
     */
    public static FeedListAdapter newInstance(final Context context) {
        FeedListAdapter adapter = null;
        if (null != context) {
            adapter = new FeedListAdapter();
            adapter.mContext = context;
        }

        return adapter;
    }

    private boolean mShowDataWarning;
    private Context mContext;
    private FeedMeta mFeedMeta;
    private SparseIntArray mDateMarkers;

    /**
     * Update the adapter data.
     *
     * @param feed
     * @param feedMeta
     * @param notifyDataChange
     * @return
     */
    public FeedListAdapter update(final Feed feed,
                                  final FeedMeta feedMeta,
                                  final boolean notifyDataChange) {
        // Find the today/yesterday/older markers (start indices)
        if (null != feedMeta) {
            feedMeta.sort();
        }

        // Identify the date markers for phone
        mDateMarkers = Config.isTablet(mContext) ? null : markDateIndices(feedMeta);

        SharedPreferences pref = CurahApplication.getPreferences();
        mShowDataWarning = (feed == Feed.CURAH_LATEST) && !pref.getBoolean(Preferences.UnderstoodAboutLatestCurah.label, false);
        mFeedMeta = feedMeta;

        if (notifyDataChange) {
            notifyDataSetChanged();
        }
        return this;
    }

    private SparseIntArray markDateIndices(final FeedMeta feedMeta) {
        // Abort if feed is empty
        if ((null == feedMeta) || (0 == feedMeta.size())) {
            return null;
        }

        final SparseIntArray indices = new SparseIntArray(4);
        final List<FeedItem> items = feedMeta.mFeedItems;
        int index = 0;
        boolean foundToday = false;
        boolean foundYesterday = false;
        boolean foundThisWeek = false;

        for (FeedItem item : items) {
            // Today
            if (!foundToday && DateUtil.isToday(item.mPubDate)) {
                indices.append(index, R.string.today);
                foundToday = true;
            }
            // Yesterday
            else if (!foundYesterday && DateUtil.isYesterday(item.mPubDate)) {
                indices.append(index, R.string.yesterday);
                foundYesterday = true;
            }
            // This week
            else if (!foundThisWeek &&
                     !DateUtil.isToday(item.mPubDate) &&
                     !DateUtil.isYesterday(item.mPubDate) &&
                     DateUtil.isThisWeek(item.mPubDate)) {
                indices.append(index, R.string.this_week);
                foundThisWeek = true;
            }
            // Older
            else if (DateUtil.isOlderThanThisWeek(item.mPubDate)) {
                indices.append(index, R.string.older_than_a_week);
                break;
            }

            index ++;
        }

        return indices;
    }

    @Override
    public int getCount() {
        int count = (null != mFeedMeta) ? mFeedMeta.size() : 0;
        count += mShowDataWarning ? 1 : 0;
        return count;
    }

    @Override
    public FeedItem getItem(int position) {
        if (mShowDataWarning && (position == 0)) {
            return null;
        }

        return (null != mFeedMeta)
                ? mFeedMeta.getItemAt(mShowDataWarning ? position - 1 : position)
                : null;
    }

    @Override
    public int getViewTypeCount() {
        return mShowDataWarning ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return mShowDataWarning && (position == 0) ? TYPE_WARNING_CARD : TYPE_CARD;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        View v = null;

        // Regular card
        if (viewType == TYPE_CARD) {
            FeedItemView feedItemView = null;
            if ((null != convertView) &&
                (convertView instanceof FeedItemView)) {
                // Try to recycle the incoming view
                feedItemView = (FeedItemView) convertView;
            } else {
                // Else, create a new view
                feedItemView = FeedItemView.newInstance(mContext);
            }

            int headerResId = -1;
            FeedItem item = null;
            boolean isTablet = Config.isTablet(mContext);
            if (null != feedItemView) {
                int index = mShowDataWarning ? position - 1 : position;
                headerResId = (null != mDateMarkers) ? mDateMarkers.get(index, -1) : -1;
                item = getItem(position);
                feedItemView.setTag(item);
                feedItemView.recycle(item)                          // Recycle the view with new data item
                        .setDateHeader(isTablet ? -1 : headerResId)     // Show the date marker if this position is in indices
                        .setDivider(isTablet ? 0 : position);           // Set the divider
            }

            v = feedItemView;
        }

        // Warning
        else if (viewType == TYPE_WARNING_CARD) {
            /*
            if ((null != convertView) && (convertView instanceof LinearLayout)) {
                v = convertView;
            } else {
                v = LayoutInflater.from(mContext).inflate(R.layout.item_feedlist_warning_header, null);
            }
            */

            // TODO: Need to create a custom view to fix this
            v = LayoutInflater.from(mContext).inflate(R.layout.item_feedlist_warning_header, null);
            v.findViewById(R.id.close).setOnClickListener(this);
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = CurahApplication.getPreferencesEditor();
        editor.putBoolean(Preferences.UnderstoodAboutLatestCurah.label, true).commit();
        mShowDataWarning = false;
        notifyDataSetChanged();
    }
}
