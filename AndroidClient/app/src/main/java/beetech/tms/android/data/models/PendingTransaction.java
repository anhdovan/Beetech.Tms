package beetech.tms.android.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_transactions")
public class PendingTransaction {
    @PrimaryKey(autoGenerate = true)
    public int localId;
    
    public String type;
    public Integer fromLocationId;
    public Integer toLocationId;
    public Integer departmentId;
    public String epcsJson; // Storing list of EPCs as JSON string for simplicity
    public long timestamp;
}
