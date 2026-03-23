package beetech.tms.android.ui.models;

public class WriteTagItem {
    public String plannedEpc; // The EPC we WANT to write
    public String currentEpc; // The actual EPC read from the tag
    public String name;
    public String code;
    public String status; // "Pending", "Written", "Failed", "Verified"

    public WriteTagItem(String plannedEpc, String name, String code) {
        this.plannedEpc = plannedEpc;
        this.name = name;
        this.code = code;
        this.status = "Pending";
    }
}
