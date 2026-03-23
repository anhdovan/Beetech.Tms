package beetech.tms.android.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class InventoryAuditSession implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("locationId")
    public Integer locationId;

    @SerializedName("startTime")
    public Date startTime;

    @SerializedName("endTime")
    public Date endTime;

    @SerializedName("status")
    public String status;

    @SerializedName("performByName")
    public String performByName;

    public InventoryAuditSession() {
        this.startTime = new Date();
    }
}
