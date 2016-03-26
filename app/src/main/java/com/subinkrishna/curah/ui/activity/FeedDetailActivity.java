package com.subinkrishna.curah.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.ui.fragment.FeedDetailsFragment;

/**
 * Feed detail activity.
 *
 * @author Subinkrishna Gopi
 */
public class FeedDetailActivity extends ActionBarActivity {

    private static final String TAG = FeedDetailActivity.class.getSimpleName();
    public static final String KEY_ITEM = "key.item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_details);

        // Sets the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (null == savedInstanceState) {
            Object tag = getIntent().getSerializableExtra(KEY_ITEM);
            FeedItem item = ((null != tag) && (tag instanceof FeedItem)) ? (FeedItem) tag : null;

            if (null != item) {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.container, FeedDetailsFragment.newInstance(item, false)).commit();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
