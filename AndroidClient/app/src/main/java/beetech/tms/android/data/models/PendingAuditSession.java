package beetech.tms.android.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_audit_sessions")
public class PendingAuditSession {
    @PrimaryKey(autoGenerate = true)
    public int localSessionId;
    
    public int locationId;
    public String status;
    public String performByName;
    public long timestamp;
    
    public String recordsJson; // Storing list of audit records as JSON string
}
