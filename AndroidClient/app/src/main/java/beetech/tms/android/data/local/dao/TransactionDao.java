package beetech.tms.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import beetech.tms.android.data.models.PendingTransaction;
import beetech.tms.android.data.models.PendingAuditSession;

@Dao
public interface TransactionDao {
    // Workflows
    @Insert
    void insertTransaction(PendingTransaction transaction);

    @Query("SELECT * FROM pending_transactions ORDER BY timestamp ASC")
    List<PendingTransaction> getPendingTransactions();

    @Delete
    void deleteTransaction(PendingTransaction transaction);

    // Audits
    @Insert
    void insertAuditSession(PendingAuditSession session);

    @Query("SELECT * FROM pending_audit_sessions ORDER BY timestamp ASC")
    List<PendingAuditSession> getPendingAuditSessions();

    @Delete
    void deleteAuditSession(PendingAuditSession session);
}
