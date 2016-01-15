package com.subinkrishna.curah;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.AdapterView;

import com.subinkrishna.curah.ui.activity.FeedActivity;
import com.subinkrishna.curah.ui.fragment.WelcomeNoteFragment;

import static com.subinkrishna.curah.util.Logger.d;

public class MainActivity
        extends FragmentActivity
        implements WelcomeNoteFragment.OnAgreementChangeListener {

    /** Log tag */
    private static final String TAG = MainActivity.class.getSimpleName();

    private AdapterView mList;
    private boolean mHasAgreedToTerms;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHasAgreedToTerms = CurahApplication.hasUserAgreedTerms();

        // NOTE: Disabled actionbar
        //configureActionBar(savedInstanceState);

        if (mHasAgreedToTerms) {
            // Start the feed activity
            startFeedActivity();
        } else {
            // Set up the welcome fragment
            setupViews(savedInstanceState);
        }
    }

    /**
     * Configure action bar.
     */
    protected void configureActionBar(final Bundle savedInstanceState) {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);  // Hide the app icon
        //actionBar.setDisplayHomeAsUpEnabled(true); // Enable Up button
    }

    /**
     * Setup views
     */
    protected void setupViews(final Bundle savedInstanceState) {
        Fragment fragment = null;
        // Setup the fragments only if savedInstanceState is null
        if (null == savedInstanceState) {
            d(TAG, "Setup fragment (Has agreed to terms: " + mHasAgreedToTerms + ")");
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction transaction = fm.beginTransaction();
            fragment =  new WelcomeNoteFragment();
            transaction.add(R.id.container, fragment).commit();
        }
    }

    @Override
    public void onAgreementChange(final boolean accepted) {
        if (accepted) {
            // Save the status and start pager fragment
            d(TAG, "onAgreementChange - User agreed!");
            CurahApplication.updateUserAgreement(true);
            //SyncAdapter.enablePeriodicSync(this);
            startFeedActivity();
        } else {
            // Close the app
            finish();
        }
    }

    /**
     * Starts the FeedActivity and finish current activity.
     */
    private void startFeedActivity() {
        startActivity(new Intent(this, FeedActivity.class));
        //finish();
    }

}
