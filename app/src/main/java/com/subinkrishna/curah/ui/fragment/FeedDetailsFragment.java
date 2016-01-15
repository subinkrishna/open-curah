package com.subinkrishna.curah.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.util.DateUtil;

import static com.subinkrishna.curah.util.Logger.d;
import static com.subinkrishna.curah.util.Logger.e;

/**
 * @author Subinkrishna Gopi
 */
public class FeedDetailsFragment
        extends DialogFragment
        implements View.OnClickListener {

    /** Log tag */
    public static final String TAG = FeedDetailsFragment.class.getSimpleName();

    public static FeedDetailsFragment newInstance(FeedItem item,
                                                  boolean asDialog) {
        if (null != item) {
            FeedDetailsFragment fragment = new FeedDetailsFragment();
            fragment.mItem = item;
            fragment.setShowsDialog(asDialog);
            fragment.setRetainInstance(true);
            return fragment;
        }

        return null;
    }

    private FeedItem mItem;

    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mDescriptionTextView;
    private TextView mVisitPageTextView;
    private TextView mShareTextView;
    private View mActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (getShowsDialog()) {
            return null;
        }
        return inflater.inflate(R.layout.fragment_feed_details, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity parent = getActivity();
        View view = parent.getLayoutInflater().inflate(R.layout.fragment_feed_details, null);
        onViewCreated(view, savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setView(view).setTitle(null);
        return builder.create();
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mAuthorTextView = (TextView) view.findViewById(R.id.author);
        mDescriptionTextView = (TextView) view.findViewById(R.id.description);

        mVisitPageTextView = (TextView) view.findViewById(R.id.visit_page_button);
        mShareTextView = (TextView) view.findViewById(R.id.share_button);
        mActionButton = view.findViewById(R.id.fab);

        mVisitPageTextView.setOnClickListener(this);
        mShareTextView.setOnClickListener(this);
        mActionButton.setOnClickListener(this);

        update(mItem);
    }

    @Override
    public void onDestroyView() {
        // Prevents the dialog from getting dismissed on rotation.
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private FeedDetailsFragment update(FeedItem item) {
        mTitleTextView.setText(item.mTitle);
        mAuthorTextView.setText(DateUtil.buildAuthorDateText(getActivity(), item, true));
        mDescriptionTextView.setText(Html.fromHtml(item.mDescription));
        mDescriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mItem = item;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.visit_page_button:
                viewPageInBrowser(mItem);
                break;
            case R.id.share_button:
            case R.id.fab:
                shareFeed(mItem);
                break;
        }
    }

    /**
     * Creates a share chooser for the item
     *
     * @param item
     */
    private void shareFeed(FeedItem item) {
        if (null != item) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.mTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT, item.mLink);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
        } else {
            Toast.makeText(getActivity(),
                    getString(R.string.error_no_matching_apps),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the URL in the browser.
     *
     * @param item
     */
    private void viewPageInBrowser(FeedItem item) {
        if (null != item) {
            String url = item.mLink;
            d(TAG, "Open URL: " + url);
            if (!url.startsWith("http://") &&
                !url.startsWith("https://")) {
                url = "http://" + url;
            }
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(browserIntent);
            } catch (Exception e) {
                e(TAG, "Exception", e);
            }
        }
    }

}
