package beetech.app.core.dto;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CommandExecDetail
        implements Serializable {
    public CommandExecDetail() {
    }


    public CommandExecDetail(long commandDetailId) {
        this.commandDetailId = commandDetailId;
    }

    public CommandExecDetail(long detailId, String codeRead, String gtin, String serialNumber, Date readAt) {
        this.commandDetailId = detailId;
        this.codeRead = codeRead;
        this.gtin = gtin;
        this.serialNumber = serialNumber;
        this.readAt = readAt;
    }
    @Id
    public  long id;
    public String code;
    public String name;
    public String note;
    public long commandDetailId;
    public String codeRead;
    public String gtin;
    public String serialNumber;
    public Date readAt;
}
