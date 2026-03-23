package beetech.tms.android.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.data.local.AppDatabase;
import beetech.tms.android.data.models.PendingAuditSession;
import beetech.tms.android.data.models.PendingTransaction;
import beetech.tms.android.data.models.InventoryAuditSession;
import beetech.tms.android.data.models.InventoryAuditResult;
import retrofit2.Response;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private final AppDatabase db;
    private final Gson gson = new Gson();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting Sync Worker...");
        boolean allSuccess = true;

        // 1. Sync Workflows
        List<PendingTransaction> workflows = db.transactionDao().getPendingTransactions();
        for (PendingTransaction workflow : workflows) {
            if (!syncWorkflow(workflow)) {
                allSuccess = false;
            }
        }

        // 2. Sync Audits
        List<PendingAuditSession> audits = db.transactionDao().getPendingAuditSessions();
        for (PendingAuditSession audit : audits) {
            if (!syncAudit(audit)) {
                allSuccess = false;
            }
        }

        return allSuccess ? Result.success() : Result.retry();
    }

    private boolean syncWorkflow(PendingTransaction pt) {
        try {
            TmsApi.MobileTransactionRequest request = new TmsApi.MobileTransactionRequest();
            request.type = pt.type;
            request.fromLocationId = pt.fromLocationId;
            request.toLocationId = pt.toLocationId;
            request.departmentId = pt.departmentId;
            request.tags = gson.fromJson(pt.epcsJson, new TypeToken<List<String>>(){}.getType());

            Response<Void> response = RetrofitClient.getApi().saveTransaction(request).execute();
            if (response.isSuccessful()) {
                db.transactionDao().deleteTransaction(pt);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync Workflow Error", e);
        }
        return false;
    }

    private boolean syncAudit(PendingAuditSession pas) {
        try {
            // Step 1: Create Session
            InventoryAuditSession session = new InventoryAuditSession();
            session.locationId = pas.locationId;
            session.status = pas.status;
            session.performByName = pas.performByName;

            Response<InventoryAuditSession> sessionResp = RetrofitClient.getApi().saveInventorySession(session).execute();
            if (sessionResp.isSuccessful() && sessionResp.body() != null) {
                int serverSessionId = sessionResp.body().id;
                List<InventoryAuditResult> records = gson.fromJson(pas.recordsJson, new TypeToken<List<InventoryAuditResult>>(){}.getType());
                
                // Update records with server session ID
                for (InventoryAuditResult r : records) {
                    r.inventoryAuditSessionId = serverSessionId;
                }

                // Step 2: Push Records
                Response<Map<String, Object>> recordsResp = RetrofitClient.getApi().saveInventoryRecordsBatch(records).execute();
                if (recordsResp.isSuccessful()) {
                    db.transactionDao().deleteAuditSession(pas);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Sync Audit Error", e);
        }
        return false;
    }
}
