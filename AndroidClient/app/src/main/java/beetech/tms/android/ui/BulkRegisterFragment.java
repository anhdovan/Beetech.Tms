package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.data.models.Category;
import beetech.tms.android.data.models.Department;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.ui.adapters.BulkRegisterAdapter;
import beetech.tms.android.ui.models.BulkRegisterItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BulkRegisterFragment extends BaseFragment {

    private final List<BulkRegisterItem> items = new ArrayList<>();
    private final Set<String> scannedEpCs = new HashSet<>();
    private BulkRegisterAdapter adapter;

    private Spinner spinnerCategory, spinnerLocation, spinnerDepartment;
    private TextView tvCountScanned, tvCountSuccess, tvInstruction;
    private MaterialButton btnSubmit, btnClear;

    private List<Category> categoriesList = new ArrayList<>();
    private List<StorageLocation> locationsList = new ArrayList<>();
    private List<Map<String, Object>> departmentsList = new ArrayList<>();

    private Mode currentMode = Mode.SCANNING;
    private boolean isBusy = false;
    private boolean isWriting = false;
    private final Object writeLock = new Object();

    enum Mode {
        SCANNING, WRITING, DONE
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bulk_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCategory = view.findViewById(R.id.spinner_reg_category);
        spinnerLocation = view.findViewById(R.id.spinner_reg_location);
        spinnerDepartment = view.findViewById(R.id.spinner_reg_department);

        tvCountScanned = view.findViewById(R.id.tv_count_scanned);
        tvCountSuccess = view.findViewById(R.id.tv_count_success);
        tvInstruction = view.findViewById(R.id.tv_reg_instruction);

        btnSubmit = view.findViewById(R.id.btn_reg_submit);
        btnClear = view.findViewById(R.id.btn_reg_clear);

        RecyclerView recycler = view.findViewById(R.id.recycler_reg_items);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BulkRegisterAdapter(items);
        recycler.setAdapter(adapter);

        btnClear.setOnClickListener(v -> clearAll());
        btnSubmit.setOnClickListener(v -> startRegistration());

        loadInitialData();
    }

    private void loadInitialData() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<TmsApi.CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<TmsApi.CategoryResponse>> call, Response<List<TmsApi.CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> names = new ArrayList<>();
                    names.add("-- Chọn loại (Bắt buộc) --");
                    for (TmsApi.CategoryResponse c : response.body()) {
                        Category cat = new Category(); cat.id = c.id; cat.name = c.name;
                        categoriesList.add(cat);
                        names.add(c.name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<TmsApi.CategoryResponse>> call, Throwable t) {}
        });

        RetrofitClient.getApi().getLocations().enqueue(new Callback<List<StorageLocation>>() {
            @Override
            public void onResponse(Call<List<StorageLocation>> call, Response<List<StorageLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationsList = response.body();
                    List<String> names = new ArrayList<>();
                    names.add("-- Chọn vị trí (Tùy chọn) --");
                    for (StorageLocation l : locationsList) names.add(l.name);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLocation.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<StorageLocation>> call, Throwable t) {}
        });

        RetrofitClient.getApi().getDepartments().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departmentsList = response.body();
                    List<String> names = new ArrayList<>();
                    names.add("-- Chọn bộ phận (Tùy chọn) --");
                    for (Map<String, Object> d : departmentsList) names.add(String.valueOf(d.get("name")));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartment.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        if (currentMode == Mode.DONE || isBusy) return;
        reader().startInventory();
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
        reader().stopInventory();
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        if (tagResult.epc == null || isBusy || isWriting) return;
        String normalized = tagResult.epc.toUpperCase().trim();

        if (currentMode == Mode.SCANNING) {
            if (!scannedEpCs.contains(normalized)) {
                scannedEpCs.add(normalized);
                runOnUiThreadSafe(() -> {
                    items.add(new BulkRegisterItem(normalized));
                    adapter.notifyItemInserted(items.size() - 1);
                    tvCountScanned.setText(String.valueOf(items.size()));
                });
            }
        } else if (currentMode == Mode.WRITING) {
            BulkRegisterItem targetItem = null;
            for (BulkRegisterItem item : items) {
                if ("Registered".equals(item.status)) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem != null) {
                final BulkRegisterItem finalItem = targetItem;
                try {
                    synchronized (writeLock) {
                        isWriting = true;
                        reader().stopInventory();
                        TagOperation op = reader().writeTagEpc(normalized, finalItem.targetEpc);
                        isWriting = false;
                        reader().startInventory();

                        if (op != null && op.success) {
                            runOnUiThreadSafe(() -> {
                                finalItem.status = "Success";
                                adapter.notifyDataSetChanged();
                                updateWrittenCount();
                                checkProgress();
                            });
                        } else {
                            runOnUiThreadSafe(() -> {
                                finalItem.status = "Failed";
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                } catch (Exception e) {
                    isWriting = false;
                }
            }
        }
    }

    private void updateWrittenCount() {
        long count = 0;
        for (BulkRegisterItem item : items) if ("Success".equals(item.status)) count++;
        tvCountSuccess.setText(String.valueOf(count));
    }

    private void checkProgress() {
        boolean allDone = true;
        for (BulkRegisterItem item : items) {
            if ("Registered".equals(item.status) || "Writing".equals(item.status) || "Failed".equals(item.status)) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            currentMode = Mode.DONE;
            reader().stopInventory();
            runOnUiThreadSafe(() -> {
                tvInstruction.setText("Ghi thẻ hoàn thành! Đang xác nhận với hệ thống...");
                confirmRegistration();
            });
        }
    }

    private void startRegistration() {
        if (items.isEmpty()) {
            Toast.makeText(getContext(), "Chưa quét thẻ nào", Toast.LENGTH_SHORT).show();
            return;
        }

        int catPos = spinnerCategory.getSelectedItemPosition();
        if (catPos <= 0) {
            Toast.makeText(getContext(), "Vui lòng chọn loại", Toast.LENGTH_SHORT).show();
            return;
        }

        isBusy = true;
        tvInstruction.setText("Đang đăng ký hệ thống...");
        
        TmsApi.BulkRegisterRequest request = new TmsApi.BulkRegisterRequest();
        request.categoryId = categoriesList.get(catPos - 1).id;
        
        int locPos = spinnerLocation.getSelectedItemPosition();
        if (locPos > 0) request.locationId = locationsList.get(locPos - 1).id;
        
        int deptPos = spinnerDepartment.getSelectedItemPosition();
        if (deptPos > 0) request.departmentId = (Integer) departmentsList.get(deptPos - 1).get("id");
        
        request.count = items.size();

        RetrofitClient.getApi().bulkRegister(request).enqueue(new Callback<List<TmsApi.ItemRegistrationResponse>>() {
            @Override
            public void onResponse(Call<List<TmsApi.ItemRegistrationResponse>> call, Response<List<TmsApi.ItemRegistrationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TmsApi.ItemRegistrationResponse> regItems = response.body();
                    for (int i = 0; i < items.size() && i < regItems.size(); i++) {
                        BulkRegisterItem item = items.get(i);
                        TmsApi.ItemRegistrationResponse res = regItems.get(i);
                        item.itemId = res.id;
                        item.code = res.code;
                        item.targetEpc = generateEpcFromCode(res.code); // Or use Chacha20 if available
                        item.status = "Registered";
                    }
                    currentMode = Mode.WRITING;
                    isBusy = false;
                    runOnUiThreadSafe(() -> {
                        adapter.notifyDataSetChanged();
                        tvInstruction.setText("Đã đăng ký. Bóp cò và đưa thẻ lại gần để ghi.");
                    });
                } else {
                    isBusy = false;
                    Toast.makeText(getContext(), "Đăng ký hệ thống lỗi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TmsApi.ItemRegistrationResponse>> call, Throwable t) {
                isBusy = false;
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmRegistration() {
        List<Integer> ids = new ArrayList<>();
        for (BulkRegisterItem item : items) if ("Success".equals(item.status)) ids.add(item.itemId);

        if (ids.isEmpty()) {
            tvInstruction.setText("Không có thẻ nào được ghi thành công.");
            return;
        }

        TmsApi.ConfirmRegistrationRequest request = new TmsApi.ConfirmRegistrationRequest();
        request.itemIds = ids;

        RetrofitClient.getApi().confirmRegistration(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnUiThreadSafe(() -> {
                    tvInstruction.setText("Xác nhận hoàn tất!");
                    showSummaryDialog();
                });
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThreadSafe(() -> tvInstruction.setText("Xác nhận hệ thống lỗi, vui lòng thử lại sau."));
            }
        });
    }

    private String generateEpcFromCode(String code) {
        // Implementation should match Chacha20 logic if possible, 
        // for now we use a simple hex conversion or assume code is hex-compatible
        try {
            // Simplified: padding with zeros
            StringBuilder sb = new StringBuilder(code);
            while (sb.length() < 24) sb.append("0");
            return sb.toString().toUpperCase();
        } catch (Exception e) { return code; }
    }

    private void showSummaryDialog() {
        int success = 0;
        for (BulkRegisterItem item : items) if ("Success".equals(item.status)) success++;

        new AlertDialog.Builder(requireContext())
                .setTitle("Kết quả đăng ký")
                .setMessage("Đăng ký thành công: " + success + " / " + items.size())
                .setPositiveButton("OK", (d, w) -> clearAll())
                .show();
    }

    private void clearAll() {
        items.clear();
        scannedEpCs.clear();
        currentMode = Mode.SCANNING;
        isBusy = false;
        runOnUiThreadSafe(() -> {
            adapter.notifyDataSetChanged();
            tvCountScanned.setText("0");
            tvCountSuccess.setText("0");
            tvInstruction.setText("Bóp cò để quét các thẻ cần đăng ký");
        });
    }

    private void runOnUiThreadSafe(Runnable action) {
        if (isAdded() && getActivity() != null) getActivity().runOnUiThread(action);
    }
}
