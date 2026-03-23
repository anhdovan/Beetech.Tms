package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import beetech.tms.android.R;

public class AuditResultAdapter extends RecyclerView.Adapter<AuditResultAdapter.ViewHolder> {

    public enum TagStatus {
        OK, MISSING, UNEXPECTED, UNKNOWN
    }

    public static class AuditItem {
        public String epc;
        public String title;
        public String code;
        public TagStatus status;

        public AuditItem(String epc, String title, String code, TagStatus status) {
            this.epc = epc;
            this.title = title;
            this.code = code;
            this.status = status;
        }
    }

    private final List<AuditItem> items;

    public AuditResultAdapter(List<AuditItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audit_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditItem item = items.get(position);
        holder.tvEpc.setText(item.epc);
        holder.tvTitle.setText(item.title != null ? item.title : "Không xác định");
        
        String statusText = "Không xác định";
        int color = 0xFF757575; // Gray
        
        switch (item.status) {
            case OK:
                statusText = "Khớp";
                color = 0xFF4CAF50; // Green
                break;
            case MISSING:
                statusText = "Thiếu";
                color = 0xFFF44336; // Red
                break;
            case UNEXPECTED:
                statusText = "Sai vị trí";
                color = 0xFFFF9800; // Orange
                break;
            case UNKNOWN:
                statusText = "Lạ";
                color = 0xFF757575; // Gray
                break;
        }
        
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEpc, tvTitle, tvStatus;

        ViewHolder(View v) {
            super(v);
            tvEpc = v.findViewById(R.id.tv_audit_epc);
            tvTitle = v.findViewById(R.id.tv_audit_title);
            tvStatus = v.findViewById(R.id.tv_audit_status);
        }
    }
}
