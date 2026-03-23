package beetech.app.core.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Command implements Serializable {

    public Command(int id, String name, String note, int commandTypeId) {
        this.id = id;
        this.commandTypeId = commandTypeId;
        this.name = name;
        this.note = note;
    }

    public Command(int commandTypeId, int id, String taskId, String name, String note) {
        this.commandTypeId = commandTypeId;
        this.id = id;
        this.taskId = taskId;
        this.name = name;
        this.note = note;
    }

    public Command() {
    }

//    public long getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getNote() {
//        return note;
//    }
//
//    public void setNote(String note) {
//        this.note = note;
//    }

    public int commandTypeId;
    @Id
    public long id; //to save to db
    public long commandId; //commandId fromsever
    public String taskId;
    public   String name;
    public  long locationId1;
    public  long locationId2;
    public  String locationName;
    public   String note;
    public Date receivedAt;
    public  Date completeAt;
    public  int status; //0: new, 1: executing, 2: complete
    //@Backlink
    public List<CommandDetail> details = new ArrayList<>();

}

