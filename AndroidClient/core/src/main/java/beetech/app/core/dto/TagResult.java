package beetech.app.core.dto;

public class TagResult {
    public String tid;
    public String epc;
    public  String rssi;
    public int commandId;
    public String pc;

    public TagResult(String epc) {
        this.epc = epc;
    }

    public TagResult() {

    }

    public String getEpc() {
        return epc;
    }
}
