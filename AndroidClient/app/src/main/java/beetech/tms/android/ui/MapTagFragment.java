package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.TmsApi;
import beetech.tms.android.api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapTagFragment extends BaseFragment {

    private TextView textScannedEpc;
    private EditText editItemCode;
    private View btnMapConfirm;
    
    private String currentEpc = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textScannedEpc = view.findViewById(R.id.text_scanned_epc);
        editItemCode = view.findViewById(R.id.edit_item_code);
        btnMapConfirm = view.findViewById(R.id.btn_map_confirm);

        btnMapConfirm.setOnClickListener(v -> executeMapTag());
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        String epc = tagResult.epc;
        if (epc != null) {
            currentEpc = epc.toUpperCase().trim();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    textScannedEpc.setText(currentEpc);
                    textScannedEpc.setBackgroundColor(0xFFE8F5E9); // Light green
                });
            }
        }
    }

    @Override
    public void onBarcodeScanned(String barcode) {
        if (barcode != null) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    editItemCode.setText(barcode);
                    Toast.makeText(getContext(), "Đã quét Barcode", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        if (reader() != null) {
            int action = translateKeyCode(keyCode, reader().getClass());
            if (action == 2) { // RFID Trigger
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

    private void executeMapTag() {
        String itemCode = editItemCode.getText().toString().trim();
        
        if (currentEpc.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng quét thẻ RFID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (itemCode.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng quét mã sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        TmsApi.MapTagRequest request = new TmsApi.MapTagRequest(currentEpc, itemCode);
        
        RetrofitClient.getApi().mapTag(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Gán thẻ thành công", Toast.LENGTH_SHORT).show();
                    resetForm();
                } else {
                    Toast.makeText(getContext(), "Lỗi gán thẻ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        currentEpc = "";
        textScannedEpc.setText("");
        textScannedEpc.setBackgroundColor(0xFFF5F5F5);
        editItemCode.setText("");
    }
}
