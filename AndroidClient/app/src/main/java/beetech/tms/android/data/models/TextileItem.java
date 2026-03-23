package beetech.tms.android.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import beetech.app.core.utils.ChaCha20Util;

@Entity(tableName = "items")
public class TextileItem implements Serializable {
    @PrimaryKey
    @SerializedName("id")
    public int id;
    
    @SerializedName("code")
    public String code;
    
    @SerializedName("status")
    public String status;
    
    @SerializedName("category")
    public String category;
    
    @SerializedName("location")
    public String location;
    
    @SerializedName("department")
    public String department;
    
    @SerializedName("washCount")
    public int washCount;
    
    @SerializedName("epc")
    public String epc;

    @Ignore
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getExpectedEpc() {
        // AssetTag prefix is 3001
        return ChaCha20Util.encryptExt(id, "3001");
    }
}
