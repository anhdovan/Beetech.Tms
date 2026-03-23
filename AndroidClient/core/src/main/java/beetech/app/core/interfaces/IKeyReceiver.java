package beetech.app.core.interfaces;

import android.view.KeyEvent;

public interface IKeyReceiver {
    public void onActivityKeyDown(int keyCode, KeyEvent event);
    void onActivityKeyUp(int keyCode, KeyEvent event);
}
