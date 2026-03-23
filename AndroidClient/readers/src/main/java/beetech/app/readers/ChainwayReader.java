package beetech.app.readers;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;
import com.rscja.barcode.BarcodeUtility;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.BarcodeEntity;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.data.DeviceItem;
import beetech.app.core.dto.ReaderProfile;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.models.ReaderSettings;


public class ChainwayReader extends AdvBaseReader {
    private static final Logger log = LoggerFactory.getLogger(ChainwayReader.class);
    public RFIDWithUHFUART mReader;
//    private Barcode1D m1DInstance;
//    private Barcode2D m2DInstance;

   private BarcodeDecoder barcodeDecoder= BarcodeFactory.getInstance().getBarcodeDecoder();

    private boolean loopFlag;

    @Override
    public void initReader(int type) {
        initUHF();
        initBarCodeScanner();
//        initScan1D();
//        initScan2D();
}

private void initBarCodeScanner() {

}

//private void initScan1D(){
//    try {
//        m1DInstance = Barcode1D.getInstance();
//        if(! m1DInstance.open()){
//            activity.showToast("Cannotopen scan 1D service");
//            return;
//        }
//    } catch (ConfigurationException e) {
//        activity.showToast("Init scan 1D service failed: " +e.getMessage());
//    }
//}
//
//    private void initScan2D(){
//        try {
//            m2DInstance = Barcode2D.getInstance();
//            if(! m2DInstance.open()){
//                activity.showToast("Cannotopen scan 2D service");
//                return;
//            }
//        } catch (ConfigurationException e) {
//            activity.showToast("Init scan 2D service failed: " +e.getMessage());
//        }
//    }

    public void initUHF() {


        try {
            mReader = RFIDWithUHFUART.getInstance();
//			if(mReader.init()){
//				System.out.println("inited");
//			}
        } catch (Exception ex) {

            //activity.showToast("Reader init error: " + ex.getMessage());

            return;
        }

        if (mReader != null) {
            new InitTask().execute();
        }
    }

    @Override
    public void onTagAccess(String action, int code, String epc, String data) {
        //Read or write tag result here
        connector.onTagAccess(action, code, epc, data);
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDilog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            openScanSevice();
            boolean r = mReader.init();
            return  r;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDilog.cancel();

            if (!result) {
                //activity.showToast("init fail", false);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

//            mypDilog = new ProgressDilog(activity);
//            mypDilog.setProgressStyle(ProgressDilog.STYLE_SPINNER);
//            mypDilog.setMessage("init...");
//            mypDilog.setCanceledOnTouchOutside(false);
//            mypDilog.show();
        }
    }

    @Override
    public void initReader(int type, String address) {
        initReader(type);
    }

    @Override
    public void initReader(DeviceItem di) {

    }

    @Override
    public Boolean connect(String address) {
        return null;
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
    public void applySettings(ReaderSettings readerProfile) {

    }

    @Override
    public void lockOrUnlockTag(String epc, String accesspwd, Boolean isLock) {

    }

    @Override
    public void killTag(String epc, String accesspwd) {

    }

    @Override
    public String readUserMemory(String epc, String password) {
        return "";
    }

    @Override
    public TagOperation writeTagEpc(String oldepc, String newepc) {
        return null;
    }

    @Override
    public TagOperation writeTagEpc(String oldepc, short currentPcBits, String newepc, String tid, String accesspwd, String oldpwd) {
        return null;
    }

    @Override
    public void startInventory() {
    try {
        loopFlag = true;
        if(!mReader.startInventoryTag()) {
            log.error("Could not startinventory");
            return;
        }
        new TagThread().start();
    }
    catch (Exception e) {
        log.error("Start inventory error: " + e.getMessage());
    }
    }

    @Override
    public void stopInventory() {
            mReader.stopInventory();
            loopFlag = false;
    }

    @Override
    public void scanBarCode() {
        startScan();
    }

    @Override
    public void takePhoto() {

    }
    ////////////////////SCANNWER/////////////////////////////////////////////////////////////
    private void startScan(){
        barcodeDecoder.startScan();
    }
    private void stopScan(){
        barcodeDecoder.stopScan();
    }

    private void openScanSevice(){
        barcodeDecoder.open(activity);
        Log.e(TAG,"open()==========================:"+ barcodeDecoder.open(activity));
        BarcodeUtility.getInstance().enablePlaySuccessSound(activity,true);
        barcodeDecoder.setDecodeCallback(new BarcodeDecoder.DecodeCallback() {
            @Override
            public void onDecodeComplete(BarcodeEntity barcodeEntity) {
                String barcode = barcodeEntity.getBarcodeData();
                Log.e(TAG,"BarcodeDecoder==========================:"+barcodeEntity.getResultCode());
                if(barcodeEntity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS){
                    connector.onScanBarCode(barcode);
                }
                stopScan();
            }
        });
    }
    private void close(){
        barcodeDecoder.close();
    }

    //////////////////////////////READ TAG THREAD///////////////////////////////////////////
    class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            UHFTAGInfo res = null;
            while (loopFlag) {
                res = mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res.getTid();
                    if (strTid.length() != 0 && !strTid.equals("0000000" +
                            "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    Log.i("data","EPC:"+res.getEPC()+"|"+strResult);
                    connector.onInventoryTag(strTid, res.getEPC(), res.getRssi());
                }
            }
        }
    }

}
