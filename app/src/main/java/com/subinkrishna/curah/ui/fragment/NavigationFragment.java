package com.subinkrishna.curah.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.subinkrishna.curah.CurahApplication;
import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.sync.SyncAdapter;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.ui.adapter.NavigationListAdapter;
import com.subinkrishna.curah.ui.adapter.NavigationListAdapter.NavigationItemType;
import com.subinkrishna.curah.ui.adapter.NavigationListAdapter.NavigationListItem;
import com.subinkrishna.curah.util.Config;
import com.subinkrishna.curah.util.DateUtil;

import java.util.LinkedList;
import java.util.List;

import static com.subinkrishna.curah.util.Logger.d;

/**
 * Navigation fragment (for Nav drawer)
 *
 * @author Subinkrishna Gopi
 */
public class NavigationFragment
        extends Fragment
        implements AdapterView.OnItemClickListener {

    public static interface OnFeedChangeListener {
        public void onFeedChange(Feed to);
    }

    /** Log tag */
    private static final String TAG = NavigationFragment.class.getSimpleName();

    /** Key to hold the current selected navigation item */
    private static final String KEY_SELECTED_FEED_ID = "key.selectedFeedId";
    private static final String KEY_IS_SYNC_IN_PROGRESS = "key.syncInProgress";

    private ListView mList;
    private NavigationListAdapter mAdapter;
    private View mRefreshItem;
    private OnFeedChangeListener mFeedChangeListener;
    private Feed mCurrentSelectedFeed;
    private boolean mIsSyncInProgress;
    private Toast mSyncInProgressToast;

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        final Activity parent = getActivity();

        // Attach network state receiver
        final IntentFilter networkStateFilter = new IntentFilter();
        networkStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        parent.registerReceiver(mNetworkStateReceiver, networkStateFilter);

        // Attach sync state receiver
        final IntentFilter syncStateFilter = new IntentFilter();
        syncStateFilter.addAction(SyncAdapter.KEY_SYNC_START_ACTION);
        syncStateFilter.addAction(SyncAdapter.KEY_SYNC_COMPLETE_ACTION);
        parent.registerReceiver(mSyncStateReceiver, syncStateFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Activity parent = getActivity();

        // Detach receivers
        parent.unregisterReceiver(mNetworkStateReceiver);
        parent.unregisterReceiver(mSyncStateReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        final Activity parent = getActivity();

        if (parent instanceof OnFeedChangeListener) {
            mFeedChangeListener = (OnFeedChangeListener) parent;
        }

        mCurrentSelectedFeed = (null != savedInstanceState)
                ? Feed.byId(savedInstanceState.getInt(KEY_SELECTED_FEED_ID, CurahApplication.DefaultFeed.mId))
                : CurahApplication.DefaultFeed;
        mIsSyncInProgress = (null != savedInstanceState)
                ? savedInstanceState.getBoolean(KEY_IS_SYNC_IN_PROGRESS, false) : false;

        mList = (ListView) view.findViewById(R.id.list);
        mAdapter = NavigationListAdapter.newInstance(parent);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mAdapter.update(getListItems(), mCurrentSelectedFeed, true);

        mRefreshItem = view.findViewById(R.id.refresh_button);

        mRefreshItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isOnline(parent)) {
                    // Force a manual sync if no sync is active or pending
                    if (!SyncAdapter.isSyncActiveOrPending(parent, SyncAdapter.getSyncAccount(parent, false))) {
                        SyncAdapter.syncNow(parent);
                    } else {
                        if (null != mSyncInProgressToast) {
                            mSyncInProgressToast.cancel();
                        }
                        mSyncInProgressToast = Toast.makeText(parent, R.string.sync_in_progress, Toast.LENGTH_SHORT);
                        mSyncInProgressToast.show();
                    }
                }
            }
        });

        onNetworkStateChange();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_FEED_ID, mCurrentSelectedFeed.mId);
        outState.putBoolean(KEY_IS_SYNC_IN_PROGRESS, mIsSyncInProgress);
    }

    /**
     * Builds the navigation list items.
     *
     * @return
     */
    private List<NavigationListItem> getListItems() {
        final List<NavigationListItem> items = new LinkedList<NavigationListItem>();
        final Resources res = getResources();

        // MS Blog & News
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_header_ms_news),
                android.R.color.holo_purple,
                null,
                NavigationItemType.Header));
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_item_ms_news_official_blog),
                R.color.text_primary,
                Feed.MICROSOFT_OFFICIAL_BLOG,
                NavigationItemType.Item));
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_item_ms_latest_in_research),
                R.color.text_primary,
                Feed.LATEST_IN_RESEARCH,
                NavigationItemType.Item));
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_item_ms_research_downloads),
                R.color.text_primary,
                Feed.RESEARCH_DOWNLOADS,
                NavigationItemType.Item));

        // Curah
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_header_curah),
                android.R.color.holo_blue_dark,
                null,
                NavigationItemType.Header));
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_item_curah_featured),
                R.color.text_primary,
                Feed.CURAH_FEATURED,
                NavigationItemType.Item));
        items.add(NavigationListItem.newInstance(res.getString(R.string.nav_item_curah_latest),
                R.color.text_primary,
                Feed.CURAH_LATEST,
                NavigationItemType.Item));

        return items;
    }

    private void onSyncStart() {
        d(TAG, "onSyncStart");
        mIsSyncInProgress = true;

        final TextView lastRefreshTextView = (TextView) mRefreshItem.findViewById(R.id.last_refresh_time);
        lastRefreshTextView.setText(R.string.sync_in_progress);
        lastRefreshTextView.setVisibility(View.VISIBLE);

        final ProgressBar progressBar = (ProgressBar) mRefreshItem.findViewById(R.id.refresh_progress_indicator);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSyncComplete() {
        d(TAG, "onSyncComplete");
        mIsSyncInProgress = false;

        final ProgressBar progressBar = (ProgressBar) mRefreshItem.findViewById(R.id.refresh_progress_indicator);
        progressBar.setVisibility(View.GONE);

        onNetworkStateChange();
    }

    /**
     * Update the sync time and network state on network state change.
     */
    public void onNetworkStateChange() {
        final boolean isNetworkAvailable = Config.isOnline(getActivity());
        d(TAG, "Network state change. Available? " + isNetworkAvailable);

        // Set the time
        final TextView lastRefreshTextView = (TextView) mRefreshItem.findViewById(R.id.last_refresh_time);
        final long timestamp = SyncAdapter.getLastSyncTime();

        // Sync in progress
        if (mIsSyncInProgress) {
            lastRefreshTextView.setText(R.string.sync_in_progress);
            lastRefreshTextView.setVisibility(View.VISIBLE);
        }
        // Last sync time available
        else if (timestamp > -1) {
            String formattedDate = DateUtil.formatDate(timestamp, true);
            final String lastRefreshTime = String.format("%s %s",
                    getString(R.string.last_updated),
                    formattedDate);
            lastRefreshTextView.setText(lastRefreshTime);
            lastRefreshTextView.setVisibility(View.VISIBLE);
        }
        // Else
        else {
            // Hide last refresh time if initial sync hasn't happened yet
            lastRefreshTextView.setVisibility(View.GONE);
        }

        // Set the online/offline status
        final View offlineIndicatorView = mRefreshItem.findViewById(R.id.offline_indicator);
        offlineIndicatorView.setVisibility(isNetworkAvailable ? View.GONE : View.VISIBLE);
    }

    /** Network state change receiver */
    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNetworkStateChange();
        }
    };

    /** Sync state receiver */
    private BroadcastReceiver mSyncStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SyncAdapter.KEY_SYNC_START_ACTION.equalsIgnoreCase(action)) {
                onSyncStart();
            } else if (SyncAdapter.KEY_SYNC_COMPLETE_ACTION.equalsIgnoreCase(action)) {
                onSyncComplete();
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
        final Feed feed = (null != view) ? (Feed) view.getTag() : null;
        if (null != feed) {
            mCurrentSelectedFeed = feed;
            mAdapter.updateSelection(feed);
            if (null != mFeedChangeListener) {
                mFeedChangeListener.onFeedChange(feed);
            }
        }
    }
}
