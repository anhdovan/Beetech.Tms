package beetech.tms.android.data.sync;

import android.content.Context;
import android.util.Log;

import java.util.List;

import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.local.AppDatabase;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.TextileItem;
import retrofit2.Response;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private final Context context;
    private final AppDatabase db;

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getDatabase(this.context);
    }

    public interface SyncCallback {
        void onSyncComplete();
        void onSyncError(String error);
    }

    public void syncMasterData(SyncCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting Master Data Sync...");

                // 1. Sync Locations
                Response<List<StorageLocation>> locResponse = RetrofitClient.getApi().getLocations().execute();
                if (locResponse.isSuccessful() && locResponse.body() != null) {
                    db.masterDataDao().clearLocations();
                    db.masterDataDao().insertLocations(locResponse.body());
                    Log.d(TAG, "Sync Locations: SUCCESS (" + locResponse.body().size() + ")");
                }

                // 2. Sync Items (Tags)
                // Note: This might be large, but for a handheld client it should be manageable.
                // In a real production app, we might want to paginate or sync only changes.
                Response<List<TextileItem>> itemResponse = RetrofitClient.getApi().getItems(null, null, null).execute();
                if (itemResponse.isSuccessful() && itemResponse.body() != null) {
                    db.masterDataDao().clearItems();
                    db.masterDataDao().insertItems(itemResponse.body());
                    Log.d(TAG, "Sync Items: SUCCESS (" + itemResponse.body().size() + ")");
                }

                if (callback != null) callback.onSyncComplete();
            } catch (Exception e) {
                Log.e(TAG, "Sync Error", e);
                if (callback != null) callback.onSyncError(e.getMessage());
            }
        }).start();
    }
}
