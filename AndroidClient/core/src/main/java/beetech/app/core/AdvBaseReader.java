package beetech.app.core;

import android.media.AudioManager;
import android.media.SoundPool;


import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import beetech.app.core.data.DeviceItem;
import beetech.app.core.dto.ReaderProfile;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.app.core.models.ReaderSettings;
import beetech.app.core.network.WebSocketConnector;
import beetech.app.rfidreader.R;


public abstract class AdvBaseReader implements IOperationListener {
    private  SoundPool soundPool;
    private int iSoundScanOK;
    private int iSoundScanError;
    private int iSoundScanRFID;
    protected Boolean reaadOne = false;
    public BaseActivity activity;

    public boolean inited = false;
    public boolean isMute = false;

    public void initSoundPool() {
        if (activity == null) {
            android.util.Log.e(TAG, "initSoundPool failed: activity is null");
            return;
        }
        if (soundPool != null) {
            soundPool.release();
        }
        this.soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        this.iSoundScanOK = this.soundPool.load(activity, R.raw.scan_ok, 1);
        this.iSoundScanError = this.soundPool.load(activity, R.raw.error, 1);
        this.iSoundScanRFID = this.soundPool.load(activity, R.raw.scan_rfid, 1);
        android.util.Log.d(TAG, "initSoundPool COMPLETE: OK=" + iSoundScanOK + ", Error=" + iSoundScanError + ", RFID=" + iSoundScanRFID);
    }
//
//    public BaseActivity activity;
    public WebSocketConnector connector = null;
    public static final String TAG =  AdvBaseReader.class.getSimpleName();
    public void setContinousMode(boolean enable) {

    }
    public abstract void  initReader(int type, String address);
    public abstract void  initReader(DeviceItem di);
    public abstract Boolean connect(String address);
    public abstract void disconnect();
    public  abstract  String getDeviceInfo();
    public abstract void start();
    public abstract void stop();
    public abstract void applySettings(ReaderProfile readerProfile);
    public abstract void applySettings(ReaderSettings readerProfile);
    public abstract void lockOrUnlockTag(String epc, String accesspwd, Boolean isLock);
    public abstract void killTag(String epc, String accesspwd);

    public  abstract String readUserMemory(String epc, String password);
    public abstract TagOperation writeTagEpc(String oldepc, String newepc);

    public abstract TagOperation writeTagEpc(String oldepc, short currentPcBits, String newepc, String tid , String accesspwd , String oldpwd);
    public  abstract  void startInventory();
    public abstract  void stopInventory();
    public abstract  void scanBarCode();
     public  abstract void takePhoto();

    public void initReader(int type) {

    }
    private void playSound(int sid) {
        if (soundPool == null) {
            android.util.Log.w(TAG, "playSound: soundPool is null, attempting auto-init. sid=" + sid);
            initSoundPool();
        }
        if (soundPool == null) {
            android.util.Log.e(TAG, "playSound failed: soundPool is still null after auto-init attempt. sid=" + sid);
            return;
        }
        if (sid <= 0) {
            // If the sound engine was just inited, sid might be 0 because it hasn't loaded yet.
            // But we should try to play the correct sound if we can identify it.
            android.util.Log.w(TAG, "playSound skip: sid is " + sid + " (invalid or not yet loaded)");
            return;
        }
        try {
            android.util.Log.v(TAG, "soundPool.play sid=" + sid + " vol=1.0");
            soundPool.play(sid, 1, 1, 1, 0, 1);
        } catch (Exception e) {
            android.util.Log.e(TAG, "playSound error: " + e.getMessage());
        }
    }

    public String calculatePCBits(int epcByteLength) {
        int epcWords = epcByteLength / 2; // Convert bytes to words (each word = 2 bytes)
        int pcBits = 0x3000 | (epcWords & 0x1F); // Masking ensures only EPC length is applied

        return String.format("%04X", pcBits); // Convert to hex string
    }


    //    public String calculatePCBits(int epcByteLength) {
//        int epcWords = epcByteLength / 2; // Convert bytes to words (each word = 2 bytes)
//        int pcBits = (0x3000 | epcWords); // 0x3000 includes RFU and encoding bits
//
//        return String.format("%04X", pcBits); // Convert to hex string
//    }
    @Override
    public void onTagRead(TagResult tagResult) {
        for (IOperationListener listener:listeners) {
            listener.onTagRead(tagResult);
        }
        playTagReadSound();
    }
    @Override
    public void onScanBarCode(String barcode) {
        //connector.onScanBarCode(barcode);
        for (IOperationListener listener:listeners) {
            listener.onScanBarCode(barcode);
        }
    }

    @Override
    public void ontakePicture(byte[] data) {
        connector.ontakePicture(data);
        for (IOperationListener listener:listeners) {
            listener.ontakePicture(data);
        }
    }

    @Override
    public void onOperationComplete(TagOperation op) {
        for (IOperationListener listener:listeners) {
            listener.onOperationComplete(op);
        }
    }
//    @Override
//    public void onInventoryTag(String tid, String epc, String rssi) {
//        connector.onInventoryTag(epc, tid, rssi);
//    }
//
//    @Override
//    public void onInventoryTagEnd() {
//
//    }

    private CopyOnWriteArrayList<IOperationListener>listeners = new CopyOnWriteArrayList<>();
//    public void registerListener(IRfidUhfEventListener listener) {
//
//    }
    public void registerListener(IOperationListener listener) {
        listeners.add(listener);
    }
    public void unregisterListener(IOperationListener listener) {
        listeners.remove(listener);
    }

    public void playErrorSound() {
        playSound(this.iSoundScanError);
    }

    public void playTagReadSound() {
        playTagReadSound(false);
    }
    public void playTagReadSound(boolean force) {
        if (force) {
            android.util.Log.v(TAG, "playTagReadSound: FORCED play");
            playSound(this.iSoundScanRFID);
            return;
        }
        if(isMute) {
            android.util.Log.v(TAG, "playTagReadSound: skip (isMute=true)");
            return;
        }
        android.util.Log.v(TAG, "playTagReadSound: normal play");
        playSound(this.iSoundScanRFID);
    }
    public void initBarCodeScan() {
    }

    public void readOne() {
        reaadOne = true;
        startInventory();
    }
    public void stopReadOne() {
        reaadOne = false;
        stopInventory();
    }
    public void setPower(int power){

    }
}

