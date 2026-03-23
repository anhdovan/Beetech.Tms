package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import beetech.tms.android.R;
import beetech.tms.android.data.models.Customer;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<Customer> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Customer item);
    }

    public CustomerAdapter(List<Customer> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setData(List<Customer> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCode.setText(item.getCustomerCode());

        // Set icon based on type
        if ("Tổ chức".equalsIgnoreCase(item.getType())) {
            holder.ivType.setImageResource(R.drawable.ic_organization);
        } else {
            holder.ivType.setImageResource(R.drawable.ic_user);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode;
        android.widget.ImageView ivType;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.text_customer_name);
            tvCode = v.findViewById(R.id.text_customer_code);
            ivType = v.findViewById(R.id.image_customer_type);
        }
    }
}
