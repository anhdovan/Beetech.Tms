package beetech.tms.android.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class InventoryAuditResult implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("inventoryAuditSessionId")
    public int inventoryAuditSessionId;

    @SerializedName("textileItemId")
    public Integer textileItemId;

    @SerializedName("tag")
    public String tag;

    @SerializedName("scanAt")
    public Date scanAt;

    @SerializedName("status")
    public String status;

    @SerializedName("isValid")
    public boolean isValid;

    @SerializedName("assetName")
    public String assetName;

    public InventoryAuditResult() {
        this.scanAt = new Date();
    }
}
