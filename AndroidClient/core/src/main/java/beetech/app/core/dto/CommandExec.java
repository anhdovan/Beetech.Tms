package beetech.app.core.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CommandExec implements Serializable {
    public CommandExec(int id, String name, String note) {
        this.id = id;
        this.note = note;
    }

    public CommandExec(int commandTypeId, int id, long commandId, String name, String note) {
        this.id = id;
        this.commandId = commandId;
        this.note = note;
    }

    public CommandExec() {
    }

    @Id
    public long id;
    public long commandId;
    public Date execAt;
    public   String note;
    public  int status; //1: executing, 2: complete

    public List<CommandExecDetail> getDetails() {
        return details;
    }

    public void setDetails(List<CommandExecDetail> details) {
        this.details = details;
    }
    //@Backlink
    public List<CommandExecDetail> details;

}

