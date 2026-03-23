package beetech.app.readers;

import static com.unitech.lib.types.DeviceType.RP902;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.unitech.lib.diagnositics.ReaderException;
import com.unitech.lib.htx.HT730Reader;
import com.unitech.lib.reader.BaseReader;
import com.unitech.lib.reader.event.IReaderEventListener;
import com.unitech.lib.reader.params.DisplayTags;
import com.unitech.lib.reader.params.ScreenOffTime;
import com.unitech.lib.reader.types.BeeperState;
import com.unitech.lib.reader.types.KeyState;
import com.unitech.lib.reader.types.KeyType;
import com.unitech.lib.reader.types.NotificationState;
import com.unitech.lib.reader.types.VibratorState;
import com.unitech.lib.rgx.RG768Reader;
import com.unitech.lib.rpx.RP902Reader;
import com.unitech.lib.transport.TransportBluetooth;
import com.unitech.lib.transport.types.ConnectState;
import com.unitech.lib.types.ActionState;
import com.unitech.lib.types.BeepAndVibrateState;
import com.unitech.lib.types.DeviceType;
import com.unitech.lib.types.ReadMode;
import com.unitech.lib.types.ReadOnceState;
import com.unitech.lib.types.ResultCode;
import com.unitech.lib.uhf.BaseUHF;
import com.unitech.lib.uhf.event.IRfidUhfEventListener;
import com.unitech.lib.uhf.params.Lock6cParam;
import com.unitech.lib.uhf.params.SelectMask6cParam;
import com.unitech.lib.uhf.params.TagExtParam;
import com.unitech.lib.uhf.types.AlgorithmType;
import com.unitech.lib.uhf.types.BLFType;
import com.unitech.lib.uhf.types.BankType;
import com.unitech.lib.uhf.types.LockState;
import com.unitech.lib.uhf.types.Mask6cAction;
import com.unitech.lib.uhf.types.Mask6cTarget;
import com.unitech.lib.uhf.types.PowerMode;
import com.unitech.lib.uhf.types.Session;
import com.unitech.lib.uhf.types.TARIType;
import com.unitech.lib.uhf.types.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.IOperationListener;
import beetech.app.core.data.DeviceItem;
import beetech.app.core.dto.ReaderProfile;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.app.core.interfaces.IBarcodeReceiver;
import beetech.app.core.interfaces.IKeyReceiver;
import beetech.app.core.models.ReaderSettings;


public class UnitechReaderV0 extends AdvBaseReader implements IReaderEventListener, IRfidUhfEventListener {
    public final int MAX_MASK = 2;
    private final int NIBLE_SIZE = 4;
    /////////////////////////////////////////////////////
    final static String ACTION_START_SCANSERVICE = "unitech.scanservice.start";
    final static String ACTION_CLOSE_SCANSERVICE = "unitech.scanservice.close";
    final static String ACTION_SCAN2KEY_SETTING = "unitech.scanservice.scan2key_setting";
    final static String ACTION_INIT_INTENT = "unitech.scanservice.init";
    final static String ACTION_RECEIVE_DATA = "unitech.scanservice.data";
    final static String ACTION_RECEIVE_DATABYTES = "unitech.scanservice.databyte";
    final static String ACTION_RECEIVE_DATALENGTH = "unitech.scanservice.datalength";
    final static String ACTION_RECEIVE_DATATYPE = "unitech.scanservice.datatype";

    final static String ACTION_SOFTWARE_SCANKEY = "unitech.scanservice.software_scankey";

    final static int CMD_TRIGGER_SOFTWARE_SCAN_KEY = 0;
    final static int CMD_START_SCANSERVICE = 1;             // 1.8
    final static int CMD_CLOSE_SCANSERVICE = 2;             // 1.7
    final static int CMD_OUTPUTMODE_INTENT = 3;             // 1.2
    final static int CMD_OUTPUTMODE_SCAN2KEY = 4;           // 1.1
    private static final Logger log = LoggerFactory.getLogger(UnitechReaderV0.class);
    private Intent intent;
    private String action = "";
    private boolean sccanServiceInited;

    public BaseReader baseReader = null;
    private DeviceType deviceType;
    private boolean readerInited;
    private CountDownLatch latch;
    private String pcBitsData;
    private ResultCode writeResultCode;
    private String writeResultMessage;
    private boolean isStop;
    private boolean settingsInited;
    private final BlockingQueue<ResultCode> resultQueue = new ArrayBlockingQueue<>(1);

    @Override
    public void initReader(int type) {
        initReader(type, "");
    }

    @Override
    public void  initReader(int type, String address) {
        this.deviceType =  DeviceType.valueOf(type);
        //region Connect to the reader
        Executor connectExecutorService = Executors.newFixedThreadPool(1);
        connectExecutorService.execute(() -> {
            try {
                if(readerInited) return;
                switch (deviceType) {
                    case Unknown: //No RFID support readers -> init arcodescanner
                        initScan();
                        break;
                    case RP902:
                        TransportBluetooth tb = new TransportBluetooth(RP902, "RP902",address);//TODO GET MAC ADDRESS
                        baseReader = new RP902Reader(tb);
                        baseReader.addListener(this);
                        baseReader.connect();
                        break;
                    case HT730:
                        baseReader = new HT730Reader(activity.getApplicationContext());
                        baseReader.addListener(this);
                        try {
                            boolean ok = baseReader.connect();
                            if (!ok) {
                                activity.runOnUiThread(() -> activity.showToast("Connect reader failed"));
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case RG768:
                        baseReader = new RG768Reader(activity.getApplicationContext());
                        baseReader.addListener(this);
                        baseReader.connect();
                        break;
                }
                readerInited = baseReader.initReader();

            } catch (Exception e) {
                e.printStackTrace();
//                Logger.error(e.toString());
//                activity.showToast("Connect exception: " + e.toString());
            }
        });
        //endregion
    }

    @Override
    public void initReader(DeviceItem di) {

    }

    @Override
    public Boolean connect(String address) {
        return true;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public String getDeviceInfo() {
        return "";
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void applySettings(ReaderProfile readerProfile) {

    }

    @Override
    public void applySettings(ReaderSettings settings) {
        try {
            baseReader.getRfidUhf().setPower((int) settings.power);
            baseReader.getRfidUhf().setMaxQ((int) settings.maxQ);;
            baseReader.getRfidUhf().setMaxQ((int) settings.maxQ);;
            baseReader.getRfidUhf().setStartQ((int) settings.startQ);;
            baseReader.getRfidUhf().setSession(Session.valueOf(settings.session));
            baseReader.getRfidUhf().setTarget(Target.valueOf(settings.target));
            baseReader.getRfidUhf().setToggleTarget(settings.toggleTarget);
            baseReader.setReadMode(ReadMode.valueOf(settings.readMode));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lockOrUnlockTag(String epc, String accesspwd, Boolean isLock) {
        setSelectMask(epc);
        Lock6cParam p = new Lock6cParam();
        p.setAccessPassword(isLock?LockState.Lock:LockState.Unlock);
        p.setEpc(isLock?LockState.Lock:LockState.Unlock);
        p.setKillPassword(isLock?LockState.Lock:LockState.Unlock);
        baseReader.getRfidUhf().lock6c(p, accesspwd);
    }

    @Override
    public void killTag(String epc, String accesspwd) {

    }

    @Override
    public String readUserMemory(String epc,String password) {
        baseReader.getRfidUhf().readMemory6c(BankType.User, 0, 64, password);
        return "";
    }

    private Object lock = new Object();
    @Override
    public TagOperation writeTagEpc(String oldepc, String newepc) {
        TagOperation op = new TagOperation();
        op.epc = oldepc;
        op.newepc = newepc;
        op.success = true;

        try {
//            SyncRfidListener listener = new SyncRfidListener();
//            baseReader.getRfidUhf().addListener(listener);

            // Step 1: Write EPC
            ResultCode req = baseReader.getRfidUhf()
                    .writeMemory6c(BankType.EPC, 2, newepc, "00000000");
            if (req != ResultCode.NoError) {
                op.success = false;
                op.message = "Failed to initiate EPC write: " + req.name();
                return op;
            }
            ResultCode result = awaitResult(1000);
            Log.i("WAIT-RESULT", "Name: " + result.name() + "-Code:" + result.getCode() + "-Message: " + result.getMessage());
            if (result != ResultCode.NoError) {
                op.success = false;
                op.message = "EPC write failed: " + result.name();
                return op;
            }
            Log.i("WRITE-EPC", newepc + "-" + result);
            if(oldepc.length() ==newepc.length())//no need to update pcbits
                return op;
            // Step 2: Write PC bits
            String pcBits = calculatePCBits(newepc.length() / 2);
            req = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 0, pcBits, "00000000");
            if (req != ResultCode.NoError) {
                op.success = false;
                op.message = "Failed to initiate PC bits write: " + req.name();
                return op;
            }

            result = awaitResult(1000);
            if (result != ResultCode.NoError) {
                op.success = false;
                op.message = "PcBits write failed: " + result.name();
                return op;
            }
            Log.i("WRITE-PCBITS", newepc + "-" + req);
        } catch (Exception e) {
            op.success = false;
            op.message = "Exception: " + e.getMessage();
        }

        return op;
    }


//    public TagOperation writeTagEpc(String oldepc, String newepc) {
//        synchronized (lock){
//            if (oldepc == null || oldepc.isEmpty()) return null;
//            if (newepc == null || newepc.isEmpty()) return null;
//
//            TagOperation op = new TagOperation();
//            op.epc = oldepc;
//            op.newepc = newepc;
//            op.opId = oldepc.hashCode();
//            op.success = true;
//
//            if (oldepc.equals(newepc)) return op;
//
//            if (!setSelectMask(oldepc)) {
//                op.success = false;
//                op.message = "Cannot select EPC: " + oldepc;
//                return op;
//            }
//            try {
//                //latch = new CountDownLatch(1); // Initialize latch for waiting
//
//                // Step 1: Write new EPC value
//                ResultCode writeEpcResult = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 2, newepc, "00000000");
//                if (writeEpcResult != ResultCode.NoError) {
//                    op.success = false;
//                    op.resultCode = writeEpcResult.getCode();
//                    op.message = "Failed to initiate EPC write: " + writeEpcResult.name();
//                    Log.e("RFIDError", "EPC write initiation failed: " + writeEpcResult.name());
//                    return op;
//                }
//                Thread.sleep(100);
//                // Step 2: Update PC bits
//                String pcBits = calculatePCBits(newepc.length() / 2);
//                Log.d("PCBits", "PCBits: " + pcBits);
//                //String oldPcBits = readPcBits();
//                writeEpcResult = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 0, pcBits, "00000000");
//                if (writeEpcResult != ResultCode.NoError) {
//                    op.success = false;
//                    op.resultCode = writeEpcResult.getCode();
//                    op.message = "Failed to initiate EPC write: " + writeEpcResult.name();
//                    Log.e("RFIDError", "EPC write initiation failed: " + writeEpcResult.name());
//                    return op;
//                }
//
//            }  catch (RuntimeException ex) {
//                op.success = false;
//                op.message = "Runtime exception: " + ex.getMessage();
//                Log.e("RFIDError", "Runtime Exception: ", ex);
//            } catch (Exception e) {
//                op.success = false;
//                op.message = "Exception  while waiting for RFID write result.";
//                Log.e("RFIDError", "Interrupted Exception: ", e);
//            }catch (Throwable e) {
//                op.success = false;
//                op.message = "Unexpected error: " + e.getMessage();
//                Log.e("RFIDError", "Unhandled Exception: ", e);
//            }
//
//            return op;
//        }
//    }

    public String readPcBits() {
        latch = new CountDownLatch(1); // Initialize the latch

        // Start RFID read operation
        ResultCode result = baseReader.getRfidUhf().readMemory6c(BankType.EPC, 0, 1, "00000000");
        if (result != ResultCode.NoError) {
            Log.e("RFIDError", "Failed to initiate PC bit read: " + result.name());
            return null;
        }

        try {
            // Wait for the RFID response, with a timeout of 3 seconds
            boolean success = latch.await(3, TimeUnit.SECONDS);
            if (!success) {
                Log.e("RFIDError", "Timeout while waiting for PC bits.");
                return null;
            }
        } catch (InterruptedException e) {
            Log.e("RFIDError", "Interrupted while waiting for PC bits.", e);
            return null;
        }

        return pcBitsData; // Return the received data
    }
//    public TagOperation writeTagEpc(String oldepc, String newepc) {
//        //check if epc is valid (not null or empty)
//        if(oldepc==null || oldepc.isEmpty()) return null;
//        if(newepc==null || newepc.isEmpty()) return null;
//
//        TagOperation op = new TagOperation();
//        op.epc = oldepc;
//        op.newepc = newepc;
//        op.opId = oldepc.hashCode();
//        op.success = true;
//        if(oldepc.equals(newepc)) return  op;
//        if(!setSelectMask(oldepc)) {
//            op.success = false;
//            op.message = "Cannot select epc: " + oldepc;
//            return op;
//        }
//        try {
//            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
//                Log.e("GlobalException", "Unhandled error: ", throwable);
//            });
//
//            ResultCode writeresult = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 2, newepc, "00000000");
//            String pcBits = calculatePCBits(newepc.length()/2);
//            writeresult = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 0, pcBits, "00000000"); // Update PC bits
//            if (writeresult != ResultCode.NoError) op.success = false;
//            op.resultCode = writeresult.getCode();
//        }
//        catch (RuntimeException ex){
//            op.success = false;
//            op.message = ex.getMessage();
//        }
//        catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//
//        return op;
//    }

    @Override
    public TagOperation writeTagEpc(String oldepc, short currentPcBits, String newepc, String tid, String accesspwd, String newaccesspwd) {
        TagOperation op = new TagOperation();
        op.epc = oldepc;
        op.newepc = newepc;
        op.opId = oldepc.hashCode();
        op.success = true;
        if(!setSelectMask(oldepc)) {
            op.success = false;
            op.message = "Cannot select epc: " + oldepc;
            return op;
        }
        try {
            op.resultCode = baseReader.getRfidUhf().writeMemory6c(BankType.EPC, 2, newepc, accesspwd).getCode();
            if(op.resultCode == 0) { //writesuccessfully -> lock tag
                if(newaccesspwd!=null && newaccesspwd!="") {
                    lockOrUnlockTag(newepc, accesspwd, false);
                    lockOrUnlockTag(newepc, newaccesspwd, true);
                }
            }
        }
        catch (Exception e) {
            op.success = false;
            op.message = e.getMessage();
        }
        return op;
    }

    @Override
    public void startInventory() {
        if(baseReader == null) {
            return;
        }
        if (baseReader.getAction() == ActionState.Stop) {
                doInventory();
        }
        else {
            doStop();
            doInventory();
        }
    }

    @Override
    public void stopInventory() {
        if(baseReader == null) return;
        if (baseReader.getAction() == ActionState.Inventory6c) {
            doStop();
        }
    }
/////////////////////////////////////////////////////////////////////
private void doInventory() {
    try {
        isStop = false;
        clearSelectMask();

        ensureSettings();

        configureReader();

        //ResultCode result = tryStartInventoryWithReconnect(10);
        stopInventory();
        ResultCode result = baseReader.getRfidUhf().inventory6c();
                Log.i("READER", "Start-inventory=" + result.getMessage());

        if (result != ResultCode.NoError && result != ResultCode.InOperation) {
            throw new ReaderException(result);
        }

    } catch (ReaderException e) {
        log.error("ReaderException", e);
        //activity.showToast("Inventory failed: " + e.getMessage());
    } catch (Exception e) {
        log.error("Unexpected exception", e);
        //activity.showToast("Inventory failed: " + e.getMessage());
    }
}

    private void ensureSettings() throws Exception {
        if (!settingsInited) {
            assertReader();
            initSetting();
            settingsInited = true;
        }
    }

    private void configureReader() {
        baseReader.setDisplayTags(new DisplayTags(ReadOnceState.Off, BeepAndVibrateState.On));
        baseReader.setSoftWareScan(true);
        baseReader.getRfidUhf().clearListener();
        baseReader.getRfidUhf().addListener(this);
    }

    private ResultCode tryStartInventoryWithReconnect(int maxRetries) {
        ResultCode result = baseReader.getRfidUhf().inventory6c();
        int attempts = 0;

        while ((result != ResultCode.NoError && result != ResultCode.InOperation) && attempts < maxRetries) {
            attempts++;
            log.warn("Inventory failed, attempt " + attempts + " — reconnecting...");
            baseReader.disconnect();
            boolean connected = baseReader.connect();
            if (!connected) {
                continue;
            }
            result = baseReader.getRfidUhf().inventory6c();
        }

        return result;
    }

    public void clearSelectMask() throws ReaderException {
        for (int i = 0; i < MAX_MASK; i++) {
            try {
                baseReader.getRfidUhf().setSelectMask6cEnabled(i, false);
                log.debug("clearSelectMask successful");
            } catch (ReaderException e) {
                //throw e;
            }
        }
    }
    public boolean setSelectMask(String maskEpc) {
        SelectMask6cParam param = new SelectMask6cParam(
                true,
                Mask6cTarget.SL,
                Mask6cAction.AB,
                BankType.EPC,
                0,
                maskEpc,
                maskEpc.length() * NIBLE_SIZE);
        try {
            for (int i = 0; i < MAX_MASK; i++) {
                baseReader.getRfidUhf().setSelectMask6cEnabled(i, false);
            }
            baseReader.getRfidUhf().setSelectMask6c(0, param);
            log.debug("setSelectMask success: " + param.toString());
        } catch (ReaderException e) {
            log.error("setSelectMask failed: \n" + e.getCode().getMessage());
            //activity.showToast("setSelectMask failed");
            return false;
        }
        return true;
    }
    @Override
    public void setContinousMode(boolean enable) {
        try {
            baseReader.getRfidUhf().setContinuousMode(enable);
        } catch (ReaderException e) {
            System.out.println(e.getMessage());
            //throw new RuntimeException(e);
        }
    }
@Override
public void registerListener(IOperationListener listener) {
        super.registerListener(listener);
}

//    @Override
//    public void registerListener(IRfidUhfEventListener listener) {
//       baseReader.getRfidUhf().clearListener();
//        baseReader.getRfidUhf().addListener(listener);
//    }
    private   void initSetting() {

        try {
            baseReader.setSoftWareScan(true);
            baseReader.getRfidUhf().setSession(Session.S0);
            baseReader.getRfidUhf().setContinuousMode(true);
            baseReader.getRfidUhf().setInventoryTime(200);
            baseReader.getRfidUhf().setIdleTime(20);


            baseReader.getRfidUhf().setAlgorithmType(AlgorithmType.FixedQ);

            baseReader.getRfidUhf().setStartQ(1);
            baseReader.getRfidUhf().setMaxQ(10);
            baseReader.getRfidUhf().setMinQ(1);

            baseReader.getRfidUhf().setTarget(Target.A);

            baseReader.getRfidUhf().setToggleTarget(true);

            switch (deviceType) {
                case RP902:
                    baseReader.getRfidUhf().setPower(22);

//                    baseReader.setScreenOffTime(new ScreenOffTime(2, 0));
//                    baseReader.setAutoOffTime(2);

                    baseReader.setBeeper(BeeperState.Medium);
                    baseReader.setVibrator(VibratorState.On);

//                    baseReader.getRfidUhf().setTARI(TARIType.T_25_00);
//                    baseReader.getRfidUhf().setBLF(BLFType.BLF_256);
//                    baseReader.getRfidUhf().setFastMode(true);

                    Date currentTime = Calendar.getInstance().getTime();
                    baseReader.setTime(currentTime);
                    break;
                case HT730:
                    //baseReader.getRfidUhf().setFastMode(true);//not support
                    baseReader.getRfidUhf().setPower(22);
                    baseReader.getRfidUhf().setModuleProfile(0);
                    baseReader.getRfidUhf().setPowerMode(PowerMode.Optimized);
                    break;
            }
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }
    private void assertReader() throws Exception {
        if (baseReader == null) {
            throw new Exception("Reader is not ready");
        } else if (baseReader.getState() != ConnectState.Connected) {
            boolean connected = baseReader.connect();
            if(!connected) throw new Exception("Reader is not connected");
        }
    }
    private void doStop() {
        baseReader.setSoftWareScan(false);
        baseReader.getRfidUhf().stop();
        isStop = true;
    }
    
    ////////////////////////////////////////////////////////////////
    @Override
    public void scanBarCode() {
        if(!sccanServiceInited) {
            initScan();
        }
        //initScan();
        startScan();
    }

    @Override
    public void takePhoto() {

    }

    @Override
    public void onOperationComplete(TagOperation op) {

    }

    @Override
    public void onTagAccess(String action, int code, String epc, String data) {

    }

    //    @Override
//    public void onScanBarCode(String barcode) {
//
//    }
//
//    @Override
//    public void ontakePicture(byte[] data) {
//
//    }
//
    /////////////////////////////////////// BARCODE //////////////////////////////////////////////////////////////
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if  (bundle == null ) return;
            switch (action) {
                case ACTION_RECEIVE_DATA:
                    //soundpool.play(soundid, 1, 1, 0, 1, 1);
                    //mVibrator.vibrate(100);
                    //mVibrator.vibrate(VibrationEffect.createOneShot(1000, 255));
                    String barcodeStr = bundle.getString("text" ).replace("\n", "");
//                    Toast.makeText(activity.getApplicationContext(), barcodeStr, LENGTH_LONG).show();
//                    mbarcodeReceivers.forEach(r -> {
//                        r.onBarcodeScanned(barcodeStr);
//                    });
                    onScanBarCode(barcodeStr);
                    //Toast.makeText(getApplicationContext(), barcodeStr, Toast.LENGTH_LONG).show();
                    break;
                case ACTION_SOFTWARE_SCANKEY:
                    break;
                /*
                    case ACTION_RECEIVE_DATALENGTH:
                    int dataLength = bundle.getInt("text" );
                    txtIntentData.append(dataLength+"\n");
                    break;
                case ACTION_RECEIVE_DATATYPE:
                    int barcodeType = bundle.getInt("text" );
                    txtIntentData.append(symbologyName[barcodeType]+"\n");
                    break;*/
            }
        }

    };
    private void setOutputModeIntent(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("enable", true);
        action =                     action = ACTION_INIT_INTENT;
        doAction(bundle);
    }
    private void initScan() {
//        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
//        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
        //stopScanService();
        startScanService();
        setOutputModeIntent();
        startReceiveData();
        //broadcast intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_DATA);
        filter.addAction(ACTION_SOFTWARE_SCANKEY);
        filter.addAction(ACTION_RECEIVE_DATABYTES);
        filter.addAction(ACTION_RECEIVE_DATALENGTH);
        filter.addAction(ACTION_RECEIVE_DATATYPE);
        activity.registerReceiver(mScanReceiver, filter);
        sccanServiceInited = true;
    }
    
    private void startScanService(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("close", true);
        action = ACTION_START_SCANSERVICE;
        doAction(bundle);
    }
    private void startReceiveData(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("scan2key", false);
        action = ACTION_SCAN2KEY_SETTING;
        doAction(bundle);
    }
    private void stopScanService(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("close", true);
        action = ACTION_CLOSE_SCANSERVICE;
        doAction(bundle);
    }

    private void startScan(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("enable", true);
        action = ACTION_SOFTWARE_SCANKEY;
        doAction(bundle);
    }
    private void stopScan(){
        Bundle bundle = new Bundle();
        bundle.putBoolean("enable", false);
        action = ACTION_SOFTWARE_SCANKEY;
        doAction(bundle);
    }
    @Override
    public void initBarCodeScan() {
        initScan();
    }
    private void doAction(Bundle bundle) {
        intent = new Intent().setAction(action).putExtras(bundle);
        activity.sendBroadcast(intent);
    }

    private List<IKeyReceiver> mkeyReceivers = new ArrayList<>();
    private List<IBarcodeReceiver> mbarcodeReceivers = new ArrayList<>();
    private List<IOperationListener> mlisteners = new ArrayList<>();

    public void registerBarcodeReceiver(IBarcodeReceiver receiver){
        mbarcodeReceivers.add(receiver);

    }
    public void unregisterBarcodeReceiver(IBarcodeReceiver receiver){
        mbarcodeReceivers.remove(receiver);

    }
    public void registerKeydownReceiver(IKeyReceiver receiver){
        mkeyReceivers.add(receiver);

    }
    public void unregisterKeydownReceiver(IKeyReceiver receiver){
        mkeyReceivers.remove(receiver);

    }

    @Override
    public void onReaderActionChanged(BaseReader reader, ResultCode retCode, ActionState state, Object params) {

    }

    @Override
    public void onReaderBatteryState(BaseReader reader, int batteryState, Object params) {

    }

    @Override
    public void onReaderKeyChanged(BaseReader reader, KeyType type, KeyState state, Object params) {
        Log.i("READER-KEY", "Type="+type +", state=" + state);
    }

    @Override
    public void onReaderStateChanged(BaseReader reader, ConnectState state, Object params) {
        Log.i("READER-EVENT", "Conect state="+ state);
    }

    @Override
    public void onNotificationState(NotificationState state, Object params) {

    }

    @Override
    public void onReaderTemperatureState(BaseReader reader, double temperatureState, Object params) {

    }

//    @Override
//    public void onRfidUhfAccessResult(BaseUHF uhf, ResultCode code, ActionState action, String epc, String data, Object params) {
//        if(action == ActionState.ReadMemory6c) {
//            if (code == ResultCode.NoError && !StringUtil.isNullOrEmpty(data)) {
//                pcBitsData = data;  // Store the received PC bits
//                latch.countDown();  // Signal that data is ready
//            } else {
//                Log.e("RFIDError", "Failed to read PC bits.");
//                latch.countDown();  // Ensure latch releases even on failure
//            }
//        }
//        onTagAccess(action.toString(), code.getCode(), epc, data);
//    }
@Override
public void onRfidUhfAccessResult(BaseUHF uhf, ResultCode code, ActionState action, String epc, String data, Object params) {
        if (epc == null||epc.isEmpty()) return;
        Log.i("TAG_ACESS",  epc + ", " + code + ", " + action + ", " + data);
        if(code == ResultCode.NoError) {
            Log.i("TAG_ACESS_OK",  epc + ", " + code + ", " + action + ", " + data);
            resultQueue.offer(code);
        }
//    if (latch != null) {
//        if (action == ActionState.ReadMemory6c) {
//            if (code == ResultCode.NoError && !StringUtil.isNullOrEmpty(data)) {
//                pcBitsData = data;  // Store the received PC bits
//                latch.countDown();  // Signal that data is ready
//            }
//            else if (action== ActionState.WriteMemory6c) {
//                writeResultCode = code;
//                writeResultMessage = code == ResultCode.NoError ? "Success" : code.toString();
//                latch.countDown();  // Ensure latch releases even on failure
//            }
//            onTagAccess(action.toString(), code.getCode(), epc, data);
//        }
//    }
    onTagAccess(action == ActionState.WriteMemory6c?"WriteMemory":action.toString(), code.getCode(), epc, data);
}

    @Override
    public void onRfidUhfReadTag(BaseUHF uhf, String tag, Object params) {
        synchronized (activity) {
            if(isStop) return;
            if (tag == null) return;
            TagExtParam p = (TagExtParam) params;
            TagResult tagResult = new TagResult();
            tagResult.epc = tag;
            tagResult.tid = p.getTID();
            tagResult.rssi = String.valueOf(p.getRssi());
            tagResult.pc = p.getPC();
            onTagRead(tagResult);
            if(reaadOne) {
                stopReadOne();
            }
        }
        //connector.onTagAccess(null, 0, tag, null);

    }

    public ResultCode awaitResult(long timeoutMs) throws InterruptedException {

        ResultCode code = resultQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        return code != null ? code : ResultCode.Timeout;
    }

    static class SyncRfidListener implements IRfidUhfEventListener {

        private final BlockingQueue<ResultCode> resultQueue = new ArrayBlockingQueue<>(1);

        @Override
        public void onRfidUhfAccessResult(BaseUHF uhf,
                                          ResultCode code,
                                          ActionState action,
                                          String epc,
                                          String data,
                                          Object params) {
            // Capture the result of a write/read/lock operation
            resultQueue.offer(code);
        }

        @Override
        public void onRfidUhfReadTag(BaseUHF uhf, String tag, Object params) {
            // You can handle inventory events here if needed
        }

        /**
         * Wait synchronously for the next access result.
         */
        public ResultCode awaitResult(long timeoutMs) throws InterruptedException {

            ResultCode code = resultQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            return code != null ? code : ResultCode.Timeout;
        }
    }
}



