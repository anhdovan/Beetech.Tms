package beetech.tms.android;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class TmsApp extends Application {
    private static TmsApp instance;
    private MainActivity mainActivity;

    public static TmsApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
