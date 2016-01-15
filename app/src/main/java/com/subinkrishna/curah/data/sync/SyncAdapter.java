package com.subinkrishna.curah.data.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;

import com.subinkrishna.curah.CurahApplication;
import com.subinkrishna.curah.data.fetcher.FeedFetcher;
import com.subinkrishna.curah.data.fetcher.FeedFetcher.FetchStatus;
import com.subinkrishna.curah.eunmeration.Feed;

import java.util.Calendar;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * Sync Adapter.
 *
 * @author Subinkrishna Gopi
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /** Log Tag */
    private static final String TAG = SyncAdapter.class.getSimpleName();

    /** Authority & account type */
    public static final String AUTHORITY = "com.subinkrishna.curah.provider";
    public static final String ACCOUNT_TYPE = "curah.subinkrishna.com";

    /** Keys to hold the sync notification flags / actions */
    public static final String KEY_SYNC_START_ACTION = "com.subinkrishna.curah.sync.start";
    public static final String KEY_SYNC_COMPLETE_ACTION = "com.subinkrishna.curah.sync.complete";
    public static final String KEY_SYNC_FEED_COMPLETE_ACTION = "com.subinkrishna.curah.sync.feed.complete";
    public static final String KEY_SYNC_FEED_ID = "com.subinkrishna.curah.sync.feedId";
    public static final String KEY_SYNC_FETCH_STATUS = "com.subinkrishna.curah.sync.fetchStatus";

    /** Sync interval */
    public static final int SYNC_INTERVAL_IN_SECONDS = 60 * 60 * 12; // Twelve hours
    public static final int SYNC_INTERVAL_IN_SECONDS_WHEN_ACTIVE = 60 * 60; // One hour

    /** Default account name & password */
    private static final String DEFAULT_ACCOUNT_USERNAME = "Sync";
    private static final String DEFAULT_ACCOUNT_PASSWORD = null;

    /** Key to hold the feed ID */
    private static final String KEY_FEED_ID = "keyFeedId";
    /** Key to hold the last sync time */
    private static final String KEY_LAST_SYNC_TIME = "keyLastSyncTime";

    /**
     * Initiates an immediate sync. Syncs all the feeds.
     *
     * @param context
     */
    public static void syncNow(final Context context) {
        syncNow(context, null);
    }

    /**
     * Initiates an immediate sync on the specified feed.
     *
     * @param context
     * @param feed
     */
    public static void syncNow(final Context context,
                               final Feed feed) {
        // Abort if context is invalid
        if (null == context) {
            e(TAG, "Aborting manual sync. Invalid context.");
            return;
        }

        final Account account = getSyncAccount(context, true);

        if (null == account) {
            e(TAG, "Aborting manual sync. Invalid sync account.");
            return;
        }

        // Cancel active/pending sync before starting new sync
        if (isSyncActiveOrPending(context, account)) {
            d(TAG, "Cancelling active/pending sync");
            cancelSync(context, account);
        }

        // Perform sync now (override all settings)
        d(TAG, "Start manual sync: " + ((null != feed) ? feed.name() : "all"));
        final Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putInt(KEY_FEED_ID, (null != feed) ? feed.mId : -1);
        ContentResolver.requestSync(account, AUTHORITY, extras);
    }

    /**
     * Checks if a sync is active or pending
     *
     * @param context
     * @param account
     * @return
     */
    public static boolean isSyncActiveOrPending(Context context,
                                                Account account) {
        boolean isActiveOrPendingSync = false;
        if ((null != context) && (null != account)) {
            isActiveOrPendingSync = ContentResolver.isSyncPending(account, AUTHORITY) ||
                    ContentResolver.isSyncActive(account, AUTHORITY);
        }
        return isActiveOrPendingSync;
    }

    /**
     * Checks if a sync is active.
     *
     * @param context
     * @param account
     * @return
     */
    public static boolean isSyncActive(Context context,
                                       Account account) {
        boolean isActive = false;
        if ((null != context) && (null != account)) {
            isActive = ContentResolver.isSyncActive(account, AUTHORITY);
        }
        return isActive;
    }

    /**
     * Enables periodic sync for the account.
     *
     * @param context
     */
    public static void enablePeriodicSync(final Context context) {
        if (null == context) {
            e(TAG, "Aborting enable periodic sync. Invalid context.");
            return;
        }

        final Account syncAccount = getSyncAccount(context, true);
        if (null == syncAccount) {
            e(TAG, "Aborting enable periodic sync. Invalid sync account.");
            return;
        }

        d(TAG, "Enable periodic sync");
        final Bundle extras = new Bundle();
        // Make the account syncable
        ContentResolver.setIsSyncable(syncAccount, AUTHORITY, 1);
        // Remove existing periodic syncs
        ContentResolver.removePeriodicSync(syncAccount, AUTHORITY, extras);
        // Add new periodic sync
        ContentResolver.addPeriodicSync(syncAccount, AUTHORITY, extras, SYNC_INTERVAL_IN_SECONDS);
    }

    /**
     * Cancels the sync on the specified account.
     *
     * @param context
     * @param account
     */
    public static void cancelSync(final Context context,
                                  final Account account) {
        if ((null == context) || (null == account)) {
            d(TAG, "Abort cancel sync. Invalid context/account.");
            return;
        }

        ContentResolver.cancelSync(account, AUTHORITY);
    }

    /**
     * Returns the sync account. Will create a new sync account if there is no existing account
     * based on {@code createIfNotExists}.
     *
     * @param context
     * @param createIfNotExists
     * @return
     */
    public static Account getSyncAccount(final Context context,
                                         final boolean createIfNotExists) {
        d(TAG, "Get sync account");

        final AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        Account syncAccount = null;

        // Add an account if no accounts exist
        if (((null == accounts) || (0 == accounts.length)) && createIfNotExists) {
            d(TAG, "Create sync account");

            // Create the default account and add it explicitly
            syncAccount = new Account(DEFAULT_ACCOUNT_USERNAME, ACCOUNT_TYPE);
            boolean hasAdded = accountManager.addAccountExplicitly(syncAccount,
                    DEFAULT_ACCOUNT_PASSWORD, // Password (empty)
                    null);                    // User info bundle (empty)

            if (hasAdded) {
                // Make the account syncable
                ContentResolver.setIsSyncable(syncAccount, AUTHORITY, 1);
                // Turn on the auto-sync
                ContentResolver.setSyncAutomatically(syncAccount, AUTHORITY, true);
            }
        }
        // Account exists
        else {
            syncAccount = accounts[0];
        }

        return syncAccount;
    }

    /**
     * Returns the last sync time.
     *
     * @return
     */
    synchronized
    public static long getLastSyncTime() {
        return CurahApplication.getPreferences().getLong(KEY_LAST_SYNC_TIME, -1);
    }

    /**
     * Updates the last sync time to current time.
     */
    synchronized
    private static void updateLastSyncTime() {
        final SharedPreferences.Editor editor = CurahApplication.getPreferencesEditor();
        editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis()).commit();
    }

    /**
     * Checks if the last sync happened with in the specified interval.
     *
     * @param intervalInSeconds
     * @return
     */
    synchronized
    public static boolean isLastSyncWithinInterval(final int intervalInSeconds) {
        final long lastSyncTime = getLastSyncTime();
        if (-1 == lastSyncTime)
            return false;

        final Calendar now = Calendar.getInstance();
        final Calendar lastSync = Calendar.getInstance();
        lastSync.setTimeInMillis(lastSyncTime);
        lastSync.add(Calendar.SECOND, intervalInSeconds);

        d(TAG, "isLastSyncWithinInterval - Now: " + now.getTimeInMillis()
                + ", Last Sync: " + lastSync.getTimeInMillis()
                + " (Interval: " + intervalInSeconds + ")"
                + " = " + !lastSync.before(now));

        return !lastSync.before(now);
    }

    /**
     * Creates SyncAdapter.
     *
     * @param context
     * @param autoInitialize
     */
    public SyncAdapter(Context context,
                       boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Creates SyncAdapter.
     *
     * @param context
     * @param autoInitialize
     * @param allowParallelSyncs
     */
    public SyncAdapter(Context context,
                       boolean autoInitialize,
                       boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {
        // Abort if user has not agreed to terms yet
        if (!CurahApplication.hasUserAgreedTerms()) {
            e(TAG, "Aborting sync. User hasn't agreed to terms yet!");
            return;
        }

        // Check if the sync is for single feed or all
        final int feedId = extras.getInt(KEY_FEED_ID, -1);
        final boolean isManualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean isExpedited = extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
        final Context context = getContext();

        d(TAG, String.format("onPerformSync() - Feed ID: %d, Manual: %s, Expedited: %s, SyncInProgress (SyncResult): %s",
                feedId,
                String.valueOf(isExpedited),
                String.valueOf(isManualSync),
                String.valueOf(syncResult.syncAlreadyInProgress)));

        // Abort if the last periodic-sync was within 1 hour
        if (!isManualSync && isLastSyncWithinInterval(SYNC_INTERVAL_IN_SECONDS_WHEN_ACTIVE)) {
            e(TAG, "Aborting auto-sync. Last auto-sync was with in 1 hour.");
            return;
        }

        try {
            // Send sync start notification
            sendSyncStartNotification();

            final Feed feed = Feed.byId(feedId);
            FetchStatus status = null;

            // Feed specific sync
            if (null != feed) {
                status = FeedFetcher.fetch(context, feed);
                // TODO: Update the SyncResult
                sendSyncCompleteNotification(feed, status);
            }
            // Complete sync
            else {
                final Feed[] allFeeds = Feed.values();
                for (Feed aFeed : allFeeds) {
                    status = FeedFetcher.fetch(context, aFeed);
                    // TODO: Update the SyncResult
                    sendSyncCompleteNotification(aFeed, status);
                }
            }

            updateLastSyncTime();
        } catch (Exception e) {
            e(TAG, "Exception during sync", e);
        } finally {
            // Re-enable periodic sync if the current sync was user initiated
            if (isManualSync) {
                d(TAG, "Re-enabling periodic sync");
                enablePeriodicSync(context);
            }
            sendSyncCompleteNotification();
        }

        d(TAG, "Finished Sync");
    }

    /**
     * Send sync complete notification.
     */
    private void sendSyncStartNotification() {
        final Intent notificationIntent = new Intent();
        notificationIntent.setAction(KEY_SYNC_START_ACTION);
        getContext().sendBroadcast(notificationIntent);
    }

    /**
     * Send sync complete notification.
     */
    private void sendSyncCompleteNotification() {
        final Intent notificationIntent = new Intent();
        notificationIntent.setAction(KEY_SYNC_COMPLETE_ACTION);
        getContext().sendBroadcast(notificationIntent);
    }

    /**
     * Send feed specific sync status notification.
     *
     * @param feed
     * @param status
     */
    private void sendSyncCompleteNotification(final Feed feed,
                                              final FetchStatus status) {
        if ((null == feed) ||
            (null == status)) {
            e(TAG, "Unable to send fetch status notification. Invalid input.");
            return;
        }

        final Intent notificationIntent = new Intent();
        notificationIntent.setAction(KEY_SYNC_FEED_COMPLETE_ACTION);
        notificationIntent.putExtra(KEY_SYNC_FEED_ID, feed.mId);
        notificationIntent.putExtra(KEY_SYNC_FETCH_STATUS, status.isSuccess);
        getContext().sendBroadcast(notificationIntent);
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        d(TAG, "onSyncCancel()");
    }
}
