package beetech.tms.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import beetech.tms.android.R;

public class WorkflowTagAdapter extends RecyclerView.Adapter<WorkflowTagAdapter.ViewHolder> {

    private final List<String> tags;

    public WorkflowTagAdapter(List<String> tags) {
        this.tags = tags;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(tags.get(position));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View v) {
            super(v);
            textView = v.findViewById(android.R.id.text1);
            textView.setTextSize(14f);
        }
    }
}
