package beetech.app.core.network;


import beetech.app.core.AdvBaseReader;
import beetech.app.core.dto.Command;

public interface IConnector {
    //todo get from preference
    public String serverAddress = "ws://192.168.1.36:5000/rfidserver?r_id=3"; //todo Reader Id must be got from preference
    public String rtcServerAddress = "ws://192.168.1.35:5000/rtc?r_id=3";
    public boolean connect();
    public boolean connect(String address);
    public void receiveCommand(Command cmd);
    public boolean sendComandExecutionDetail();

    void setReader(AdvBaseReader reader);

    void send(String command, String taskId);
}
