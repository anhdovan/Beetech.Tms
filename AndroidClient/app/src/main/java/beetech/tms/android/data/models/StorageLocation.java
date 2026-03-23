package beetech.tms.android.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class StorageLocation implements Serializable {
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("code")
    public String code;

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
