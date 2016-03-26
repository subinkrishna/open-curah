package com.subinkrishna.curah.ui.view;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;
import com.subinkrishna.curah.util.Config;
import com.subinkrishna.curah.util.DateUtil;

/**
 * @author Subinkrishna Gopi
 */
public class FeedItemView extends LinearLayout {

    /**
     * Create an instance of FeedItemView.
     *
     * @param context
     * @return
     */
    public static FeedItemView newInstance(final Context context) {
        return (null != context) ?new FeedItemView(context) : null;
    }

    private TextView mAuthorAndDate;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mDateHeader;
    private View mDivider;

    public FeedItemView(Context context) {
        super(context);
        configure(context, null);
    }

    public FeedItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure(context, attrs);
    }

    public FeedItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configure(context, attrs);
    }

    /**
     * Configure the view
     *
     * @param context
     * @param attrs
     */
    private void configure(final Context context,
                           final AttributeSet attrs) {
        // Inflate the view layout
        if (null != context) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.item_feed_item_card_with_date, this, true);

            mAuthorAndDate = (TextView) findViewById(R.id.author);
            mTitle = (TextView) findViewById(R.id.title);
            mDescription = (TextView) findViewById(R.id.description);
            mDateHeader = (TextView) findViewById(R.id.date_header);
            mDivider = findViewById(R.id.divider);
        }
    }

    /**
     * Recycles the view with the contents from the incoming FeedItem
     *
     * @param item
     * @return
     */
    public FeedItemView recycle (final FeedItem item) {
        if (null != item) {
            final TextView[] textViews = { mTitle, mDescription };
            final String[] values = { item.mTitle,
                    item.mDescription };
            // Set the view contents
            TextView aTextView = null;
            for (int i = 0; i < textViews.length; i++) {
                aTextView = textViews[i];
                if (null != aTextView) {
                    aTextView.setText(TextUtils.isEmpty(values[i])
                            ? ""
                            // Strip HTML tags
                            : Html.fromHtml(values[i].trim()).toString());
                }
            }

            // Set the styled (spanned)
            Context context = getContext();
            final boolean isTablet = Config.isTablet(context);
            mAuthorAndDate.setText(DateUtil.buildAuthorDateText(context, item, isTablet));
        }

        return this;
    }

    /**
     * Set the date header.
     *
     * @param resId
     * @return
     */
    public FeedItemView setDateHeader(final int resId) {
        if (null != mDateHeader) {
            String header = null;
            if ((-1 == resId) || (TextUtils.isEmpty(header = getContext().getString(resId)))) {
                mDateHeader.setVisibility(GONE);
            } else {
                mDateHeader.setText(header);
                mDateHeader.setVisibility(VISIBLE);
            }
        }

        return this;
    }

    /**
     * Set the divider.
     *
     * @param pos
     * @return
     */
    public FeedItemView setDivider(final int pos) {
        if (null != mDivider) {
            mDivider.setVisibility((0 == pos) ? GONE : VISIBLE);
        }

        return this;
    }
}
