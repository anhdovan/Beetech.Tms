package beetech.tms.android.data.models;

import com.google.gson.annotations.SerializedName;

public class FileLoanRequest {
    @SerializedName("customerFileId")
    public int customerFileId;
    
    @SerializedName("purpose")
    public String purpose;
    
    @SerializedName("userName")
    public String userName;

    @SerializedName("expectedReturnDate")
    public String expectedReturnDate;

    public FileLoanRequest() {}

    public FileLoanRequest(int customerFileId, String purpose, String userName, String expectedReturnDate) {
        this.customerFileId = customerFileId;
        this.purpose = purpose;
        this.userName = userName;
        this.expectedReturnDate = expectedReturnDate;
    }
}
