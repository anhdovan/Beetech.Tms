package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.media.ToneGenerator;

import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.local.AppDatabase;
import beetech.tms.android.data.models.PendingAuditSession;
import beetech.tms.android.data.models.TextileItem;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.InventoryAuditSession;
import beetech.tms.android.data.models.InventoryAuditResult;
import beetech.tms.android.data.sync.SyncWorker;
import beetech.tms.android.ui.adapters.AuditResultAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFragment extends BaseFragment {

    private final List<AuditResultAdapter.AuditItem> auditItems = new ArrayList<>();
    private final Map<String, TextileItem> expectedItems = new HashMap<>();
    private final Set<String> scannedEpCs = new HashSet<>();

    private AuditResultAdapter adapter;
    private TextView tvMatched, tvMissing, tvUnexpected, tvExpected, tvTotal;
    private TextView tvCurrentEpc, tvCurrentTitle, tvCurrentStatus;
    private View cardCurrentResult;
    private RecyclerView recyclerView;
    private MaterialButton btnClear, btnShowResults, btnSave;
    private Spinner spinnerLocation;
    private List<StorageLocation> locationsList = new ArrayList<>();
    private Integer selectedLocationId = null;
    private ToneGenerator toneGenerator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMatched = view.findViewById(R.id.tv_count_matched);
        tvMissing = view.findViewById(R.id.tv_count_missing);
        tvUnexpected = view.findViewById(R.id.tv_count_unexpected);
        tvTotal = view.findViewById(R.id.tv_count_total);
        tvExpected = view.findViewById(R.id.tv_count_expected);
        recyclerView = view.findViewById(R.id.recycler_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cardCurrentResult = view.findViewById(R.id.card_current_result);
        tvCurrentEpc = view.findViewById(R.id.tv_current_epc);
        tvCurrentTitle = view.findViewById(R.id.tv_current_title);
        tvCurrentStatus = view.findViewById(R.id.tv_current_status);

        spinnerLocation = view.findViewById(R.id.spinner_inventory_location);
        btnClear = view.findViewById(R.id.btn_clear_inventory);
        btnShowResults = view.findViewById(R.id.btn_show_results);
        btnSave = view.findViewById(R.id.btn_save_inventory);

        adapter = new AuditResultAdapter(auditItems);
        recyclerView.setAdapter(adapter);

        btnClear.setOnClickListener(v -> clearResults());
        btnShowResults.setOnClickListener(v -> showResultsDialog());
        btnSave.setOnClickListener(v -> saveInventory());

        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        setupLocationSpinner();
    }

    private void setupLocationSpinner() {
        // Local-first
        new Thread(() -> {
            locationsList = AppDatabase.getDatabase(requireContext()).masterDataDao().getAllLocations();
            requireActivity().runOnUiThread(() -> {
                if (!locationsList.isEmpty()) {
                    updateLocationSpinnerUi(locationsList);
                }
            });
        }).start();

        RetrofitClient.getApi().getLocations().enqueue(new Callback<List<StorageLocation>>() {
            @Override
            public void onResponse(Call<List<StorageLocation>> call, Response<List<StorageLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationsList = response.body();
                    updateLocationSpinnerUi(locationsList);
                    new Thread(() -> {
                        AppDatabase.getDatabase(requireContext()).masterDataDao().insertLocations(response.body());
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<StorageLocation>> call, Throwable t) {
                if (locationsList.isEmpty()) {
                    Toast.makeText(getContext(), "Không thể tải danh sách vị trí", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateLocationSpinnerUi(List<StorageLocation> list) {
        List<String> names = new ArrayList<>();
        names.add("-- Chọn vị trí kiểm kê --");
        for (StorageLocation loc : list) {
            names.add(loc.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);

        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedLocationId = list.get(position - 1).id;
                    loadExpectedDataForLocation(selectedLocationId);
                } else {
                    selectedLocationId = null;
                    expectedItems.clear();
                    updateSummary();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadExpectedDataForLocation(int locationId) {
        RetrofitClient.getApi().getItems(null, locationId, null)
                .enqueue(new Callback<List<TextileItem>>() {
                    @Override
                    public void onResponse(Call<List<TextileItem>> call, Response<List<TextileItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            expectedItems.clear();
                            for (TextileItem item : response.body()) {
                                String epc = item.epc != null && !item.epc.isEmpty() ? item.epc : item.getExpectedEpc();
                                if (epc != null) {
                                    expectedItems.put(epc.toUpperCase().trim(), item);
                                }
                            }
                            updateSummary();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TextileItem>> call, Throwable t) {
                        Toast.makeText(getContext(), "Không thể tải dữ liệu dự kiến", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        if (reader() != null) {
            int action = translateKeyCode(keyCode, reader().getClass());
            if (action == 2) { // 2 = Inventory trigger in legacy reader logic
                if (selectedLocationId == null) {
                    showNoLocationError();
                    return;
                }
                reader().startInventory();
            }
        }
    }

    private void showNoLocationError() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 200);
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chưa chọn vị trí")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Vui lòng chọn vị trí kiểm kê trước khi bắt đầu quét.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
        if (reader() != null) {
            reader().stopInventory();
        }
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        if (selectedLocationId == null) return;

        String epc = tagResult.epc;
        if (epc == null) return;

        String normalized = epc.toUpperCase().trim();
        if (scannedEpCs.contains(normalized)) return;

        scannedEpCs.add(normalized);

        if (expectedItems.containsKey(normalized)) {
            TextileItem item = expectedItems.get(normalized);
            addAuditItem(normalized, item.category + " - " + item.code, AuditResultAdapter.TagStatus.OK);
        } else {
            checkEpcInDatabase(normalized);
        }
    }

    private void checkEpcInDatabase(String epc) {
        // Local check first
        new Thread(() -> {
            TextileItem localItem = AppDatabase.getDatabase(requireContext()).masterDataDao().getItemByEpc(epc);
            if (localItem != null) {
                String title = localItem.category + " (" + (localItem.location != null ? localItem.location : "N/A") + ")";
                addAuditItem(epc, title, AuditResultAdapter.TagStatus.UNEXPECTED);
            } else {
                // Try remote
                RetrofitClient.getApi().getItemByTag(epc).enqueue(new Callback<TextileItem>() {
                    @Override
                    public void onResponse(Call<TextileItem> call, Response<TextileItem> response) {
                        AuditResultAdapter.TagStatus status;
                        String title = "Thẻ lạ";

                        if (response.isSuccessful() && response.body() != null) {
                            status = AuditResultAdapter.TagStatus.UNEXPECTED;
                            TextileItem item = response.body();
                            title = item.category + " (" + (item.location != null ? item.location : "N/A") + ")";
                        } else {
                            status = AuditResultAdapter.TagStatus.UNKNOWN;
                        }

                        addAuditItem(epc, title, status);
                    }

                    @Override
                    public void onFailure(Call<TextileItem> call, Throwable t) {
                        addAuditItem(epc, "Thẻ lạ (Offline)", AuditResultAdapter.TagStatus.UNKNOWN);
                    }
                });
            }
        }).start();
    }

    private void addAuditItem(String epc, String title, AuditResultAdapter.TagStatus status) {
        AuditResultAdapter.AuditItem item = new AuditResultAdapter.AuditItem(epc, title, "", status);
        runOnUiThreadSafe(() -> {
            auditItems.add(0, item);
            adapter.notifyItemInserted(0);
            updateSummary();
            updateCurrentResultCard(item);
        });
    }

    private void updateCurrentResultCard(AuditResultAdapter.AuditItem item) {
        tvCurrentEpc.setText("EPC: " + item.epc);
        tvCurrentTitle.setText(item.title);
        
        String statusText = "UNKNOWN";
        int color = 0xFF757575;
        switch (item.status) {
            case OK: statusText = "KHỚP"; color = 0xFF4CAF50; break;
            case UNEXPECTED: statusText = "SAI VỊ TRÍ"; color = 0xFFFF9800; break;
            case UNKNOWN: statusText = "LẠ"; color = 0xFFF44336; break;
        }
        
        tvCurrentStatus.setText(statusText);
        tvCurrentStatus.setBackgroundColor(color);
        cardCurrentResult.setVisibility(View.VISIBLE);
    }

    private void showResultsDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventory_results, null);
        RecyclerView rv = dialogView.findViewById(R.id.recycler_inventory_dialog);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),
                androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_close_dialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateSummary() {
        int matched = 0;
        int unexpected = 0;
        for (AuditResultAdapter.AuditItem item : auditItems) {
            if (item.status == AuditResultAdapter.TagStatus.OK) matched++;
            if (item.status == AuditResultAdapter.TagStatus.UNEXPECTED) unexpected++;
        }
        int missing = Math.max(0, expectedItems.size() - matched);

        tvMatched.setText(String.valueOf(matched));
        tvMissing.setText(String.valueOf(missing));
        tvUnexpected.setText(String.valueOf(unexpected));
        tvExpected.setText(String.valueOf(expectedItems.size()));
        tvTotal.setText(String.valueOf(scannedEpCs.size()));
    }

    private void saveInventory() {
        if (selectedLocationId == null) {
            showNoLocationError();
            return;
        }

        if (auditItems.isEmpty() && scannedEpCs.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("beetech_tms_prefs",
                android.content.Context.MODE_PRIVATE);
        String operatorName = prefs.getString("full_name", "Handy Terminal");

        InventoryAuditSession session = new InventoryAuditSession();
        session.locationId = selectedLocationId;
        session.status = "Completed";
        session.performByName = operatorName;

        RetrofitClient.getApi().saveInventorySession(session)
                .enqueue(new Callback<InventoryAuditSession>() {
                    @Override
                    public void onResponse(Call<InventoryAuditSession> call, Response<InventoryAuditSession> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            saveAuditRecords(response.body().id);
                        } else {
                            saveInventoryOffline(session);
                        }
                    }

                    @Override
                    public void onFailure(Call<InventoryAuditSession> call, Throwable t) {
                        saveInventoryOffline(session);
                    }
                });
    }

    private void saveInventoryOffline(InventoryAuditSession session) {
        new Thread(() -> {
            List<InventoryAuditResult> records = new ArrayList<>();
            for (AuditResultAdapter.AuditItem item : auditItems) {
                records.add(createRecord(0, item));
            }
            // Add missing items
            for (Map.Entry<String, TextileItem> entry : expectedItems.entrySet()) {
                if (!scannedEpCs.contains(entry.getKey())) {
                    AuditResultAdapter.AuditItem m = new AuditResultAdapter.AuditItem(
                        entry.getKey(), entry.getValue().category, entry.getValue().code, AuditResultAdapter.TagStatus.MISSING);
                    records.add(createRecord(0, m));
                }
            }

            PendingAuditSession pas = new PendingAuditSession();
            pas.locationId = session.locationId;
            pas.status = session.status;
            pas.performByName = session.performByName;
            pas.timestamp = System.currentTimeMillis();
            pas.recordsJson = new Gson().toJson(records);

            AppDatabase.getDatabase(requireContext()).transactionDao().insertAuditSession(pas);

            // WorkManager
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance(requireContext()).enqueue(workRequest);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Đã lưu ngoại tuyến. Sẽ tự động đồng bộ khi có mạng.", Toast.LENGTH_LONG).show();
                clearResults();
            });
        }).start();
    }

    private void saveAuditRecords(int sessionId) {
        List<InventoryAuditResult> recordsToSave = new ArrayList<>();

        for (AuditResultAdapter.AuditItem item : auditItems) {
            recordsToSave.add(createRecord(sessionId, item));
        }

        for (Map.Entry<String, TextileItem> entry : expectedItems.entrySet()) {
            if (!scannedEpCs.contains(entry.getKey())) {
                AuditResultAdapter.AuditItem missingItem = new AuditResultAdapter.AuditItem(
                        entry.getKey(), entry.getValue().category, entry.getValue().code, AuditResultAdapter.TagStatus.MISSING);
                recordsToSave.add(createRecord(sessionId, missingItem));
            }
        }

        if (recordsToSave.isEmpty()) {
            Toast.makeText(getContext(), "Đã lưu phiên kiểm kê (không có kết quả)", Toast.LENGTH_SHORT).show();
            clearResults();
            return;
        }

        RetrofitClient.getApi().saveInventoryRecordsBatch(recordsToSave)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã lưu kết quả kiểm kê", Toast.LENGTH_LONG).show();
                            clearResults();
                            if (isAdded() && getActivity() != null) {
                                if (getActivity() instanceof beetech.tms.android.MainActivity) {
                                    ((beetech.tms.android.MainActivity) getActivity()).selectItems(null);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi lưu kết quả chi tiết", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối khi lưu chi tiết", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private InventoryAuditResult createRecord(int sessionId, AuditResultAdapter.AuditItem item) {
        InventoryAuditResult record = new InventoryAuditResult();
        record.inventoryAuditSessionId = sessionId;
        record.tag = item.epc;
        record.assetName = item.title;

        String backendStatus = "Unknown";
        switch (item.status) {
            case OK: backendStatus = "Verified"; break;
            case MISSING: backendStatus = "Missing"; break;
            case UNEXPECTED: backendStatus = "Relocated"; break;
        }
        record.status = backendStatus;
        record.isValid = (item.status == AuditResultAdapter.TagStatus.OK);

        if (expectedItems.containsKey(item.epc)) {
            record.textileItemId = expectedItems.get(item.epc).id;
        }

        return record;
    }

    private void clearResults() {
        auditItems.clear();
        scannedEpCs.clear();
        adapter.notifyDataSetChanged();
        updateSummary();
        cardCurrentResult.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(r);
        }
    }
}
