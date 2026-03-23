package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import beetech.app.core.AdvBaseReader;
import beetech.tms.android.MainActivity;
import beetech.tms.android.R;
import beetech.tms.android.utils.SettingsManager;

public class SettingsFragment extends BaseFragment {

    private EditText etServerUrl;
    private SeekBar sbReaderPower;
    private TextView tvPowerValue;
    private MaterialSwitch switchBeep;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etServerUrl = view.findViewById(R.id.et_server_url);
        sbReaderPower = view.findViewById(R.id.sb_reader_power);
        tvPowerValue = view.findViewById(R.id.tv_power_value);
        switchBeep = view.findViewById(R.id.switch_beep);
        btnSave = view.findViewById(R.id.btn_save_settings);

        loadSettings();

        sbReaderPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPowerValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SettingsManager settings = SettingsManager.getInstance();
        etServerUrl.setText(settings.getServerUrl());
        sbReaderPower.setProgress(settings.getReaderPower());
        tvPowerValue.setText(String.valueOf(settings.getReaderPower()));
        switchBeep.setChecked(settings.isBeepEnabled());
    }

    private void saveSettings() {
        String url = etServerUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(requireContext(), "URL không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        int power = sbReaderPower.getProgress();
        boolean beep = switchBeep.isChecked();

        SettingsManager settings = SettingsManager.getInstance();
        settings.setServerUrl(url);
        settings.setReaderPower(power);
        settings.setBeepEnabled(beep);

        // Apply hardware settings immediately if reader is available
        if (requireActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) requireActivity();
            AdvBaseReader reader = main.getReader();
            if (reader != null) {
                reader.setPower(power);
                reader.isMute = !beep;
            }
        }

        Toast.makeText(requireContext(), "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show();
    }
}
