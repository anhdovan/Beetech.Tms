package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import beetech.tms.android.MainActivity;
import beetech.tms.android.R;

public class TagManagementFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.card_write_tag).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).selectItems(null);
            }
        });

        view.findViewById(R.id.card_map_tag).setOnClickListener(v -> {
            // Navigate to MapTagFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapTagFragment())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.card_bulk_register).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BulkRegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
