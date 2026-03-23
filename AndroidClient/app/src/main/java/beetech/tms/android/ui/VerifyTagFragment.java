package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.api.RetrofitClient;
import beetech.tms.android.data.models.TextileItem;
import beetech.tms.android.ui.adapters.VerifyTagAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyTagFragment extends BaseFragment {

    private VerifyTagAdapter adapter;
    private TextView tvInstruction, tvSummary;
    private Set<String> processingTags = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvSummary = view.findViewById(R.id.tv_summary);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_verify);
        view.findViewById(R.id.btn_finish).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        adapter = new VerifyTagAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        String epc = tagResult.epc;
        if (epc == null || epc.isEmpty())
            return;

        String normalizedEpc = epc.toUpperCase().trim();

        runOnUiThreadSafe(() -> {
            adapter.addOrUpdateTag(normalizedEpc);
            updateSummary();
            checkAndMapTag(normalizedEpc);
        });
    }

    private void checkAndMapTag(String epc) {
        // If already mapped or being processed, skip
        for (VerifyTagAdapter.VerifyItem item : adapter.getItems()) {
            if (item.epc.equalsIgnoreCase(epc) && item.isMapped)
                return;
        }
        if (processingTags.contains(epc))
            return;

        processingTags.add(epc);
        RetrofitClient.getApi().getItemByTag(epc).enqueue(new Callback<TextileItem>() {
            @Override
            public void onResponse(Call<TextileItem> call, Response<TextileItem> response) {
                processingTags.remove(epc);
                if (response.isSuccessful() && response.body() != null) {
                    TextileItem item = response.body();
                    String title = item.category;
                    String code = item.code;
                    int id = item.id;

                    runOnUiThreadSafe(() -> {
                        adapter.updateMapping(epc, title, code);
                        if (id > 0) {
                            updateItemStatusOnServer(id, "Verified");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<TextileItem> call, Throwable t) {
                processingTags.remove(epc);
            }
        });
    }

    private void updateItemStatusOnServer(int itemId, String status) {
        RetrofitClient.getApi().updateItemStatus(itemId, status).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Status updated on server
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Failed to update status
            }
        });
    }

    private void updateSummary() {
        int total = adapter.getItemCount();
        int mapped = 0;
        for (VerifyTagAdapter.VerifyItem item : adapter.getItems()) {
            if (item.isMapped)
                mapped++;
        }
        tvSummary.setText("Đã quét: " + total + " thẻ (" + mapped + " thẻ đã liên kết)");

        if (total > 0) {
            tvInstruction.setText("Đang quét và kiểm tra thông tin thẻ...");
        }
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(r);
        }
    }

    @Override
    public void onActivityKeyDown(int keyCode, android.view.KeyEvent event) {
        if (reader() != null) {
            reader().startInventory();
        }
    }

    @Override
    public void onActivityKeyUp(int keyCode, android.view.KeyEvent event) {
        if (reader() != null) {
            reader().stopInventory();
        }
    }
}
