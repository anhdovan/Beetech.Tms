package beetech.tms.android.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import beetech.tms.android.R;
import beetech.tms.android.ui.models.BulkRegisterItem;

public class BulkRegisterAdapter extends RecyclerView.Adapter<BulkRegisterAdapter.ViewHolder> {

    private final List<BulkRegisterItem> items;

    public BulkRegisterAdapter(List<BulkRegisterItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_write_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BulkRegisterItem item = items.get(position);
        holder.tvName.setText("Tag: " + item.currentEpc);
        holder.tvCode.setText(item.code != null ? "Mã: " + item.code : "Đợi đăng ký...");
        holder.tvStatus.setText(item.status);

        if (item.targetEpc != null) {
            holder.tvTargetEpc.setText("Target: " + item.targetEpc);
            holder.tvTargetEpc.setVisibility(View.VISIBLE);
        } else {
            holder.tvTargetEpc.setVisibility(View.GONE);
        }

        int color = Color.GRAY;
        switch (item.status) {
            case "Scanned": color = Color.BLUE; break;
            case "Registering": color = Color.parseColor("#FFA500"); break;
            case "Registered": color = Color.parseColor("#4CAF50"); break;
            case "Writing": color = Color.parseColor("#FFD700"); break;
            case "Success": color = Color.parseColor("#2E7D32"); break;
            case "Failed": color = Color.RED; break;
        }
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvStatus, tvTargetEpc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvCode = itemView.findViewById(R.id.tv_item_code);
            tvStatus = itemView.findViewById(R.id.tv_item_status);
            tvTargetEpc = itemView.findViewById(R.id.tv_target_epc);
        }
    }
}
