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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.ui.adapters.WorkflowTagAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkflowFragment extends BaseFragment {

    private Spinner spinnerType, spinnerDepartment, spinnerToLocation;
    private View containerDepartment, containerTransfer;
    private TextView textScannedCount;
    private RecyclerView recyclerView;
    private MaterialButton btnSave;
    
    private final List<String> scannedTags = new ArrayList<>();
    private final Set<String> tagSet = new HashSet<>();
    private WorkflowTagAdapter adapter;

    private List<Map<String, Object>> departmentsList = new ArrayList<>();
    private List<StorageLocation> locationsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workflow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerType = view.findViewById(R.id.spinner_workflow_type);
        spinnerDepartment = view.findViewById(R.id.spinner_workflow_department);
        spinnerToLocation = view.findViewById(R.id.spinner_workflow_to_location);
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
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 0: LaundrySend, 1: LaundryReceive, 2: InternalTransfer
                if (position == 2) {
                    containerDepartment.setVisibility(View.GONE);
                    containerTransfer.setVisibility(View.VISIBLE);
                } else {
                    containerDepartment.setVisibility(View.VISIBLE);
                    containerTransfer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadData() {
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
                    List<String> names = new ArrayList<>();
                    for (StorageLocation loc : locationsList) {
                        names.add(loc.name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerToLocation.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<StorageLocation>> call, Throwable t) {}
        });
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
        int typePos = spinnerType.getSelectedItemPosition();
        
        // Map UI pos to C# enum LaundrySend=4, LaundryReceive=5, InternalTransfer=6
        if (typePos == 0) request.type = 4;
        else if (typePos == 1) request.type = 5;
        else if (typePos == 2) request.type = 6;
        
        if (typePos == 2) {
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

        RetrofitClient.getApi().saveTransaction(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
                    clearWorkflow();
                } else {
                    Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearWorkflow() {
        scannedTags.clear();
        tagSet.clear();
        adapter.notifyDataSetChanged();
        textScannedCount.setText("Đã quét: 0 sản phẩm");
    }
}
