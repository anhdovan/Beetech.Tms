package beetech.app.core.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import beetech.app.core.dto.ScannedItemDto;
import beetech.app.rfidreader.R;


public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private final List<ScannedItemDto> items = new ArrayList<>();

    public void addItem(ScannedItemDto item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedItemDto item = items.get(position);

        holder.txtCode.setText(item.code);
        holder.txtName.setText(item.name);
        holder.txtInternalCode.setText(item.internalCode);
        holder.txtCondition.setText(item.condition);
        holder.txtLocation.setText(item.location);
        holder.txtCategory.setText(item.category);
        holder.txtDepartment.setText(item.department);
        holder.txtManufacturer.setText(item.manufacturer);

        if ("Unknown".equals(item.name)) {
            holder.chipStatus.setText("❌ Unknown");
            holder.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark))
            );
        } else {
            holder.chipStatus.setText("✅ Verified");
            holder.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark))
            );
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        Chip chipStatus;
        TextView txtCode, txtName, txtInternalCode, txtCondition,
                txtLocation, txtCategory, txtDepartment, txtManufacturer;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTag);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            txtCode = itemView.findViewById(R.id.txtCode);
            txtName = itemView.findViewById(R.id.txtName);
            txtInternalCode = itemView.findViewById(R.id.txtInternalCode);
            txtCondition = itemView.findViewById(R.id.txtCondition);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDepartment = itemView.findViewById(R.id.txtDepartment);
            txtManufacturer = itemView.findViewById(R.id.txtManufacturer);
        }
    }
}