package beetech.tms.android.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.IOperationListener;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.app.core.interfaces.IBarcodeReceiver;
import beetech.app.core.interfaces.IKeyReceiver;
import beetech.tms.android.TmsApp;
import beetech.tms.android.MainActivity;
import beetech.app.readers.ChainwayReader;
import beetech.app.readers.UnitechReaderV0;
import beetech.app.readers.UrovoDT50P;

public class BaseFragment extends Fragment
        implements
        IOperationListener,
        IBarcodeReceiver,
        IKeyReceiver {

    public MainActivity activity() {
        return TmsApp.getInstance().getMainActivity();
    }

    public AdvBaseReader reader() {
        return activity() != null ? activity().getReader() : null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (reader() != null) {
            reader().registerListener(this);
        }
        if (activity() != null) {
            activity().registerKeyReceiver(this);
            activity().registerBarcodeReceiver(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (reader() != null) {
            reader().unregisterListener(this);
        }
        if (activity() != null) {
            activity().unregisterKeyReceiver(this);
            activity().unregisterBarcodeReceiver(this);
        }
    }

    @Override
    public void onBarcodeScanned(String barcode) {
    }

    @Override
    public void onActivityKeyDown(int keyCode, KeyEvent event) {
    }

    @Override
    public void onActivityKeyUp(int keyCode, KeyEvent event) {
    }

    public static int translateKeyCode(int keycode, Class readerclass) {
        android.util.Log.d("BaseFragment", "translateKeyCode: keycode=" + keycode + " reader=" + (readerclass != null ? readerclass.getSimpleName() : "null"));
        if (readerclass == UrovoDT50P.class) {
            switch (keycode) {
                case 523:
                    return 2;
                case 521:
                    return 1;
                case 520:
                    return 3;
            }
        } else if (readerclass == UnitechReaderV0.class) {
            switch (keycode) {
                case 298:
                    return 2;
                case 291:
                    return 1;
                case 292:
                    return 3;
            }
        } else if (readerclass == ChainwayReader.class) {
            if (keycode == 293)
                return 2;
            if (keycode == 139)
                return 1;
            if (keycode == 142)
                return 3;
        }

        // Generic Fallback for common trigger keys
        if (keycode == 520 || keycode == 521 || keycode == 523 || keycode == 139 || keycode == 280 || keycode == 293) {
            android.util.Log.i("BaseFragment", "translateKeyCode: Generic trigger detected for keycode=" + keycode);
            return 2; // Default to RFID Trigger
        }

        return 0;
    }

    @Override
    public void onOperationComplete(TagOperation op) {
    }

    @Override
    public void onTagRead(TagResult tagResult) {
    }

    @Override
    public void onScanBarCode(String barcode) {
    }

    @Override
    public void ontakePicture(byte[] data) {
    }

    @Override
    public void onTagAccess(String action, int code, String epc, String data) {
    }
}
