package beetech.tms.android.data.models;

import com.google.gson.annotations.SerializedName;

public class FileReturnRequest {
    @SerializedName("returnerName")
    public String returnerName;

    @SerializedName("receiverUserName")
    public String receiverUserName;

    @SerializedName("note")
    public String note;

    public FileReturnRequest(String returnerName, String receiverUserName, String note) {
        this.returnerName = returnerName;
        this.receiverUserName = receiverUserName;
        this.note = note;
    }
}
