package beetech.tms.android.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.models.TextileItem;
import beetech.tms.android.data.models.StorageLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<List<TextileItem>> items = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StorageLocation>> locations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Map<String, Object>>> departments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);

    private String currentSearch = "";
    private Integer selectedLocationId = null;
    private Integer selectedDepartmentId = null;

    public LiveData<List<TextileItem>> getItems() {
        return items;
    }

    public LiveData<List<StorageLocation>> getLocations() {
        return locations;
    }

    public LiveData<List<Map<String, Object>>> getDepartments() {
        return departments;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    public void loadLocations() {
        RetrofitClient.getApi().getLocations().enqueue(new Callback<List<StorageLocation>>() {
            @Override
            public void onResponse(Call<List<StorageLocation>> call, Response<List<StorageLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locations.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<StorageLocation>> call, Throwable t) {
            }
        });
    }

    public void loadDepartments() {
        RetrofitClient.getApi().getDepartments().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departments.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
            }
        });
    }

    public void loadItems() {
        isLoading.setValue(true);
        RetrofitClient.getApi().getItems(currentSearch, selectedLocationId, selectedDepartmentId)
                .enqueue(new Callback<List<TextileItem>>() {
                    @Override
                    public void onResponse(Call<List<TextileItem>> call, Response<List<TextileItem>> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            items.setValue(response.body());
                            updateSelectedCount();
                        } else {
                            errorMessage.setValue("Lỗi tải dữ liệu: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TextileItem>> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    public void setSearchQuery(String query) {
        this.currentSearch = query;
        loadItems();
    }

    public void setLocationFilter(Integer locationId) {
        this.selectedLocationId = locationId;
        loadItems();
    }

    public void setDepartmentFilter(Integer departmentId) {
        this.selectedDepartmentId = departmentId;
        loadItems();
    }

    public void toggleSelection(TextileItem item) {
        item.setSelected(!item.isSelected());
        updateSelectedCount();
        items.setValue(items.getValue());
    }

    public void clearSelection() {
        List<TextileItem> currentItems = items.getValue();
        if (currentItems != null) {
            for (TextileItem item : currentItems) {
                item.setSelected(false);
            }
            items.setValue(currentItems);
            updateSelectedCount();
        }
    }

    private void updateSelectedCount() {
        List<TextileItem> currentItems = items.getValue();
        int count = 0;
        if (currentItems != null) {
            for (TextileItem item : currentItems) {
                if (item.isSelected()) count++;
            }
        }
        selectedCount.setValue(count);
    }

    public List<TextileItem> getSelectedItems() {
        List<TextileItem> selected = new ArrayList<>();
        List<TextileItem> currentItems = items.getValue();
        if (currentItems != null) {
            for (TextileItem item : currentItems) {
                if (item.isSelected()) selected.add(item);
            }
        }
        return selected;
    }
}
