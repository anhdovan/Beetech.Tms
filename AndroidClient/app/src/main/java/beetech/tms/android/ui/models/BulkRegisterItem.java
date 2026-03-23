package beetech.tms.android.ui.models;

public class BulkRegisterItem {
    public String currentEpc;
    public String targetEpc;
    public String code;
    public String status; // Scanned, Registering, Registered, Writing, Success, Failed
    public int itemId;

    public BulkRegisterItem(String currentEpc) {
        this.currentEpc = currentEpc;
        this.status = "Scanned";
    }
}
