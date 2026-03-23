package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;
import beetech.tms.android.ui.adapters.WriteTagAdapter;
import beetech.tms.android.ui.models.WriteTagItem;

public class WriteTagFragment extends BaseFragment {

    private final List<WriteTagItem> items = new ArrayList<>();
    private final List<String> plannedEpCs = new ArrayList<>(); // queue to write
    private final List<String> expectedEpCs = new ArrayList<>(); // all target EPCs

    private final Set<String> writtenEpCs = new HashSet<>();
    private final Set<String> failedEpCs = new HashSet<>();
    private final Set<String> verifiedEpCs = new HashSet<>();

    private WriteTagAdapter adapter;
    private TextView txtProgress;
    private TextView tvInstruction;
    private Button btnFinish;

    private Mode currentMode = Mode.IDLE;
    private boolean isWriting = false;
    private final Object writeLock = new Object();

    enum Mode {
        IDLE, WRITING, CHECK_READY, CHECKING, DONE
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtProgress = view.findViewById(R.id.tv_progress);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        btnFinish = view.findViewById(R.id.btn_finish);

        RecyclerView recycler = view.findViewById(R.id.recycler_write_tags);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Handle arguments from FileListFragment
        Bundle args = getArguments();
        if (args != null) {
            String[] codes = args.getStringArray("itemData");
            if (codes != null) {
                for (String item : codes) {
                    // Expected format "EPC^Name^Code"
                    String[] parts = item.split("\\^");
                    String epc = parts[0].toUpperCase().trim();
                    String name = parts.length > 1 ? parts[1] : "";
                    String code = parts.length > 2 ? parts[2] : "";

                    plannedEpCs.add(epc);
                    expectedEpCs.add(epc);
                    items.add(new WriteTagItem(epc, name, code));
                }
            }
        }

        adapter = new WriteTagAdapter(items);
        recycler.setAdapter(adapter);

        if (items.isEmpty()) {
            tvInstruction.setText("Không có dữ liệu để ghi");
        } else {
            tvInstruction.setText("Bóp cò để bắt đầu ghi thẻ");
        }
        updateProgress();

        btnFinish.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        switch (currentMode) {
            case DONE:
                reader().stopInventory();
                return;
            case IDLE:
                currentMode = Mode.WRITING;
                tvInstruction.setText("Writing tags...");
                reader().startInventory();
                return;
            case CHECK_READY:
                currentMode = Mode.CHECKING;
                tvInstruction.setText("Checking tags...");
                reader().startInventory();
                return;
            default:
                reader().startInventory();
                break;
        }
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
        if (reader() != null) {
            reader().stopInventory();
        }
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        String epc = tagResult.epc;
        if (epc == null || isWriting)
            return;

        String normalized = epc.toUpperCase().trim();

        if (currentMode == Mode.WRITING) {
            if (!plannedEpCs.isEmpty()) {
                String targetEpc = plannedEpCs.get(0);
                try {
                    synchronized (writeLock) {
                        isWriting = true;
                        reader().stopInventory();
                        TagOperation op = reader().writeTagEpc(normalized, targetEpc);
                        isWriting = false;
                        reader().startInventory();

                        if (op != null && op.success) {
                            writtenEpCs.add(targetEpc);
                            plannedEpCs.remove(targetEpc);
                            failedEpCs.remove(targetEpc);

                            runOnUiThreadSafe(() -> adapter.updateItemByPlannedEpc(targetEpc, normalized, "Written"));
                        } else {
                            if (!failedEpCs.contains(targetEpc)) {
                                failedEpCs.add(targetEpc);
                                runOnUiThreadSafe(
                                        () -> adapter.updateItemByPlannedEpc(targetEpc, normalized, "Failed"));
                            }
                        }
                    }
                } catch (Exception e) {
                    isWriting = false;
                    if (!failedEpCs.contains(targetEpc)) {
                        failedEpCs.add(targetEpc);
                        runOnUiThreadSafe(() -> adapter.updateItemByPlannedEpc(targetEpc, normalized, "Failed"));
                    }
                }
            }
            runOnUiThreadSafe(this::updateProgress);

            if (plannedEpCs.isEmpty()) {
                reader().stopInventory();
                currentMode = Mode.CHECK_READY;
                runOnUiThreadSafe(() -> tvInstruction.setText("All tags written. Pull trigger to check/verify tags"));
            }
        } else if (currentMode == Mode.CHECKING) {
            if (expectedEpCs.contains(normalized) && !verifiedEpCs.contains(normalized)) {
                verifiedEpCs.add(normalized);
                runOnUiThreadSafe(() -> adapter.updateItemByPlannedEpc(normalized, normalized, "Verified"));
            }
            runOnUiThreadSafe(this::updateProgress);

            if (verifiedEpCs.size() == expectedEpCs.size()) {
                reader().stopInventory();
                currentMode = Mode.DONE;
                runOnUiThreadSafe(() -> {
                    tvInstruction.setText("Check complete.");
                    btnFinish.setVisibility(View.VISIBLE);
                    showSummaryDialog();
                });
            }
        }
    }

    private void updateProgress() {
        txtProgress.setText(
                "Written: " + writtenEpCs.size() +
                        " / " + items.size() +
                        " | Verified: " + verifiedEpCs.size());
    }

    private void showSummaryDialog() {
        StringBuilder message = new StringBuilder();
        int success = writtenEpCs.size();
        int failed = failedEpCs.size();
        int verified = verifiedEpCs.size();

        message.append("✅ Written: ").append(success)
                .append("\n❌ Failed: ").append(failed)
                .append("\n🔍 Verified: ").append(verified)
                .append("\n\nDetails:\n");

        for (WriteTagItem item : items) {
            message.append(item.plannedEpc)
                    .append(" - ")
                    .append(item.status != null ? item.status : "Pending")
                    .append("\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Write/Check Summary")
                .setMessage(message.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    // TODO: Could navigate back or reset
                })
                .show();
    }

    private void runOnUiThreadSafe(Runnable action) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(action);
        }
    }
}
