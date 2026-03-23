package beetech.app.core.dto;

public class SgtinResult {
    public String epc;
    public String gtin;
    public String serial;
    public  boolean success;

    public SgtinResult(String gtin, String serial, boolean success) {
        epc = this.gtin = gtin;
        this.serial = serial;
        this.success = success;
    }
    public SgtinResult(String epc, String gtin, String serial, boolean success) {
       this(gtin, serial, success);
       this.epc = epc;
    }
}
