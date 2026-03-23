package beetech.app.readers;

import static beetech.app.core.AdvBaseReader.TAG;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.device.scanner.configuration.Triggering;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ubx.usdk.RFIDSDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.unitech.lib.uhf.params.TagExtParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.IOperationListener;
import beetech.app.core.data.DeviceItem;
import beetech.app.core.dto.ReaderProfile;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.app.core.models.ReaderSettings;

public class UrovoDT50P extends AdvBaseReader implements IRfidCallback {
    private static final boolean DEBUG = true;

    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE; // default action
    private static final String ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;
    private static final String BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG;
    private static final String BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG;
    private static final String DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG;

    private static final String DECODE_ENABLE = "decode_enable";
    private static final String DECODE_TRIGGER_MODE = "decode_trigger_mode";
    private static final String DECODE_TRIGGER_MODE_HOST = "HOST";
    private static final String DECODE_TRIGGER_MODE_CONTINUOUS = "CONTINUOUS";
    private static final String DECODE_TRIGGER_MODE_PAUSE = "PAUSE";
    private static String DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST;

    private static final int DECODE_OUTPUT_MODE_INTENT = 0;
    private static final int DECODE_OUTPUT_MODE_FOCUS = 1;
    private static int DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS;
    private static final String DECODE_OUTPUT_MODE = "decode_output_mode";
    private static final String DECODE_CAPTURE_IMAGE_KEY = "bitmapBytes";
    private static final String DECODE_CAPTURE_IMAGE_SHOW = "scan_capture_image";

    private static Map<String, BarcodeHolder> mBarcodeMap = new HashMap<String, BarcodeHolder>();
    private RfidManager mRfidManager;
    private ScanManager mscanManager;

    private ScanCallback callback;
    private ScanManager mScanManager;
    private boolean mScanEnable = true;
    private boolean sccanServiceInited;
    private boolean isInitRfid = false;
    // private boolean mscanneregistered;

    private void getRFIDManager() {
        if (mRfidManager == null) {
            initRfid();
            // setCallback();
        }
    }

    public UrovoDT50P() {
        Log.i(TAG, "UrovoDT50P instance created: " + this.hashCode());
        initRfid();
    }

    private void setCallback() {
        if (mRfidManager != null) {
            mRfidManager.registerCallback(this);
        }
    }

    @Override
    public void initReader(int type, String address) {
        initReader(type);
    }

    @Override
    public void initReader(DeviceItem di) {

    }

    public void initRfid() {
        if (isInitRfid) return;
        isInitRfid = true;
        Log.i(TAG, "initRfid() called");
        RFIDSDKManager.getInstance().power(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3500); // Increased from 1500 to 3500

                Log.i(TAG, "initRfid() connecting...");
                boolean status = RFIDSDKManager.getInstance().connect();

                Log.i(TAG, "initRfid() connect status : " + status);
                if (status) {
                    mRfidManager = RFIDSDKManager.getInstance().getRfidManager();
                    Log.i(TAG, "initRfid() RfidManager obtained: " + (mRfidManager != null));
                } else {
                    Log.e(TAG, "initRfid() connection failed");
                    isInitRfid = false; // Allow retry
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (status) {
                            activity.showToast("RFID Reader Connected Successfully");
                        } else {
                            activity.showToast("RFID Reader Connection FAILED");
                        }
                    }
                });

            }
        }).start();

    }

    @Override
    public Boolean connect(String address) {
        RFIDSDKManager.getInstance().power(true); // Ensure power is on
        return mRfidManager != null && mRfidManager.connect();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public String getDeviceInfo() {
        getRFIDManager();
        return "";
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setPower(int power) {
        if (power < 0 || power > 33)
            return;
        mRfidManager.setOutputPower(power);
    }

    @Override
    public void applySettings(ReaderProfile readerProfile) {

    }

    @Override
    public void applySettings(ReaderSettings readerProfile) {

    }

    @Override
    public void lockOrUnlockTag(String epc, String accesspwd, Boolean isLock) {
        if (isLock) {
            mRfidManager.lockTag(epc, accesspwd, 1, 1);
        } else {
            mRfidManager.lockTag(epc, null, 1, 1);
        }
    }

    @Override
    public void killTag(String epc, String accesspwd) {
        mRfidManager.killTag(epc, accesspwd);
    }

    @Override
    public String readUserMemory(String epc, String password) {
        String data = mRfidManager.readTag(epc, 3, 0, 64, password);
        return data;
    }

    @Override
    public TagOperation writeTagEpc(String oldepc, String newepc) {
        return writeTagEpc(oldepc, (short) 0, newepc, null, "00000000", null);
    }

    @Override
    public TagOperation writeTagEpc(String oldepc, short currentPcBits, String newepc, String tid, String accesspwd,
            String newaccesspwd) {
        TagOperation o = new TagOperation();
        o.success = true;
        o.epc = oldepc;
        o.newepc = newepc;
        o.tid = tid;
        o.opId = oldepc.hashCode();
        try {
            o.resultCode = mRfidManager.writeTagEpc(oldepc, accesspwd, newepc);
            if (o.resultCode == 0) { // writesuccessfully -> lock tag
                if (newaccesspwd != null && newaccesspwd != "") {
                    lockOrUnlockTag(newepc, accesspwd, false);
                    lockOrUnlockTag(newepc, newaccesspwd, true);
                }
            } else {
                o.success = false;
                o.message = "Error-code:" + o.resultCode;
            }

        } catch (Exception e) {
            o.success = false;
            o.message = e.getMessage();
        }

        return o;
    }

    @Override
    public void startInventory() {
        Log.i(TAG, "startInventory() called");
        getRFIDManager();
        if (mRfidManager != null) {
            mRfidManager.registerCallback(this);
            mRfidManager.startRead();
            Log.i(TAG, "startInventory() startRead() executed");
        } else {
            Log.e(TAG, "startInventory() mRfidManager is NULL");
            activity.showToast("Lỗi: Đầu đọc chưa sẵn sàng (mRfidManager null)");
        }
    }

    @Override
    public void stopInventory() {
        Log.i(TAG, "stopInventory() called");
        if (mRfidManager != null) {
            mRfidManager.stopInventory();
        }
    }

    @Override
    public void scanBarCode() {
        if (!sccanServiceInited) {
            initScan();
            openScanner();
        }
        startDecode();
    }

    @Override
    public void takePhoto() {

    }

    @Override
    public void onOperationComplete(TagOperation op) {

    }

    @Override
    public void onTagAccess(String action, int code, String epc, String data) {
        connector.onTagAccess(action, code, epc, data);
    }

    // @Override
    // public void onScanBarCode(String barcode) {
    // System.out.println("barcode scanned: " + barcode);
    // }

    // @Override
    // public void ontakePicture(byte[] data) {
    // System.out.println(data.length + "--");
    // }
    ///////////////////////////////////// BARCODE
    // SCANNER////////////////////////////////////////////////////////
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static final int[] SCAN_KEYCODE = { 520, 521, 522, 523 };
    private static final int MSG_SHOW_SCAN_RESULT = 1;
    private static final int MSG_SHOW_SCAN_IMAGE = 2;
    private boolean mScanCaptureImageShow = true;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogI("onReceive , action:" + action);
            // Get Scan Image . Make sure to make a request before getting a scanned image
            if (ACTION_CAPTURE_IMAGE.equals(action)) {
                byte[] imagedata = intent.getByteArrayExtra(DECODE_CAPTURE_IMAGE_KEY);
                if (imagedata != null && imagedata.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                    Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_IMAGE);
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                } else {
                    LogI("onReceive , ignore imagedata:" + imagedata);
                }
            } else {
                // Get scan results, including string and byte data etc.
                byte[] barcode = intent.getByteArrayExtra(DECODE_DATA_TAG);
                int barcodeLen = intent.getIntExtra(BARCODE_LENGTH_TAG, 0);
                byte temp = intent.getByteExtra(BARCODE_TYPE_TAG, (byte) 0);
                String barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG);
                if (mScanCaptureImageShow) {
                    // Request images of this scan
                    context.sendBroadcast(new Intent(ACTION_DECODE_IMAGE_REQUEST));
                }
                LogI("barcode type:" + temp);
                String scanResult = new String(barcode, 0, barcodeLen);
                // print scan results.
                scanResult = " length：" + barcodeLen + "\nbarcode：" + scanResult + "\nbytesToHexString："
                        + bytesToHexString(barcode) + "\nbarcodeStr:" + barcodeStr;
                Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_RESULT);
                msg.obj = scanResult;
                mHandler.sendMessage(msg);
                onScanBarCode(barcodeStr);
            }
        }
    };

    /**
     * @param register , ture register , false unregister
     */
    private void registerReceiver(boolean register) {
        if (register && mScanManager != null) {
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[] { PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG };
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(ACTION_DECODE);
            }
            filter.addAction(ACTION_CAPTURE_IMAGE);
            try {
                activity.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                LogD(e.getMessage());
            } finally {
                activity.registerReceiver(mReceiver, filter);
            }
        } else if (mScanManager != null) {
            mScanManager.stopDecode();
            activity.unregisterReceiver(mReceiver);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_SCAN_RESULT:
                    String scanResult = (String) msg.obj;
                    // printScanResult(scanResult);
                    break;
                case MSG_SHOW_SCAN_IMAGE:
                    // if (mScanImage != null && mScanCaptureImageShow) {
                    // Bitmap bitmap = (Bitmap) msg.obj;
                    // mScanImage.setImageBitmap(bitmap);
                    // mScanImage.setVisibility(View.VISIBLE);
                    // } else {
                    // mScanCaptureImageShow = false;
                    // mScanImage.setVisibility(View.INVISIBLE);
                    // LogI("handleMessage , MSG_SHOW_SCAN_IMAGE scan image:" + mScanImage);
                    // }
                    break;
            }
        }
    };

    private void initBarcodeParameters() {
        mBarcodeMap.clear();
        BarcodeHolder holder = new BarcodeHolder();
        // Symbology.AZTEC
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.AZTEC_ENABLE };
        holder.mParaKeys = new String[] { "AZTEC_ENABLE" };
        mBarcodeMap.put(Symbology.AZTEC + "", holder);
        // Symbology.CHINESE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.C25_ENABLE };
        holder.mParaKeys = new String[] { "C25_ENABLE" };
        mBarcodeMap.put(Symbology.CHINESE25 + "", holder);
        // Symbology.CODABAR
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeNOTIS = new CheckBoxPreference(activity);
        holder.mBarcodeCLSI = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODABAR_ENABLE, PropertyID.CODABAR_LENGTH1, PropertyID.CODABAR_LENGTH2,
                PropertyID.CODABAR_NOTIS, PropertyID.CODABAR_CLSI };
        holder.mParaKeys = new String[] { "CODABAR_ENABLE", "CODABAR_LENGTH1", "CODABAR_LENGTH2", "CODABAR_NOTIS",
                "CODABAR_CLSI" };
        mBarcodeMap.put(Symbology.CODABAR + "", holder);
        // Symbology.CODE11
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeCheckDigit = new ListPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE11_ENABLE, PropertyID.CODE11_LENGTH1, PropertyID.CODE11_LENGTH2,
                PropertyID.CODE11_SEND_CHECK };
        holder.mParaKeys = new String[] { "CODE11_ENABLE", "CODE11_LENGTH1", "CODE11_LENGTH2", "CODE11_SEND_CHECK" };
        mBarcodeMap.put(Symbology.CODE11 + "", holder);
        // Symbology.CODE32
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE32_ENABLE };
        holder.mParaKeys = new String[] { "CODE32_ENABLE" };
        mBarcodeMap.put(Symbology.CODE32 + "", holder);
        // Symbology.CODE39
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeChecksum = new CheckBoxPreference(activity);
        holder.mBarcodeSendCheck = new CheckBoxPreference(activity);
        holder.mBarcodeFullASCII = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE39_ENABLE, PropertyID.CODE39_LENGTH1, PropertyID.CODE39_LENGTH2,
                PropertyID.CODE39_ENABLE_CHECK, PropertyID.CODE39_SEND_CHECK, PropertyID.CODE39_FULL_ASCII };
        holder.mParaKeys = new String[] { "CODE39_ENABLE", "CODE39_LENGTH1", "CODE39_LENGTH2", "CODE39_ENABLE_CHECK",
                "CODE39_SEND_CHECK", "CODE39_FULL_ASCII" };
        mBarcodeMap.put(Symbology.CODE39 + "", holder);
        // Symbology.CODE93
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE93_ENABLE, PropertyID.CODE93_LENGTH1, PropertyID.CODE93_LENGTH2 };
        holder.mParaKeys = new String[] { "CODE93_ENABLE", "CODE93_LENGTH1", "CODE93_LENGTH2" };
        mBarcodeMap.put(Symbology.CODE93 + "", holder);
        // Symbology.CODE128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeISBT = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE128_ENABLE, PropertyID.CODE128_LENGTH1, PropertyID.CODE128_LENGTH2,
                PropertyID.CODE128_CHECK_ISBT_TABLE };
        holder.mParaKeys = new String[] { "CODE128_ENABLE", "CODE128_LENGTH1", "CODE128_LENGTH2",
                "CODE128_CHECK_ISBT_TABLE" };
        mBarcodeMap.put(Symbology.CODE128 + "", holder);
        // Symbology.COMPOSITE_CC_AB
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.COMPOSITE_CC_AB_ENABLE };
        holder.mParaKeys = new String[] { "COMPOSITE_CC_AB_ENABLE" };
        mBarcodeMap.put(Symbology.COMPOSITE_CC_AB + "", holder);
        // Symbology.COMPOSITE_CC_C
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.COMPOSITE_CC_C_ENABLE };
        holder.mParaKeys = new String[] { "COMPOSITE_CC_C_ENABLE" };
        mBarcodeMap.put(Symbology.COMPOSITE_CC_C + "", holder);
        // Symbology.DATAMATRIX
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.DATAMATRIX_ENABLE };
        holder.mParaKeys = new String[] { "DATAMATRIX_ENABLE" };
        mBarcodeMap.put(Symbology.DATAMATRIX + "", holder);
        // Symbology.DISCRETE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.D25_ENABLE };
        holder.mParaKeys = new String[] { "D25_ENABLE" };
        mBarcodeMap.put(Symbology.DISCRETE25 + "", holder);
        // Symbology.EAN8
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.EAN8_ENABLE };
        holder.mParaKeys = new String[] { "EAN8_ENABLE" };
        mBarcodeMap.put(Symbology.EAN8 + "", holder);
        // Symbology.EAN13
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeBookland = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.EAN13_ENABLE, PropertyID.EAN13_BOOKLANDEAN };
        holder.mParaKeys = new String[] { "EAN13_ENABLE", "EAN13_BOOKLANDEAN" };
        mBarcodeMap.put(Symbology.EAN13 + "", holder);
        // Symbology.GS1_14
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.GS1_14_ENABLE };
        holder.mParaKeys = new String[] { "GS1_14_ENABLE" };
        mBarcodeMap.put(Symbology.GS1_14 + "", holder);
        // Symbology.GS1_128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.CODE128_GS1_ENABLE };
        holder.mParaKeys = new String[] { "CODE128_GS1_ENABLE" };
        mBarcodeMap.put(Symbology.GS1_128 + "", holder);
        // Symbology.GS1_EXP
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mParaIds = new int[] { PropertyID.GS1_EXP_ENABLE, PropertyID.GS1_EXP_LENGTH1,
                PropertyID.GS1_EXP_LENGTH2 };
        holder.mParaKeys = new String[] { "GS1_EXP_ENABLE", "GS1_EXP_LENGTH1", "GS1_EXP_LENGTH2" };
        mBarcodeMap.put(Symbology.GS1_EXP + "", holder);
        // Symbology.GS1_LIMIT
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.GS1_LIMIT_ENABLE };
        holder.mParaKeys = new String[] { "GS1_LIMIT_ENABLE" };
        mBarcodeMap.put(Symbology.GS1_LIMIT + "", holder);
        // Symbology.INTERLEAVED25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeChecksum = new CheckBoxPreference(activity);
        holder.mBarcodeSendCheck = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.I25_ENABLE, PropertyID.I25_LENGTH1, PropertyID.I25_LENGTH2,
                PropertyID.I25_ENABLE_CHECK, PropertyID.I25_SEND_CHECK };
        holder.mParaKeys = new String[] { "I25_ENABLE", "I25_LENGTH1", "I25_LENGTH2", "I25_ENABLE_CHECK",
                "I25_SEND_CHECK" };
        mBarcodeMap.put(Symbology.INTERLEAVED25 + "", holder);
        // Symbology.MATRIX25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.M25_ENABLE };
        holder.mParaKeys = new String[] { "M25_ENABLE" };
        mBarcodeMap.put(Symbology.MATRIX25 + "", holder);
        // Symbology.MAXICODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.MAXICODE_ENABLE };
        holder.mParaKeys = new String[] { "MAXICODE_ENABLE" };
        mBarcodeMap.put(Symbology.MAXICODE + "", holder);
        // Symbology.MICROPDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.MICROPDF417_ENABLE };
        holder.mParaKeys = new String[] { "MICROPDF417_ENABLE" };
        mBarcodeMap.put(Symbology.MICROPDF417 + "", holder);
        // Symbology.MSI
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeLength1 = new EditTextPreference(activity);
        holder.mBarcodeLength2 = new EditTextPreference(activity);
        holder.mBarcodeSecondChecksum = new CheckBoxPreference(activity);
        holder.mBarcodeSendCheck = new CheckBoxPreference(activity);
        holder.mBarcodeSecondChecksumMode = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.MSI_ENABLE, PropertyID.MSI_LENGTH1, PropertyID.MSI_LENGTH2,
                PropertyID.MSI_REQUIRE_2_CHECK, PropertyID.MSI_SEND_CHECK, PropertyID.MSI_CHECK_2_MOD_11 };
        holder.mParaKeys = new String[] { "MSI_ENABLE", "MSI_LENGTH1", "MSI_LENGTH2", "MSI_REQUIRE_2_CHECK",
                "MSI_SEND_CHECK", "MSI_CHECK_2_MOD_11" };
        mBarcodeMap.put(Symbology.MSI + "", holder);
        // Symbology.PDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.PDF417_ENABLE };
        holder.mParaKeys = new String[] { "PDF417_ENABLE" };
        mBarcodeMap.put(Symbology.PDF417 + "", holder);
        // Symbology.QRCODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.QRCODE_ENABLE };
        holder.mParaKeys = new String[] { "QRCODE_ENABLE" };
        mBarcodeMap.put(Symbology.QRCODE + "", holder);
        // Symbology.TRIOPTIC
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.TRIOPTIC_ENABLE };
        holder.mParaKeys = new String[] { "TRIOPTIC_ENABLE" };
        mBarcodeMap.put(Symbology.TRIOPTIC + "", holder);
        // Symbology.UPCA
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeChecksum = new CheckBoxPreference(activity);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(activity);
        holder.mBarcodeConvertEAN13 = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.UPCA_ENABLE, PropertyID.UPCA_SEND_CHECK, PropertyID.UPCA_SEND_SYS,
                PropertyID.UPCA_TO_EAN13 };
        holder.mParaKeys = new String[] { "UPCA_ENABLE", "UPCA_SEND_CHECK", "UPCA_SEND_SYS", "UPCA_TO_EAN13" };
        mBarcodeMap.put(Symbology.UPCA + "", holder);
        // Symbology.UPCE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mBarcodeChecksum = new CheckBoxPreference(activity);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(activity);
        holder.mBarcodeConvertUPCA = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.UPCE_ENABLE, PropertyID.UPCE_SEND_CHECK, PropertyID.UPCE_SEND_SYS,
                PropertyID.UPCE_TO_UPCA };
        holder.mParaKeys = new String[] { "UPCE_ENABLE", "UPCE_SEND_CHECK", "UPCE_SEND_SYS", "UPCE_TO_UPCA" };
        mBarcodeMap.put(Symbology.UPCE + "", holder);
        // Symbology.UPCE1
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(activity);
        holder.mParaIds = new int[] { PropertyID.UPCE1_ENABLE };
        holder.mParaKeys = new String[] { "UPCE1_ENABLE" };
        mBarcodeMap.put(Symbology.UPCE1 + "", holder);
    }

    private void initScan() {
        mScanManager = new ScanManager();
        boolean powerOn = mScanManager.getScannerState();
        if (!powerOn) {
            powerOn = mScanManager.openScanner();
            if (!powerOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Scanner cannot be turned on!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
        initBarcodeParameters();
        sccanServiceInited = true;
    }

    /**
     * ScanManager.getTriggerMode
     *
     * @return
     */
    private Triggering getTriggerMode() {
        Triggering mode = mScanManager.getTriggerMode();
        return mode;
    }

    private String getDecodeStringShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String value = sharedPrefs.getString(key, "");
        return value;
    }

    /**
     * Attribute helper
     *
     * @param key
     * @param value
     */
    private void updateStringShared(String key, String value) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateStringShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (value == getDecodeStringShared(key) || "".equals(value.trim())) {
            LogI("updateStringShared ,ignore key:" + key + " update.");
            return;
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    private boolean getDecodeScanShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean enable = sharedPrefs.getBoolean(key, true);
        return enable;
    }

    /**
     * Attribute helper
     *
     * @param key
     * @param enable
     */
    private void updateScanShared(String key, boolean enable) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateScanShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (enable == getDecodeScanShared(key)) {
            LogI("updateScanShared ,ignore key:" + key + " update.");
            return;
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(key, enable);
        editor.apply();
        editor.commit();
    }

    /**
     * ScanManager.setTriggerMode
     *
     * @param mode value : Triggering.HOST, Triggering.CONTINUOUS, or
     *             Triggering.PULSE.
     */
    private void setTrigger(Triggering mode) {
        Triggering currentMode = getTriggerMode();
        LogD("setTrigger , mode;" + mode + ",currentMode:" + currentMode);
        if (mode != currentMode) {
            mScanManager.setTriggerMode(mode);
            if (mode == Triggering.HOST) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_HOST);
            } else if (mode == Triggering.CONTINUOUS) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_CONTINUOUS;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_CONTINUOUS);
            } else if (mode == Triggering.PULSE) {
                DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_PAUSE;
                updateStringShared(DECODE_TRIGGER_MODE, DECODE_TRIGGER_MODE_PAUSE);
            }
        } else {
            LogI("setTrigger , ignore update Trigger mode:" + mode);
        }
    }

    private void LogD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    private void LogI(String msg) {
        Log.i(TAG, msg);
    }

    /**
     * ScanManager.getOutputMode
     *
     * @return
     */
    private int getScanOutputMode() {
        int mode = mScanManager.getOutputMode();
        return mode;
    }

    /**
     * ScanManager.switchOutputMode
     *
     * @param mode
     */
    private void setScanOutputMode(int mode) {
        int currentMode = getScanOutputMode();
        if (mode != currentMode && (mode == DECODE_OUTPUT_MODE_FOCUS ||
                mode == DECODE_OUTPUT_MODE_INTENT)) {
            mScanManager.switchOutputMode(mode);
            if (mode == DECODE_OUTPUT_MODE_FOCUS) {
                DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS;
                updateIntShared(DECODE_OUTPUT_MODE, DECODE_OUTPUT_MODE_FOCUS);
            } else if (mode == DECODE_OUTPUT_MODE_INTENT) {
                DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_INTENT;
                updateIntShared(DECODE_OUTPUT_MODE, DECODE_OUTPUT_MODE_INTENT);
            }
        } else {
            LogI("setScanOutputMode , ignore update Output mode:" + mode);
        }
    }

    private int getDecodeIntShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        int value = sharedPrefs.getInt(key, 1);
        return value;
    }

    private void updateIntShared(String key, int value) {
        if (key == null || "".equals(key.trim())) {
            LogI("updateIntShared , key:" + key);
            return;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (value == getDecodeIntShared(key)) {
            LogI("updateIntShared ,ignore key:" + key + " update.");
            return;
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
        editor.commit();
    }

    private void resetScanner() {
        showResetDialog();
    }

    private void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Are you sure you want to reset?");
        builder.setTitle("Scanner reset");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ResetAsyncTask task = new ResetAsyncTask(activity);
                // task.execute("reset");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * ScanManager.getTriggerLockState
     *
     * @return
     */
    private boolean getlockTriggerState() {
        boolean state = mScanManager.getTriggerLockState();
        return state;
    }

    /**
     * ScanManager.lockTrigger and ScanManager.unlockTrigger
     *
     * @param state value ture or false
     */
    private void updateLockTriggerState(boolean state) {
        boolean currentState = getlockTriggerState();
        if (state != currentState) {
            if (state) {
                mScanManager.lockTrigger();
            } else {
                mScanManager.unlockTrigger();
            }
        } else {
            LogI("updateLockTriggerState , ignore update lockTrigger state:" + state);
        }
    }

    /**
     * ScanManager.startDecode
     */
    private void startDecode() {
        if (!mScanEnable) {
            LogI("startDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        boolean lockState = getlockTriggerState();
        if (lockState) {
            LogI("startDecode ignore, Scan lockTrigger state:" + lockState);
            return;
        }
        if (mScanManager != null) {
            mScanManager.startDecode();
        }
    }

    /**
     * ScanManager.stopDecode
     */
    private void stopDecode() {
        if (!mScanEnable) {
            LogI("stopDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
    }

    /**
     * ScanManager.closeScanner
     *
     * @return
     */
    private boolean closeScanner() {
        boolean state = false;
        if (mScanManager != null) {
            mScanManager.stopDecode();
            state = mScanManager.closeScanner();
        }
        return state;
    }

    /**
     * Obtain an instance of BarCodeReader with ScanManager
     * ScanManager.getScannerState
     * ScanManager.openScanner
     * ScanManager.enableAllSymbologies
     *
     * @return
     */
    private boolean openScanner() {
        mScanManager = new ScanManager();
        boolean powerOn = mScanManager.getScannerState();
        if (!powerOn) {
            powerOn = mScanManager.openScanner();
            if (!powerOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Scanner cannot be turned on!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
        mScanManager.enableAllSymbologies(true); // or execute enableSymbologyDemo() || enableSymbologyDemo2() is the
                                                 // same.
        setTrigger(getTriggerMode());
        // setScanOutputMode(getScanOutputMode());
        setScanOutputMode(DECODE_OUTPUT_MODE_INTENT);
        registerReceiver(true);
        return powerOn;
    }

    @Override
    public void onInventoryTag(String epc, String tid, String rssi) {
        Log.d(TAG, "onInventoryTag: epc=" + epc + ", rssi=" + rssi + ", tid=" + tid);
        TagResult tagResult = new TagResult();
        tagResult.epc = epc;
        tagResult.tid = tid;
        tagResult.rssi = rssi;
        onTagRead(tagResult);
        if (reaadOne) {
            stopReadOne();
        }
    }

    @Override
    public void onInventoryTagEnd() {
        // do nothing
    }
}

class BarcodeHolder {
    CheckBoxPreference mBarcodeEnable = null;
    EditTextPreference mBarcodeLength1 = null;
    EditTextPreference mBarcodeLength2 = null;

    CheckBoxPreference mBarcodeNOTIS = null;
    CheckBoxPreference mBarcodeCLSI = null;

    CheckBoxPreference mBarcodeISBT = null;
    CheckBoxPreference mBarcodeChecksum = null;
    CheckBoxPreference mBarcodeSendCheck = null;
    CheckBoxPreference mBarcodeFullASCII = null;
    ListPreference mBarcodeCheckDigit = null;
    CheckBoxPreference mBarcodeBookland = null;
    CheckBoxPreference mBarcodeSecondChecksum = null;
    CheckBoxPreference mBarcodeSecondChecksumMode = null;
    ListPreference mBarcodePostalCode = null;
    CheckBoxPreference mBarcodeSystemDigit = null;
    CheckBoxPreference mBarcodeConvertEAN13 = null;
    CheckBoxPreference mBarcodeConvertUPCA = null;
    CheckBoxPreference mBarcodeEanble25DigitExtensions = null;
    CheckBoxPreference mBarcodeDPM = null;
    int[] mParaIds = null;
    String[] mParaKeys = null;
}

///////////////////////////////////////////////////////////////////////////////////////
class ScanCallback implements IRfidCallback {
    @Override
    public void onInventoryTag(String EPC, final String TID, final String strRSSI) {
        Log.e(TAG, "onInventoryTag:............... epc:" + EPC + "    tid:" + TID);
    }

    /**
     * 盘存结束回调(Inventory Command Operate End)
     */
    @Override
    public void onInventoryTagEnd() {
        Log.d(TAG, "onInventoryTagEnd()");

    }
}
