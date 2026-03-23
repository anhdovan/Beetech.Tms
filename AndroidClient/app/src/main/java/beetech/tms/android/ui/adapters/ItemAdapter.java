package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import beetech.tms.android.R;
import beetech.tms.android.data.models.TextileItem;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<TextileItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isSelectionMode = false;

    public interface OnItemClickListener {
        void onItemClick(TextileItem item);
        void onItemLongClick(TextileItem item);
        void onWriteRfidClick(TextileItem item);
        void onLocateRfidClick(TextileItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TextileItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        TextileItem item = items.get(position);
        holder.bind(item, isSelectionMode, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageStatus;
        TextView textFileCode, textTitle, textCategory, textRfidCode;
        CheckBox checkboxSelect;
        View btnWriteRfid, btnLocateRfid;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            imageStatus = itemView.findViewById(R.id.image_status);
            textFileCode = itemView.findViewById(R.id.text_file_code);
            textTitle = itemView.findViewById(R.id.text_title);
            textCategory = itemView.findViewById(R.id.text_customer); // Reusing ID for Category
            textRfidCode = itemView.findViewById(R.id.text_rfid_code);
            checkboxSelect = itemView.findViewById(R.id.checkbox_select);
            btnWriteRfid = itemView.findViewById(R.id.btn_write_rfid);
            btnLocateRfid = itemView.findViewById(R.id.btn_locate_rfid);
        }

        public void bind(TextileItem item, boolean isSelectionMode, OnItemClickListener listener) {
            textFileCode.setText(item.code);
            textTitle.setText(item.category);
            textCategory.setText("Vị trí: " + (item.location != null ? item.location : "N/A"));
            if (textRfidCode != null) {
                textRfidCode.setText(item.epc != null && !item.epc.isEmpty() ? item.epc : item.getExpectedEpc());
            }

            // Status icon and color
            if ("Active".equalsIgnoreCase(item.status)) {
                imageStatus.setImageResource(android.R.drawable.presence_online);
                imageStatus.setColorFilter(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if ("Lost".equalsIgnoreCase(item.status)) {
                imageStatus.setImageResource(android.R.drawable.presence_busy);
                imageStatus.setColorFilter(itemView.getContext().getColor(android.R.color.holo_red_dark));
            } else {
                imageStatus.setImageResource(android.R.drawable.presence_away);
                imageStatus.setColorFilter(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            }

            // Selection state
            checkboxSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            checkboxSelect.setChecked(item.isSelected());
            cardView.setChecked(item.isSelected());

            // Hide action buttons in selection mode
            int actionVisibility = isSelectionMode ? View.GONE : View.VISIBLE;
            if (btnWriteRfid != null) {
                btnWriteRfid.setVisibility(actionVisibility);
                btnWriteRfid.setOnClickListener(v -> {
                    if (listener != null) listener.onWriteRfidClick(item);
                });
            }
            if (btnLocateRfid != null) {
                btnLocateRfid.setVisibility(actionVisibility);
                btnLocateRfid.setOnClickListener(v -> {
                    if (listener != null) listener.onLocateRfidClick(item);
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(item);
                    return true;
                }
                return false;
            });
        }
    }
}
