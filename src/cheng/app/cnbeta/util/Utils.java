package cheng.app.cnbeta.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cheng.app.cnbeta.R;

public class Utils {
    public static final String PREFERENCE_THEME = "preferences_theme";
    public static final String PREFERENCE_FONT_SIZE = "preferences_font_size";
    public static final String PREFERENCE_THEME_DEFAULT = "0";
    public static final String PREFERENCE_FONT_SIZE_DEFAULT = "16";
    private static SharedPreferences sharedPreferences;

    public static String getSharedPreferences(Context c, String key, String defaultValue) {
        return getSharedPreferencesObject(c).getString(key, defaultValue);
    }

    public static int getSharedPreferences(Context c, String key, int defaultValue) {
        return getSharedPreferencesObject(c).getInt(key, defaultValue);
    }

    public static boolean getSharedPreferences(Context c, String key, boolean defaultValue) {
        return getSharedPreferencesObject(c).getBoolean(key, defaultValue);
    }

    private static SharedPreferences getSharedPreferencesObject(Context c) {
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPreferences;
    }

    public static int getAppTheme(Context c) {
        String value = getSharedPreferences(c, PREFERENCE_THEME, PREFERENCE_THEME_DEFAULT);

        switch (Integer.valueOf(value)) {
            case 0:
                return R.style.AppTheme;

            case 1:
                return R.style.AppTheme_Dark;

            default:
                return R.style.AppTheme;

        }
    }

}
