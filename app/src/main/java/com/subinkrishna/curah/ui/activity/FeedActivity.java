package com.subinkrishna.curah.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.subinkrishna.curah.CurahApplication;
import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.sync.SyncAdapter;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.ui.fragment.FeedListFragment;
import com.subinkrishna.curah.ui.fragment.NavigationFragment;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * Feed Activity implementation.
 *
 * @author Subinkrishna Gopi
 */
public class FeedActivity
        extends ActionBarActivity
        implements NavigationFragment.OnFeedChangeListener {

    /** Log tag */
    private static final String TAG = FeedActivity.class.getSimpleName();

    /** Key to hold the current selected navigation item */
    private static final String KEY_CURRENT_FEED_ID = "key.currentFeedId";

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Feed mCurrentFeed;
    private NavigationFragment mNavigationFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mCurrentFeed = (null != savedInstanceState)
                ? Feed.byId(savedInstanceState.getInt(KEY_CURRENT_FEED_ID, CurahApplication.DefaultFeed.mId))
                : CurahApplication.DefaultFeed;

        // Set up the view
        configureView(savedInstanceState);
        configureToolbar(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start the new sync to fetch the latest content ONLY IF no sync is in progress and
        // the last sync was not with in the interval
        //
        // NOTE: This uses a different (shorter) sync interval
        if (!SyncAdapter.isLastSyncWithinInterval(SyncAdapter.SYNC_INTERVAL_IN_SECONDS_WHEN_ACTIVE) &&
            !SyncAdapter.isSyncActive(this, SyncAdapter.getSyncAccount(this, false))) {
            SyncAdapter.syncNow(FeedActivity.this);
        } else {
            e(TAG, "Abort expedited-sync. Last sync was within 1 hour.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_FEED_ID, mCurrentFeed.mId);
    }

    private void configureToolbar(final Bundle savedInstanceState) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this,  mDrawer, toolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mNavigationFragment.onNetworkStateChange();
            }
        };
        mDrawer.setDrawerListener(mDrawerToggle);
    }

    /**
     * Configure the view.
     *
     * @param savedInstanceState
     */
    private void configureView(final Bundle savedInstanceState) {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);

        final FragmentManager fm = getSupportFragmentManager();

        if (null == savedInstanceState) {
            d(TAG, "Set the fragments");
            final FeedListFragment feedListFragment = FeedListFragment.newInstance(mCurrentFeed);
            mNavigationFragment = new NavigationFragment();

            final FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.feed_list_container, feedListFragment)
                    .add(R.id.navigation_container, mNavigationFragment)
                    .commit();
        } else {
            mNavigationFragment = (NavigationFragment) fm.findFragmentById(R.id.navigation_container);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
        }
        // android.R.id.home - navigation icon
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(Gravity.LEFT)) {
            mDrawer.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFeedChange(Feed to) {
        // Close navigation drawer
        mDrawer.closeDrawer(Gravity.LEFT);

        // Add new feed fragment if selection has changed
        if ((null != to) && (to != mCurrentFeed)) {
            d(TAG, "Feed change to: " + to);
            mCurrentFeed = to;

            // Delay fragment replacement to avoid jank
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final FeedListFragment feedListFragment = FeedListFragment.newInstance(mCurrentFeed);
                    final FragmentManager fm = getSupportFragmentManager();
                    final FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.feed_list_container, feedListFragment)
                            .commit();
                }
            }, getResources().getInteger(android.R.integer.config_shortAnimTime));
        }
    }
}
