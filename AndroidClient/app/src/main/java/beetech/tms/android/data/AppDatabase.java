package beetech.tms.android.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { DocumentEntity.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DocumentDao documentDao();
}
