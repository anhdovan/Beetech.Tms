package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.media.AudioManager;
import android.media.ToneGenerator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

import beetech.app.core.dto.TagResult;
import beetech.tms.android.R;

public class LocateFragment extends BaseFragment {

    private EditText etTargetEpc;
    private TextView tvSignalPercent, tvStatus, tvFileCode, tvFileTitle;
    private ProgressBar progressSignal;

    private String targetEpc;
    private int currentRssi = 0;
    private boolean isLocating = false;
    private volatile int currentPercent = 0;

    // Geiger sound components
    private ToneGenerator toneGenerator;
    private ScheduledExecutorService geigerExecutor;
    private long lastBeepTime = 0;

    // RSSI Range for mapping to 0-100%
    private static final int MIN_RSSI = -90;
    private static final int MAX_RSSI = -40; // -30 is very close
    private Thread geigerThread;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTargetEpc = view.findViewById(R.id.et_target_epc);
        tvSignalPercent = view.findViewById(R.id.tv_signal_percent);
        tvStatus = view.findViewById(R.id.tv_locate_status);
        progressSignal = view.findViewById(R.id.progress_signal);
        tvFileCode = view.findViewById(R.id.tv_file_code);
        tvFileTitle = view.findViewById(R.id.tv_file_title);

        setupAnimations();

        // Check for args (navigated from FileList or similar)
        if (getArguments() != null) {
            String epc = getArguments().getString("targetEpc");
            String code = getArguments().getString("itemCode");
            String title = getArguments().getString("itemName");

            if (epc != null) {
                etTargetEpc.setText(epc);
                targetEpc = epc.toUpperCase().trim();
            }
            if (code != null) {
                tvFileCode.setText("Code: " + code);
            }
            if (title != null) {
                tvFileTitle.setText(title);
            }
        }
    }

    private void setupAnimations() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            android.util.Log.e("LocateFragment", "Failed to init ToneGenerator", e);
        }
    }

    private void startGeigerLoop() {
        stopGeigerLoop();
        geigerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.i("Geiger", "Geiger Thread Started with MAX_PRIORITY");
                while (isLocating && !Thread.currentThread().isInterrupted()) {
                    // Constant rhythm calculation: 100% -> 50ms interval, 0% -> 1000ms interval
                    // This ensures even at 0%, we still hear 1 search-beep per second.
                    long interval = 1000;
                    if (currentPercent > 0) {
                        interval = (long) (1000 - (currentPercent * 9.5));
                        if (interval < 50) interval = 50;
                    }

                    if (toneGenerator != null) {
                        android.util.Log.v("Geiger", "BEEP! percent=" + currentPercent + " interval=" + interval);
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 40);
                    }

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        break;
                    }
                    
                    // Ultra-slow decay: Maintain the current signal rhythm for longer between hardware reads
                    // Reduces percent by 1 unit per beat (approx 1 unit/sec at 0%, 20 units/sec at 100%)
                    currentPercent = Math.max(0, currentPercent - 1);
                }
                android.util.Log.i("Geiger", "Geiger Thread Stopped");
            }
        });
        geigerThread.setPriority(Thread.MAX_PRIORITY);
        geigerThread.start();
    }

    private void stopGeigerLoop() {
        if (geigerThread != null) {
            geigerThread.interrupt();
            geigerThread = null;
        }
        currentPercent = 0;
    }

    private int getSignalColor(int percent) {
        if (percent < 30)
            return android.graphics.Color.parseColor("#2196F3"); // Blue
        if (percent < 70)
            return android.graphics.Color.parseColor("#00BCD4"); // Cyan
        return android.graphics.Color.parseColor("#4CAF50"); // Green
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() > 0) return; // Prevent repetition behavior

        if (reader() != null) {
            int action = translateKeyCode(keyCode, reader().getClass());
            if (action == 2) {
                if (!isLocating) {
                    targetEpc = etTargetEpc.getText().toString().toUpperCase().trim();
                    if (targetEpc.isEmpty()) {
                        return;
                    }
                    tvStatus.setText("Locating " + targetEpc + "...");
                    reader().isMute = true; // Still mute reader to avoid overlapping sounds
                    reader().startInventory();
                    isLocating = true;
                    currentPercent = 0;
                    startGeigerLoop();
                }
            }
        }
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
        if (reader() != null) {
            int action = translateKeyCode(keyCode, reader().getClass());
            if (action == 2) {
                stopLocating();
            }
        }
    }

    private void stopLocating() {
        isLocating = false;
        if (reader() != null) {
            reader().stopInventory();
            final beetech.app.core.AdvBaseReader r = reader();
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isLocating && r != null) {
                    r.isMute = false;
                    android.util.Log.d("LocateFragment", "Reader unmuted after stop delay");
                }
            }, 500);
        }
        stopGeigerLoop();
        resetUI();
    }

    @Override
    public void onTagRead(TagResult tagResult) {
        if (!isLocating || tagResult == null || tagResult.epc == null) return;

        String epcField = tagResult.epc != null ? tagResult.epc.toUpperCase() : "";
        String rssiField = tagResult.rssi != null ? tagResult.rssi.toUpperCase() : "";
        String tidField = tagResult.tid != null ? tagResult.tid.toUpperCase() : "";

        // Diagnostic logging of raw values
        android.util.Log.v("LocateFragment", "onTagRead: raw_epc=[" + epcField + "] raw_rssi=[" + rssiField + "] target=[" + targetEpc + "]");

        // Match target in ANY field (SDKs often mismatch or swap fields)
        if (epcField.contains(targetEpc) || rssiField.contains(targetEpc) || tidField.contains(targetEpc)) {
            try {
                int bestRssi = -100;
                boolean foundValid = false;

                // Identify the true RSSI field by checking if it strictly formats as a number
                String[] fields = {rssiField, epcField, tidField};
                for (String field : fields) {
                    String clean = field.replace("DBM", "").trim();
                    if (clean.matches("-?\\d+(\\.\\d+)?")) {
                        double val = Double.parseDouble(clean);
                        // Valid RSSI ranges typically don't exceed -110 to 110.
                        if (val > -110 && val < 110) {
                            bestRssi = (int) val;
                            foundValid = true;
                            break;
                        }
                    }
                }

                if (!foundValid) return;

                currentRssi = bestRssi;
                int percent = normalizeRSSI(currentRssi);
                currentPercent = percent; // Update the volatile for the geiger thread
                int color = getSignalColor(percent);

                runOnUiThreadSafe(() -> {
                    if (percent != progressSignal.getProgress()) {
                        tvStatus.setText("Found " + targetEpc);
                        tvSignalPercent.setText(percent + "%");
                        tvSignalPercent.setTextColor(color);
                        progressSignal.setProgress(percent);
                        progressSignal.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("LocateFragment", "Error processing RSSI: " + e.getMessage());
            }
        }
    }

    private int normalizeRSSI(int rssi) {
        // Handle positive RSSI (0-100 scale common in some Urovo/Chainway firmwares)
        if (rssi > 0) {
            if (rssi > 100) return 100;
            return rssi;
        }
        
        // Handle negative RSSI (dBm scale)
        if (rssi <= MIN_RSSI) return 0;
        if (rssi >= MAX_RSSI) return 100;
        return (int) ((rssi - MIN_RSSI) * 100 / (MAX_RSSI - MIN_RSSI));
    }



    private void resetUI() {
        runOnUiThreadSafe(() -> {
            tvSignalPercent.setText("0%");
            tvSignalPercent.setTextColor(android.graphics.Color.DKGRAY);
            progressSignal.setProgress(0);
            progressSignal
                    .setProgressTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.LTGRAY));
            tvStatus.setText("Pull trigger to start locating");
        });
    }

    private void runOnUiThreadSafe(Runnable r) {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(r);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isLocating = false;
        stopGeigerLoop();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
