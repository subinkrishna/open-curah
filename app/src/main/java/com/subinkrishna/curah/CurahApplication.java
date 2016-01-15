package com.subinkrishna.curah;

import android.app.Application;
import android.content.SharedPreferences;

import com.subinkrishna.curah.data.sync.SyncAdapter;
import com.subinkrishna.curah.eunmeration.Feed;
import com.subinkrishna.curah.eunmeration.Preferences;

/**
 * Application.
 *
 * @author Subinkrishna Gopi
 */
public class CurahApplication extends Application {

    /** Singleton instance of the application for quicker/static access */
    private static CurahApplication sInstance;

    /** Preferences */
    private static final String DEFAULT_PREF = "com.subinkrishna.curah.pref";

    /** Default feed to be populated */
    public static final Feed DefaultFeed = Feed.CURAH_FEATURED;

    /**
     * Returns the static instance of application.
     *
     * @return
     */
    public static CurahApplication get() {
        return sInstance;
    }

    /**
     * @return Returns {@code true} if the user has agreed to the terms, else {@code false}
     */
    public static boolean hasUserAgreedTerms() {
        final SharedPreferences pref = getPreferences();
        return (null != pref) ? pref.getBoolean(Preferences.HasAgreedToTerms.label, false) : false;
    }

    /**
     * Updates the user's agreements to terms.
     *
     * @param status
     */
    public static void updateUserAgreement(final boolean status) {
        final SharedPreferences.Editor editor = getPreferencesEditor();
        editor.putBoolean(Preferences.HasAgreedToTerms.label, status).commit();
    }

    /**
     * Returns an instance of the default shared preferences.
     *
     * @return
     */
    public static SharedPreferences getPreferences() {
        return sInstance.getSharedPreferences(DEFAULT_PREF,
                MODE_MULTI_PROCESS); // Using MODE_MULTI_PROCESS to enable
                                     // different processes with in the same app to read the pref
    }

    /**
     * @return Returns an instance of the default preference editor.
     */
    public static SharedPreferences.Editor getPreferencesEditor() {
        return sInstance.getSharedPreferences(DEFAULT_PREF, MODE_MULTI_PROCESS).edit();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up the static instance
        sInstance = this;

        // Enable periodic sync
        SyncAdapter.enablePeriodicSync(this);
    }
}
