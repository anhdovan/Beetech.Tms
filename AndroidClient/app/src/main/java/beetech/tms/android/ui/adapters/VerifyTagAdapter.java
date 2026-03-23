package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import beetech.tms.android.R;

public class VerifyTagAdapter extends RecyclerView.Adapter<VerifyTagAdapter.VerifyViewHolder> {

    public static class VerifyItem {
        public String epc;
        public String fileTitle;
        public String fileCode;
        public int count = 1;
        public boolean isMapped = false;

        public VerifyItem(String epc) {
            this.epc = epc;
        }

        public void updateInfo(String title, String code) {
            this.fileTitle = title;
            this.fileCode = code;
            this.isMapped = true;
        }
    }

    private List<VerifyItem> items = new ArrayList<>();

    public void addOrUpdateTag(String epc) {
        for (VerifyItem item : items) {
            if (item.epc.equalsIgnoreCase(epc)) {
                item.count++;
                notifyItemChanged(items.indexOf(item));
                return;
            }
        }
        items.add(new VerifyItem(epc));
        notifyItemInserted(items.size() - 1);
    }

    public void updateMapping(String epc, String title, String code) {
        for (int i = 0; i < items.size(); i++) {
            VerifyItem item = items.get(i);
            if (item.epc.equalsIgnoreCase(epc)) {
                item.updateInfo(title, code);
                // Sort: Mapped items to the top
                sortItems();
                return;
            }
        }
    }

    private void sortItems() {
        Collections.sort(items, (a, b) -> {
            if (a.isMapped && !b.isMapped)
                return -1;
            if (!a.isMapped && b.isMapped)
                return 1;
            return 0;
        });
        notifyDataSetChanged();
    }

    public List<VerifyItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VerifyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_verify_tag, parent, false);
        return new VerifyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VerifyViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VerifyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBadge;
        TextView tvEpc, tvFileInfo, tvCount;

        public VerifyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBadge = itemView.findViewById(R.id.iv_badge);
            tvEpc = itemView.findViewById(R.id.tv_epc);
            tvFileInfo = itemView.findViewById(R.id.tv_file_info);
            tvCount = itemView.findViewById(R.id.tv_count);
        }

        public void bind(VerifyItem item) {
            tvEpc.setText(item.epc);
            tvCount.setText("x" + item.count);

            if (item.isMapped) {
                ivBadge.setImageResource(android.R.drawable.presence_online);
                ivBadge.setColorFilter(itemView.getContext().getColor(android.R.color.holo_green_dark));
                tvFileInfo.setText(item.fileTitle + " (" + item.fileCode + ")");
                tvFileInfo.setAlpha(1.0f);
            } else {
                ivBadge.setImageResource(android.R.drawable.presence_busy);
                ivBadge.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_dark));
                tvFileInfo.setText("Chưa liên kết hồ sơ");
                tvFileInfo.setAlpha(0.7f);
            }
        }
    }
}
