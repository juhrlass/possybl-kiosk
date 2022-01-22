package de.ecube.kioskweb.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {

    // Values for Shared Prefrences
    private static final String INSTANCE_ID_PREF = "instance_id";
    private static final String RELOAD_PENDING_PREF = "reload_pending";
    private static final String LOCK_PENDING_PREF = "lock_pending";
    private static final String UNLOCK_PENDING_PREF = "unlock_pending";
    private static final String WEB_URL_PREF = "web_url";
    private static final String NETWORK_TIMEOUT_PREF = "network_timeout";
    private static final String FORCE_CACHE_LOADING_REF = "force_cache_loading";

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setInstanceId(Context context, String instanceId) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(INSTANCE_ID_PREF, instanceId);
        editor.apply();
    }

    public static String getInstanceId(Context context) {
        return getPreferences(context).getString(INSTANCE_ID_PREF, null);
    }

    public static void setReloadPending(Context context, boolean reloadPending) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(RELOAD_PENDING_PREF, reloadPending);
        editor.apply();
    }

    public static boolean getReloadPending(Context context) {
        return getPreferences(context).getBoolean(RELOAD_PENDING_PREF, false);
    }

    public static void setLockPending(Context context, boolean lockPending) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOCK_PENDING_PREF, lockPending);
        editor.apply();
    }

    public static boolean getLockPending(Context context) {
        return getPreferences(context).getBoolean(LOCK_PENDING_PREF, false);
    }

    public static void setUnlockPending(Context context, boolean unlockPending) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(UNLOCK_PENDING_PREF, unlockPending);
        editor.apply();
    }

    public static boolean getUnlockPending(Context context) {
        return getPreferences(context).getBoolean(UNLOCK_PENDING_PREF, false);
    }

    public static void setWebUrl(Context context, String webUrl) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(WEB_URL_PREF, webUrl);
        editor.apply();
    }

    public static String getWebUrl(Context context) {
        return getPreferences(context).getString(WEB_URL_PREF, null);
    }

    public static void setForceCacheLoading(Context context, boolean forceCacheLoading) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(FORCE_CACHE_LOADING_REF, forceCacheLoading);
        editor.apply();
    }

    public static Boolean getForceCacheLoading(Context context) {
        return getPreferences(context).getBoolean(FORCE_CACHE_LOADING_REF, false);
    }


    public static int getNetworkTimeoutDelay(Context context) {
        return getPreferences(context).getInt(NETWORK_TIMEOUT_PREF, 20000);
    }
}