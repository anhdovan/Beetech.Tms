package beetech.tms.android.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "documents")
public class DocumentEntity {
    @PrimaryKey
    public int id;
    public String documentCode;
    public String title;
    public String status;
    public String category;
    public String customer;
    public String expiryDate;
    public String rfidTag;
    public String barcode;
    public String storageLocation;
    public String tagStatus;
    public long lastSyncAt;
}
