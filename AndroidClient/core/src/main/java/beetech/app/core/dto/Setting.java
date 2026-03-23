package beetech.app.core.dto;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Setting {
    public Setting() {
    }

    public Setting(long id, int eaderId, String readerClass, int readerCode, String readerBlueToothAddress, String serverAddress, int serverPort, String userName, String password, int readerTriggerHandler) {
        this.id = id;
        this.eaderId = eaderId;
        this.readerClass = readerClass;
        this.readerCode = readerCode;
        this.readerBlueToothAddress = readerBlueToothAddress;
        this.serverAddress = serverAddress;
        ServerPort = serverPort;
        this.userName = userName;
        this.password = password;
        this.readerTriggerHandler = readerTriggerHandler;
    }

    @Id
    public long id;
    public int eaderId;
    public String readerClass;
    public int readerCode; //Unitech reader code
    public String readerBlueToothAddress;
    public String serverAddress;
    public  int ServerPort;
    public String userName;
    public String password;
    public int readerTriggerHandler;

}

