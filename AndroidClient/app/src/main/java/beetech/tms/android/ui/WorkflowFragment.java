package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.local.AppDatabase;
import beetech.tms.android.data.models.PendingTransaction;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.sync.SyncWorker;
import beetech.tms.android.ui.adapters.WorkflowTagAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkflowFragment extends BaseFragment {

    private Spinner spinnerCurrentLocation, spinnerDepartment, spinnerToLocation;
    private View containerCurrentLocation, containerDepartment, containerTransfer;
    private TextView textScannedCount;
    private RecyclerView recyclerView;
    private MaterialButton btnSave;
    
    private final List<String> scannedTags = new ArrayList<>();
    private final Set<String> tagSet = new HashSet<>();
    private WorkflowTagAdapter adapter;

    private List<Map<String, Object>> departmentsList = new ArrayList<>();
    private List<StorageLocation> locationsList = new ArrayList<>();
    private String currentOperation = "Unknown";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workflow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentOperation = getArguments().getString("OPERATION", "Unknown");
        }
        
        if (getActivity() != null) {
            getActivity().setTitle(currentOperation);
        }

        spinnerCurrentLocation = view.findViewById(R.id.spinner_current_location);
        spinnerDepartment = view.findViewById(R.id.spinner_workflow_department);
        spinnerToLocation = view.findViewById(R.id.spinner_workflow_to_location);
        containerCurrentLocation = view.findViewById(R.id.current_location_container);
        containerDepartment = view.findViewById(R.id.department_container);
        containerTransfer = view.findViewById(R.id.transfer_container);
        textScannedCount = view.findViewById(R.id.text_scanned_count);
        recyclerView = view.findViewById(R.id.recycler_workflow_tags);
        btnSave = view.findViewById(R.id.btn_save_workflow);

        adapter = new WorkflowTagAdapter(scannedTags);
        recyclerView.setAdapter(adapter);

        setupSpinners();
        loadData();

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupSpinners() {
        if ("InternalTransfer".equals(currentOperation)) {
            containerDepartment.setVisibility(View.GONE);
            containerTransfer.setVisibility(View.VISIBLE);
        } else {
            containerDepartment.setVisibility(View.VISIBLE);
            containerTransfer.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        // Local-first strategy: Load from Room immediately
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            locationsList = db.masterDataDao().getAllLocations();
            List<Map<String, Object>> localDepts = new ArrayList<>(); // TODO: Cache depts too in future or use default
            
            // Note: Since we didn't create a DepartmentEntity yet in the plan, 
            // we'll try API for depts but fallback to empty if offline.
            // For now, let's just make Locations work offline.
            
            requireActivity().runOnUiThread(() -> {
                if (!locationsList.isEmpty()) {
                    populateLocationSpinners(locationsList);
                }
            });
        }).start();

        // Optional: Update cache from API if online
        RetrofitClient.getApi().getDepartments().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departmentsList = response.body();
                    List<String> names = new ArrayList<>();
                    for (Map<String, Object> dept : departmentsList) {
                        names.add((String) dept.get("name"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartment.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });

        RetrofitClient.getApi().getLocations().enqueue(new Callback<List<StorageLocation>>() {
            @Override
            public void onResponse(Call<List<StorageLocation>> call, Response<List<StorageLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationsList = response.body();
                    populateLocationSpinners(locationsList);
                    // Update cache
                    new Thread(() -> {
                        AppDatabase.getDatabase(requireContext()).masterDataDao().insertLocations(response.body());
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<StorageLocation>> call, Throwable t) {}
        });
    }

    private void populateLocationSpinners(List<StorageLocation> list) {
        List<String> names = new ArrayList<>();
        for (StorageLocation loc : list) {
            names.add(loc.name);
        }
        ArrayAdapter<String> adapterTo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
        adapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToLocation.setAdapter(adapterTo);
        
        ArrayAdapter<String> adapterCurr = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
        adapterCurr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrentLocation.setAdapter(adapterCurr);
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        String epc = tagResult.epc;
        if (epc != null && !tagSet.contains(epc)) {
            tagSet.add(epc);
            scannedTags.add(0, epc);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    textScannedCount.setText("Đã quét: " + scannedTags.size() + " sản phẩm");
                });
            }
        }
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        if (reader() != null) {
            int action = translateKeyCode(keyCode, reader().getClass());
            if (action == 2) {
                reader().startInventory();
            }
        }
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
        if (reader() != null) {
            reader().stopInventory();
        }
    }

    private void saveTransaction() {
        if (scannedTags.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng quét sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        TmsApi.MobileTransactionRequest request = new TmsApi.MobileTransactionRequest();
        request.type = currentOperation;
        
        int currLocPos = spinnerCurrentLocation.getSelectedItemPosition();
        if (currLocPos >= 0 && currLocPos < locationsList.size()) {
            request.fromLocationId = locationsList.get(currLocPos).id;
        }
        
        if ("InternalTransfer".equals(currentOperation)) {
            int locPos = spinnerToLocation.getSelectedItemPosition();
            if (locPos >= 0 && locPos < locationsList.size()) {
                request.toLocationId = locationsList.get(locPos).id;
            }
        } else {
            int deptPos = spinnerDepartment.getSelectedItemPosition();
            if (deptPos >= 0 && deptPos < departmentsList.size()) {
                request.departmentId = ((Double) departmentsList.get(deptPos).get("id")).intValue();
            }
        }
        
        request.epcs = scannedTags;
        request.notes = "Giao dịch từ Handheld";

        RetrofitClient.getApi().saveTransaction(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
                    clearWorkflow();
                } else {
                    saveOffline(request);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saveOffline(request);
            }
        });
    }

    private void saveOffline(TmsApi.MobileTransactionRequest request) {
        new Thread(() -> {
            PendingTransaction pt = new PendingTransaction();
            pt.type = request.type;
            pt.fromLocationId = request.fromLocationId;
            pt.toLocationId = request.toLocationId;
            pt.departmentId = request.departmentId;
            pt.epcsJson = new Gson().toJson(request.epcs);
            pt.timestamp = System.currentTimeMillis();

            AppDatabase.getDatabase(requireContext()).transactionDao().insertTransaction(pt);
            
            // Enqueue WorkManager
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .setConstraints(constraints)
                    .build();
            
            WorkManager.getInstance(requireContext()).enqueue(workRequest);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Đã lưu ngoại tuyến. Sẽ tự động đồng bộ khi có mạng.", Toast.LENGTH_LONG).show();
                clearWorkflow();
            });
        }).start();
    }

    private void clearWorkflow() {
        scannedTags.clear();
        tagSet.clear();
        adapter.notifyDataSetChanged();
        textScannedCount.setText("Đã quét: 0 sản phẩm");
    }
}
