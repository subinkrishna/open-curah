package com.subinkrishna.curah.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.subinkrishna.curah.R;
import com.subinkrishna.curah.eunmeration.Feed;

import java.util.List;

/**
 * @author Subinkrishna Gopi
 */
public class NavigationListAdapter extends BaseAdapter {

    /**
     * Creates an instance of NavigationListAdapter.
     *
     * @param context
     * @return
     */
    public static NavigationListAdapter newInstance(final Context context) {
        if (null != context) {
            final NavigationListAdapter adapter = new NavigationListAdapter();
            adapter.mContext = context;
            return adapter;
        }
        return null;
    }

    private Context mContext;
    public Feed mSelectedFeed = null;
    private List<NavigationListItem> mItems = null;

    @Override
    public int getCount() {
        return (null != mItems) ? mItems.size() : 0;
    }

    @Override
    public NavigationListItem getItem(int position) {
        return (null != mItems) ? mItems.get(position) : null;
    }

    @Override
    public boolean isEnabled(int position) {
        return (NavigationItemType.Item == getItem(position).mType);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (null != mItems)
                ? ((NavigationItemType.Header == getItem(position).mType) ? 0 : 1)
                : 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final NavigationListItem item = getItem(position);
        View v = null;

        if ((null != convertView) &&
            (convertView instanceof LinearLayout) &&
            (null != convertView.findViewById(R.id.text1))) {
            // Recycle
            v = convertView;
        } else {
            // Create
            final int resId = (NavigationItemType.Header == item.mType)
                    ? R.layout.item_navigation_list_item_header
                    : R.layout.item_navigation_list_item;
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(resId, null, false);
        }

        // Set color & label
        final TextView tv = (TextView) v.findViewById(R.id.text1);
        tv.setText(item.mLabel);
        tv.setTextColor(mContext.getResources().getColor(item.mColor));
        v.setTag(item.mFeed);

        // Highlight the selected item
        tv.setSelected(mSelectedFeed == item.mFeed);

        return v;
    }

    public void update(final List<NavigationListItem> items,
                       final Feed selectedFeed,
                       final boolean notifyDataSetChange) {
        // Abort if input is invalid
        if (null == items) {
            return;
        }

        mItems = items;
        mSelectedFeed = selectedFeed;
        if (notifyDataSetChange) {
            notifyDataSetChanged();
        }
    }

    public void updateSelection(final Feed selectedFeed) {
        if ((null != selectedFeed) && (mSelectedFeed != selectedFeed)) {
            mSelectedFeed = selectedFeed;
            notifyDataSetChanged();
        }
    }

    //---------- Holds the list item data ------------

    public static enum NavigationItemType { Header, Item }

    public static class NavigationListItem {

        /**
         * Creates a new instance of NavigationListItem
         *
         * @param label
         * @param colorResourceId
         * @param feed
         * @param type
         * @return
         */
        public static NavigationListItem newInstance(final String label,
                                                     final int colorResourceId,
                                                     final Feed feed,
                                                     final NavigationItemType type) {
            final NavigationListItem item = new NavigationListItem();
            item.mLabel = label;
            item.mColor = colorResourceId;
            item.mFeed = feed;
            item.mType = type;
            return item;
        }

        public String mLabel;
        public int mColor;
        public Feed mFeed;
        public NavigationItemType mType;
    }

}
