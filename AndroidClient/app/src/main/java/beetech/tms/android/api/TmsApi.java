package beetech.tms.android.api;

import java.util.List;
import java.util.Map;

import beetech.tms.android.data.models.StorageLocation;
import beetech.tms.android.data.models.TextileItem;
import beetech.tms.android.data.models.InventoryAuditResult;
import beetech.tms.android.data.models.InventoryAuditSession;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmsApi {

    @POST("api/mobile/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/mobile/locations")
    Call<List<StorageLocation>> getLocations();

    @GET("api/mobile/categories")
    Call<List<CategoryResponse>> getCategories();
    
    class CategoryResponse {
        public int id;
        public String name;
    }

    @GET("api/mobile/items")
    Call<List<TextileItem>> getItems(
            @Query("search") String search,
            @Query("locationId") Integer locationId,
            @Query("departmentId") Integer departmentId);

    @GET("api/mobile/departments")
    Call<List<Map<String, Object>>> getDepartments();

    @GET("api/mobile/items/by-tag/{epc}")
    Call<TextileItem> getItemByTag(@Path("epc") String epc);

    @POST("api/mobile/transaction")
    Call<Void> saveTransaction(@Body MobileTransactionRequest request);

    @POST("api/mobile/inventory/session")
    Call<InventoryAuditSession> saveInventorySession(@Body InventoryAuditSession session);

    @POST("api/mobile/inventory/records-batch")
    Call<Map<String, Object>> saveInventoryRecordsBatch(@Body List<InventoryAuditResult> records);

    @POST("api/mobile/items/map-tag")
    Call<Map<String, Object>> mapTag(@Body MapTagRequest request);

    @POST("api/mobile/items/{id}/status")
    Call<Void> updateItemStatus(@Path("id") int id, @Query("status") String status);

    @POST("api/mobile/items/bulk-register")
    Call<List<ItemRegistrationResponse>> bulkRegister(@Body BulkRegisterRequest request);

    @POST("api/mobile/items/confirm-registration")
    Call<Void> confirmRegistration(@Body ConfirmRegistrationRequest request);

    class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    class LoginResponse {
        public String token;
        public String username;
        public String fullName;
        public String role;
    }

    class MobileTransactionRequest {
        public String type; // TransactionType enum name
        public Integer fromLocationId;
        public Integer toLocationId;
        public Integer departmentId;
        public List<String> epcs;
        public String notes;
    }

    class MapTagRequest {
        public String epc;
        public String itemCode;

        public MapTagRequest(String epc, String itemCode) {
            this.epc = epc;
            this.itemCode = itemCode;
        }
    }

    class BulkRegisterRequest {
        public int categoryId;
        public Integer locationId;
        public Integer departmentId;
        public int count;
    }

    class ItemRegistrationResponse {
        public int id;
        public String code;
    }

    class ConfirmRegistrationRequest {
        public List<Integer> itemIds;
    }
}
