package beetech.tms.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import beetech.tms.android.TmsApp;

public class SettingsManager {
    private static final String PREF_NAME = "beetech_tms_settings";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_READER_POWER = "reader_power";
    private static final String KEY_BEEP_ENABLED = "beep_enabled";

    private static final String DEFAULT_SERVER_URL = "http://192.168.1.2:5269/";
    private static final int DEFAULT_READER_POWER = 30;

    private static SettingsManager instance;
    private final SharedPreferences prefs;

    private SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager(TmsApp.getInstance());
        }
        return instance;
    }

    public String getServerUrl() {
        String url = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL);
        if (url == null) url = DEFAULT_SERVER_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    public void setServerUrl(String url) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply();
    }

    public int getReaderPower() {
        return prefs.getInt(KEY_READER_POWER, DEFAULT_READER_POWER);
    }

    public void setReaderPower(int power) {
        prefs.edit().putInt(KEY_READER_POWER, power).apply();
    }

    public boolean isBeepEnabled() {
        return prefs.getBoolean(KEY_BEEP_ENABLED, true);
    }

    public void setBeepEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BEEP_ENABLED, enabled).apply();
    }
}
