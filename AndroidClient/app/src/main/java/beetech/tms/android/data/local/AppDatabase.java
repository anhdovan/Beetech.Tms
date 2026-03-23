package beetech.tms.android.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import beetech.tms.android.data.local.dao.MasterDataDao;
import beetech.tms.android.data.local.dao.TransactionDao;
import beetech.tms.android.data.models.PendingAuditSession;
import beetech.tms.android.data.models.PendingTransaction;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.TextileItem;

@Database(entities = {
        StorageLocation.class,
        TextileItem.class,
        PendingTransaction.class,
        PendingAuditSession.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract MasterDataDao masterDataDao();
    public abstract TransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tms_offline_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
