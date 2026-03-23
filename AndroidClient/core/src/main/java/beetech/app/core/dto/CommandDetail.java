package beetech.app.core.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class  CommandDetail implements Serializable {
    public CommandDetail() {
    }

    public CommandDetail(long commandDetailId, long commandId, String code, String name, int quantity) {
        this.commandDetailId = commandDetailId;
        this.commandId = commandId;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
    }

    @Id
    public long id;
    //public long iuomId;
    public String code;
    public String name;
    public int quantity;
    public String note;
    public  String extInfo;
    public long commandDetailId;
    public long commandId;
    public List<CommandExecDetail> ExecDetails = new ArrayList<>();
    //public ToOne<Command> command;
}
