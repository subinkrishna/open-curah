package com.subinkrishna.curah.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.subinkrishna.curah.CurahApplication;
import com.subinkrishna.curah.R;
import com.subinkrishna.curah.data.feed.FeedItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Subinkrishna Gopi
 */
public class DateUtil {

    /**
     * Build the author-date text.
     * NOTE: ColorSpan may not work if "textAllCaps" is set.
     *
     * @param context
     * @param item
     * @param prefix
     * @return
     */
    public static SpannableStringBuilder buildAuthorDateText(final Context context,
                                                             final FeedItem item,
                                                             final boolean prefix) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        int startIndex = 0;

        // Append author name
        if (!TextUtils.isEmpty(item.mAuthor)) {
            builder.append(item.mAuthor.toUpperCase());
            startIndex = item.mAuthor.length();
        }

        // Append date
        final String formattedDate = DateUtil.formatDate(item.mPubDate, prefix);
        if (!TextUtils.isEmpty(formattedDate)) {
            // Append separator
            if (builder.length() > 0) {
                builder.append(" . ");
            }

            // Apply color span
            builder.append(formattedDate.toUpperCase());
            final ForegroundColorSpan colorSpan = new ForegroundColorSpan(R.color.text_light);
            builder.setSpan(colorSpan, startIndex, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

    /**
     * Formats date (time in millis) to JUST NOW, TODAY / YESTERDAY format.
     *
     * @param timeInMillis
     * @param prefix
     * @return
     */
    public static String formatDate(final long timeInMillis,
                                    final boolean prefix) {
        // Date Formats
        final String A_DAY_BEFORE_YESTERDAY = "MMM d h:mm aaa";
        final String YESTERDAY_OR_TODAY = "h:mm aaa";
        final SimpleDateFormat formatter = new SimpleDateFormat(A_DAY_BEFORE_YESTERDAY);
        String formattedDate = null;

        final boolean isToday = isToday(timeInMillis);
        final boolean isYesterday = isYesterday(timeInMillis);

        if (isJustNow(timeInMillis)) {
            // Just now
            formattedDate = CurahApplication.get().getString(R.string.just_now);
        } else if (isToday || isYesterday) {
            // Today or yesterday
            formatter.applyPattern(YESTERDAY_OR_TODAY);
            formattedDate = formatter.format(new Date(timeInMillis));

            // Append Today/Yesterday if prefix is true
            if (prefix) {
                final String datePrefix = CurahApplication.get().getString(isToday ? R.string.today : R.string.yesterday);
                formattedDate = datePrefix + " " + formattedDate;
            }
        } else {
            // Some other date
            formattedDate = formatter.format(new Date(timeInMillis));
        }

        return formattedDate;
    }

    public static boolean isJustNow(final long timeInMillis) {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -15);
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return incoming.after(now);
    }

    public static boolean isToday(final long timeInMillis) {
        final Calendar now = Calendar.getInstance();
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return (now.get(Calendar.ERA) == incoming.get(Calendar.ERA)) &&
               (now.get(Calendar.YEAR) == incoming.get(Calendar.YEAR)) &&
               (now.get(Calendar.DAY_OF_YEAR) == incoming.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isYesterday(final long timeInMillis) {
        final Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return (yesterday.get(Calendar.ERA) == incoming.get(Calendar.ERA)) &&
               (yesterday.get(Calendar.YEAR) == incoming.get(Calendar.YEAR)) &&
               (yesterday.get(Calendar.DAY_OF_YEAR) == incoming.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isThisWeek(final long timeInMillis) {
        final Calendar thisWeek = Calendar.getInstance();
        thisWeek.set(Calendar.HOUR_OF_DAY, 0);
        thisWeek.set(Calendar.MINUTE, 0);
        thisWeek.set(Calendar.SECOND, 0);
        thisWeek.set(Calendar.MILLISECOND, 0);
        thisWeek.add(Calendar.WEEK_OF_YEAR, -1);
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return  !incoming.before(thisWeek);
    }

    public static boolean isOlderThanThisWeek(final long timeInMillis) {
        final Calendar thisWeek = Calendar.getInstance();
        thisWeek.set(Calendar.HOUR_OF_DAY, 0);
        thisWeek.set(Calendar.MINUTE, 0);
        thisWeek.set(Calendar.SECOND, 0);
        thisWeek.set(Calendar.MILLISECOND, 0);
        thisWeek.add(Calendar.WEEK_OF_YEAR, -1);
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return  incoming.before(thisWeek);
    }

    public static boolean isOlderThanYesterday(final long timeInMillis) {
        final Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        final Calendar incoming = Calendar.getInstance();
        incoming.setTimeInMillis(timeInMillis);
        return  incoming.before(yesterday);
    }

}
