package beetech.tms.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.TextileItem;

@Dao
public interface MasterDataDao {
    @Query("SELECT * FROM locations")
    List<StorageLocation> getAllLocations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocations(List<StorageLocation> locations);

    @Query("DELETE FROM locations")
    void clearLocations();

    @Query("SELECT * FROM items WHERE epc = :epc LIMIT 1")
    TextileItem getItemByEpc(String epc);

    @Query("SELECT * FROM items")
    List<TextileItem> getAllItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(List<TextileItem> items);

    @Query("DELETE FROM items")
    void clearItems();
}
