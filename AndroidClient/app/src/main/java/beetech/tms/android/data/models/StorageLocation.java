package beetech.tms.android.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "locations")
public class StorageLocation implements Serializable {
    @PrimaryKey
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
