package com.subinkrishna.curah.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;

/**
 * View Utility.
 *
 * @author Subinkrishna Gopi
 */
public class ViewUtil {

    /** Log tag */
    public static final String TAG = ImageUtil.class.getSimpleName();

    private ViewUtil() {

    }

    /**
     * Sets a Bitmap as background to a view.
     *
     * @param context
     * @param view
     * @param bitmap
     */
    public static void setBitmapBackground(final Context context,
                                           final View view,
                                           final Bitmap bitmap) {
        if ((null != context) &&
            (null != view) &&
            (null != bitmap)) {
            if (Build.VERSION.SDK_INT >= 16) {
                setBitmapBackgroundV16Plus(context, view, bitmap);
            } else {
                setBitmapBackgroundV16Minus(context, view, bitmap);
            }
        } else {
            Logger.d(TAG, "Invalid inputs. Unable to set the bitmap background");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setBitmapBackgroundV16Plus(final Context context,
                                                   final View view,
                                                   final Bitmap bitmap) {
        view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
    }

    @SuppressWarnings("deprecation")
    private static void setBitmapBackgroundV16Minus(final Context context,
                                                    final View view,
                                                    final Bitmap bitmap) {
        view.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

}
