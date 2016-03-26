package com.subinkrishna.curah.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.data.feed.FeedMeta;
import com.subinkrishna.curah.data.loader.FileSystemFeedLoader;
import com.subinkrishna.curah.data.sync.SyncAdapter;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.ui.activity.FeedDetailActivity;
import com.subinkrishna.curah.ui.adapter.FeedListAdapter;
import com.subinkrishna.curah.ui.widget.FlyInListView;
import com.subinkrishna.curah.util.Config;

import static com.subinkrishna.curah.util.Logger.d;

/**
 * @author Subinkrishna Gopi
 */
public class FeedListFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<FeedMeta>,
                   View.OnClickListener,
                   AdapterView.OnItemClickListener {

    /** Log tag */
    private static final String TAG = FeedListFragment.class.getSimpleName();

    /** Keys to hold the list state */
    private static final String KEY_LIST_STATE = "key.listState";
    /** Holds current Feed ID */
    private static final String KEY_FEED_ID = "keyFeedId";
    /** Holds the list animation status */
    private static final String KEY_STOP_ANIMATION = "keyStopAnimate";

    /**
     * Creates a new instance of the FeedListFragment.
     *
     * @param feed
     * @return
     */
    public static FeedListFragment newInstance(final Feed feed) {
        if (null != feed) {
            final FeedListFragment fragment = new FeedListFragment();
            // Set arguments
            final Bundle args = new Bundle();
            args.putInt(KEY_FEED_ID, feed.mId);
            fragment.setArguments(args);
            return fragment;
        }

        return null;
    }

    private Feed mFeed;
    private View mProgressBar;
    private TextView mMessageTextView;
    private TextView mFirstSyncMessage;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFeedUpdateNotifier;
    private AbsListView mList;
    private FeedListAdapter mAdapter;

    private boolean mListPositionRestored = false;
    private Parcelable mListState = null;

    private FeedMeta mData = null;
    private boolean mStopAnimation = false;

    private boolean isTablet = false;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_list, container, false);
    }

    @Override
    public void onViewCreated(final View view,
                              final Bundle savedInstanceState) {
        final Activity parent = getActivity();
        isTablet = Config.isTablet(parent);

        // Get the arguments
        final Bundle args = getArguments();
        final int feedId = args.getInt(KEY_FEED_ID, -1);
        mFeed = Feed.byId(feedId);

        if (null != savedInstanceState) {
            mStopAnimation = savedInstanceState.getBoolean(KEY_STOP_ANIMATION, false);
            mListState = savedInstanceState.getParcelable(KEY_LIST_STATE);
        }

        // Set up the views
        mFeedUpdateNotifier = view.findViewById(R.id.feed_update_notifier);
        mMessageTextView = (TextView) view.findViewById(R.id.message);
        mFirstSyncMessage = (TextView) view.findViewById(R.id.first_sync_message);
        mProgressBar = view.findViewById(R.id.progressbar);
        mList = (AbsListView) view.findViewById(R.id.list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.feed_container);

        // TODO:
        mSwipeRefreshLayout.setEnabled(false);

        if (null != mFeed) {
            showProgressBar();

            mAdapter = FeedListAdapter.newInstance(parent);
            mList.setAdapter(mAdapter);
            mList.setOnItemClickListener(this);
            parent.setTitle(getString(mFeed.mTitleResId));
        } else {
            showMessage(R.string.error_unknown);
            parent.setTitle("");
        }

        // Attach the sync status receiver
        registerStatusReceiver();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_STOP_ANIMATION, mStopAnimation);
        // Preserve list state
        if (null != mList) {
            outState.putParcelable(KEY_LIST_STATE, mList.onSaveInstanceState());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load the data
        // NOTE: forceLoad() should be called from onStart() because the Callback
        // gets attached to the Fragment only after the fragment is started. Else,
        // onLoadFinished() may not get called.
        if (null != mFeed) {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Unregister the status receiver
        unregisterStatusReceiver();
    }

    @Override
    public Loader<FeedMeta> onCreateLoader(int id, Bundle args) {
        d(TAG, "OnCreateLoader() feed id: " + id);
        return FileSystemFeedLoader.newInstance(getActivity(), mFeed);
    }

    @Override
    public void onLoadFinished(Loader<FeedMeta> loader, FeedMeta data) {
        // TO avoid getting called twice
        getLoaderManager().destroyLoader(0);

        final Activity parent = getActivity();
        int count = (null != data) ? data.size() : 0;
        d(TAG, "Load finished. Feed: " + mFeed.name()  + " list size: " + count);

        final boolean isFirstSyncCompleted = SyncAdapter.getLastSyncTime() > -1;
        final boolean isNetworkAvailable = Config.isOnline(parent);

        if ((count <= 0) &&
            (mAdapter.getCount() == 0)) {
            // Show error message if sync adapter finished it's fetch and
            // if no data available. Else wait for the sync adapter to
            // send the sync complete notification.
            if (isFirstSyncCompleted || !isNetworkAvailable) {
                showMessage(!isNetworkAvailable
                        ? R.string.error_empty_feed_network
                        : R.string.error_empty_feed);
            }
        } else {
            // TODO: Remove after testing
            d(TAG, "Feed date: " + data.getPublishDate() + " (Now: " + System.currentTimeMillis() + ")");

            // TODO: Check if the incoming data's publish date is latest than mData.
            // If yes, show bubble, else ignore.

            showFeeds();

            // TODO: Only for phone, as of now
            if (!isTablet && !(mList instanceof StaggeredGridView)) {
                // Enable view animation if the view is not already animated
                ((FlyInListView)mList).enableAnimations(!mStopAnimation);
            }

            if (count > 0) {
                mAdapter.update(mFeed, data, true);
                // Disable future animations on receiving valid data
                mStopAnimation = true;
                // TODO: Show feed update notifier
                mData = data;
            }

            if (!mListPositionRestored && (null != mListState)) {
                d(TAG, "Restore list position");
                mList.onRestoreInstanceState(mListState);
                mListState = null;
            }
        }

        mListPositionRestored = true;
    }

    @Override
    public void onLoaderReset(Loader<FeedMeta> loader) {
        //d(TAG, "Loader reset");
    }

    private void showFeeds() {
        mList.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mFirstSyncMessage.setVisibility(View.GONE);
        mMessageTextView.setVisibility(View.GONE);
    }

    private void showMessage(final int resId) {
        mList.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mFirstSyncMessage.setVisibility(View.GONE);
        mMessageTextView.setText(resId);
        mMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        final boolean firstSync = SyncAdapter.getLastSyncTime() == -1;
        mFirstSyncMessage.setVisibility(firstSync ? View.VISIBLE : View.GONE);
        mList.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mMessageTextView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
    }

    private void registerStatusReceiver() {
        d(TAG, "Register sync status receiver");
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SyncAdapter.KEY_SYNC_START_ACTION);
        filter.addAction(SyncAdapter.KEY_SYNC_FEED_COMPLETE_ACTION);
        filter.addAction(SyncAdapter.KEY_SYNC_COMPLETE_ACTION);
        getActivity().registerReceiver(mSyncStatusReceiver, filter);
    }

    private void unregisterStatusReceiver() {
        d(TAG, "Unregister sync status receiver");
        getActivity().unregisterReceiver(mSyncStatusReceiver);
    }

    /**
     * BroadcastReceiver to receive sync status notifications.
     */
    private BroadcastReceiver mSyncStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context,
                              final Intent intent) {
            final String action = intent.getAction();
            if (SyncAdapter.KEY_SYNC_FEED_COMPLETE_ACTION.equalsIgnoreCase(action)) {
                final int feedId = intent.getIntExtra(SyncAdapter.KEY_SYNC_FEED_ID, -1);
                // Ask the loader manager to reload if feed ID matches.
                if (feedId == mFeed.mId) {
                    d(TAG, "Received sync completed notification. Load the contents.");
                    getLoaderManager().initLoader(0, null, FeedListFragment.this).forceLoad();
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tag = (null != view) ? view.getTag() : null;
        FeedItem item = ((null != tag) && (tag instanceof FeedItem))
                ? (FeedItem) view.getTag() : null;

        if (null == item) return;

        Intent intent = null;
        Activity activity = getActivity();

        if (isTablet) {
            FragmentManager fm = getFragmentManager();
            // TODO: Cache the dialog?
            FeedDetailsFragment.newInstance(item, true).show(fm, "details");
        } else {
            intent = new Intent(activity, FeedDetailActivity.class);
            // TODO: Serializable is not preferred, need to use Parcelable
            intent.putExtra(FeedDetailActivity.KEY_ITEM, item);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        }
    }
}
