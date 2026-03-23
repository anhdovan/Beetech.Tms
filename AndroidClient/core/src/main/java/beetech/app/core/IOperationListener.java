package beetech.app.core;


import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;

public  interface IOperationListener {// extends IRfidCallback {
    void onOperationComplete(TagOperation op);
    void onTagRead(TagResult tagResult);
    void onScanBarCode(String barcode);
    void ontakePicture(byte[] data);
    //void onTagAccess(String action, ResultCode code, String epc, String data);
    void onTagAccess(String action, int code, String epc, String data);
}
