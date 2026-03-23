package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import beetech.tms.android.R;
import beetech.tms.android.ui.models.WriteTagItem;

public class WriteTagAdapter extends RecyclerView.Adapter<WriteTagAdapter.ViewHolder> {

    private final List<WriteTagItem> items;

    public WriteTagAdapter(List<WriteTagItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_write_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WriteTagItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvCode.setText(item.code);
        holder.tvTargetEpc.setText("Target: " + item.plannedEpc);
        holder.tvStatus.setText(item.status);

        // Color coding based on status
        int color;
        switch (item.status) {
            case "Written":
                color = 0xFF4CAF50; // Green
                break;
            case "Failed":
                color = 0xFFF44336; // Red
                break;
            case "Verified":
                color = 0xFF2196F3; // Blue
                break;
            default:
                color = 0xFF757575; // Gray
                break;
        }
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItemByPlannedEpc(String plannedEpc, String currentEpc, String status) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).plannedEpc.equals(plannedEpc)) {
                items.get(i).currentEpc = currentEpc;
                items.get(i).status = status;
                notifyItemChanged(i);
                return;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvTargetEpc, tvStatus;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_item_name);
            tvCode = view.findViewById(R.id.tv_item_code);
            tvTargetEpc = view.findViewById(R.id.tv_target_epc);
            tvStatus = view.findViewById(R.id.tv_item_status);
        }
    }
}
