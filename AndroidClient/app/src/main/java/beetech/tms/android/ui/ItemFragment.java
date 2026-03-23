package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import beetech.tms.android.MainActivity;
import beetech.tms.android.R;
import beetech.tms.android.data.models.TextileItem;
import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.ui.adapters.ItemAdapter;

public class ItemFragment extends BaseFragment {

    private ItemViewModel viewModel;
    private ItemAdapter adapter;
    private RecyclerView recyclerView;
    private Spinner spinnerLocation;
    private Spinner spinnerDepartment;
    private SearchView searchView;
    private FloatingActionButton fabWriteAll;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_view_items);
        spinnerLocation = view.findViewById(R.id.spinner_location);
        spinnerDepartment = view.findViewById(R.id.spinner_department);
        searchView = view.findViewById(R.id.search_view);
        fabWriteAll = view.findViewById(R.id.fab_write_all);

        setupRecyclerView();
        setupFilters();
        observeViewModel();

        viewModel.loadLocations();
        viewModel.loadDepartments();
        viewModel.loadItems();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter();
        adapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TextileItem item) {
                if (viewModel.getSelectedCount().getValue() > 0) {
                    viewModel.toggleSelection(item);
                } else {
                    // TODO: Show details
                    Toast.makeText(getContext(), item.code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(TextileItem item) {
                viewModel.toggleSelection(item);
                adapter.setSelectionMode(true);
            }

            @Override
            public void onWriteRfidClick(TextileItem item) {
                navigateToWriteTag(new TextileItem[] { item });
            }

            @Override
            public void onLocateRfidClick(TextileItem item) {
                navigateToLocate(item);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Integer locationId = null;
                if (position > 0) {
                    List<StorageLocation> locs = viewModel.getLocations().getValue();
                    if (locs != null && position <= locs.size()) {
                        locationId = locs.get(position - 1).id;
                    }
                }
                viewModel.setLocationFilter(locationId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Integer deptId = null;
                if (position > 0) {
                    List<Map<String, Object>> depts = viewModel.getDepartments().getValue();
                    if (depts != null && position <= depts.size()) {
                        deptId = ((Double) depts.get(position - 1).get("id")).intValue();
                    }
                }
                viewModel.setDepartmentFilter(deptId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.removeCallbacks(searchRunnable);
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> viewModel.setSearchQuery(newText);
                searchHandler.postDelayed(searchRunnable, 500);
                return true;
            }
        });

        fabWriteAll.setOnClickListener(v -> confirmWriteAll());
    }

    private void observeViewModel() {
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            fabWriteAll.setVisibility(items != null && !items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations == null) return;
            List<String> names = new ArrayList<>();
            names.add("Tất cả vị trí");
            for (StorageLocation loc : locations) {
                names.add(loc.name);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLocation.setAdapter(adapter);
        });

        viewModel.getDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments == null) return;
            List<String> names = new ArrayList<>();
            names.add("Tất cả phòng ban");
            for (Map<String, Object> dept : departments) {
                names.add((String) dept.get("name"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDepartment.setAdapter(adapter);
        });

        viewModel.getSelectedCount().observe(getViewLifecycleOwner(), count -> {
            adapter.setSelectionMode(count > 0);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmWriteAll() {
        int count = viewModel.getSelectedCount().getValue() != null ? viewModel.getSelectedCount().getValue() : 0;
        String message;
        if (count > 0) {
            message = "Bạn có chắc chắn muốn ghi thẻ cho " + count + " sản phẩm đã chọn?";
        } else {
            int total = viewModel.getItems().getValue() != null ? viewModel.getItems().getValue().size() : 0;
            if (total == 0) return;
            message = "Bạn có chắc chắn muốn ghi thẻ cho tất cả " + total + " sản phẩm đang hiển thị?";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận ghi thẻ hàng loạt")
                .setMessage(message)
                .setPositiveButton("Ghi thẻ", (dialog, which) -> executeWriteAll())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeWriteAll() {
        List<TextileItem> allItems = viewModel.getItems().getValue();
        if (allItems == null || allItems.isEmpty()) return;

        List<TextileItem> selectedItems = viewModel.getSelectedItems();
        if (selectedItems.isEmpty()) {
            selectedItems.addAll(allItems);
        }

        navigateToWriteTag(selectedItems.toArray(new TextileItem[0]));
        viewModel.clearSelection();
    }

    private void navigateToWriteTag(TextileItem[] items) {
        if (items == null || items.length == 0) return;

        String[] data = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            // Format: EPC^Name^Code
            data[i] = items[i].getExpectedEpc() + "^" + items[i].category + "^" + items[i].code;
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray("itemData", data);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectWriteTag(bundle);
        }
    }

    private void navigateToLocate(TextileItem item) {
        Bundle bundle = new Bundle();
        bundle.putString("targetEpc", item.getExpectedEpc());
        bundle.putString("itemCode", item.code);
        bundle.putString("itemName", item.category);

        LocateFragment fragment = new LocateFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
