package beetech.app.core;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import beetech.app.core.dto.Command;
import beetech.app.core.dto.ReaderSettings;


public class BaseActivity extends AppCompatActivity {
    public Command currentCommand;

    public void showToast(String msg) {
        showToast(msg, false);
    }
    public void showToast(String msg, boolean lengthLong) {
        Toast.makeText(this.getApplicationContext(), msg, lengthLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public ReaderSettings getReaderSettings() {
        //todo: get settings from server
        return  null;
    }
}
