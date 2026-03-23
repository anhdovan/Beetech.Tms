package beetech.app.core.network;

import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TypeReference;
//import com.unitech.lib.reader.BaseReader;
//import com.unitech.lib.reader.event.IReaderEventListener;
//import com.unitech.lib.reader.types.KeyState;
//import com.unitech.lib.reader.types.KeyType;
//import com.unitech.lib.reader.types.NotificationState;
//import com.unitech.lib.transport.types.ConnectState;
//import com.unitech.lib.types.ActionState;
//import com.unitech.lib.types.ResultCode;
//import com.unitech.lib.uhf.BaseUHF;
//import com.unitech.lib.uhf.event.IRfidUhfEventListener;
//import com.unitech.lib.uhf.params.TagExtParam;
//import com.unitech.lib.util.diagnotics.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import beetech.app.core.AdvBaseReader;
import beetech.app.core.IOperationListener;

import beetech.app.core.dto.Command;
import beetech.app.core.dto.CommandDetail;
import beetech.app.core.dto.ReaderStatus;
import beetech.app.core.dto.TagOperation;
import beetech.app.core.dto.TagResult;
import beetech.app.core.dto.WriteParams;
import io.reactivex.rxjava3.core.Single;

public class WebSocketConnector implements IConnector,
        IOperationListener
        //, com.ubx.usdk.rfid.aidl.IRfidCallback //DT50P tag read callback
//        ,IReaderEventListener,//unitech reader state
//        IRfidUhfEventListener //unitech tagread event
{
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnector.class);
    private ReaderStatus currentStatus = ReaderStatus.Unknown;
    private HubConnection hubConnection;
    private AdvBaseReader reader = null;
    private IOperationListener interceptor;

    @Override
    public void setReader(AdvBaseReader reader) {
        reader.connector = this;
        this.reader = reader;
    }

    @Override
    public void send(String cmd, String args) {
        hubConnection.send(cmd, args);
    }

    @Override
    public boolean connect() {
        return connect(serverAddress);
    }

    @Override
    public boolean connect(String address) {
        hubConnection = HubConnectionBuilder.create(address)
                .withAccessTokenProvider(Single.defer(() -> {
                            // Your logic here.
                            return Single.just("An Access Token");
                        })
                )
                //.withHubProtocol(new MessagePackHubProtocol())
                //.withConverter(new CustomGsonConverterFactory(yourCustomGsonInstance))
                .build();

        hubConnection.onWithResult("WriteTagEpc", (params) -> {
            TagOperation op = new TagOperation();
            op.opId = 102;
            op.tid = "my-tid";
            op.epc = "my-epc";
            return Single.just(op);
        }, WriteParams.class);
        hubConnection.onWithResult("GetMessage", (msg) -> {
            return Single.just("message:::OKKKKKKKKKK:::" + msg);
        }, String.class);

        hubConnection.on("ScanBarcode", (flag) -> {
            reader.scanBarCode();
        }, Boolean.class);
//        hubConnection.on("TakePhoto", (flag) -> {
//            ((MainActivity)reader.activity).takePhoto();
//        }, Boolean.class);
        hubConnection.on("StopInventory", (flag) -> {
            reader.stopInventory();
        }, Boolean.class);

//        hubConnection.on("StartStreaming", (flag) -> {
//            try {
//                MainActivity activity = ((MainActivity)reader.activity);
//
//                activity.runOnUiThread(() ->  activity.showStreaming());
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, Boolean.class);

        hubConnection.on("StartInventory", (flag) -> {
            try {
                reader.startInventory();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, Boolean.class);

        hubConnection.on("Disconnect", () -> {
            hubConnection.stop().doOnComplete(() -> {
                currentStatus = ReaderStatus.Disconnected;
            });
        });
        hubConnection.<BigInteger>on("KeepAlive", (tick) ->{
            System.out.println("Keepalive msg: " + tick);
        }, BigInteger.class);

        hubConnection.<String,  String>on("Broadcast", (param1, param2) ->{
            System.out.println(param1);
            System.out.println(param2);
        }, String.class, String.class);
        hubConnection.<Command>on("Command", cmd -> {
            CommandDetail unmatched = new CommandDetail(0, 0, "","Unmatched items", -1);
//            for (CommandDetail d :cmd.details
//            ) {
//                for (int i = 0; i < 10; i++) {
//                    CommandExecDetail e = new CommandExecDetail(d.commandDetailId, "code-ean-123", "123-456", "123" + i, new Date());
//                    d.ExecDetails.add(e);
//                    unmatched.ExecDetails.add(e);
//                }
//            }

            cmd.details.add(unmatched);
            cmd.receivedAt = new Date();
//            new DataService(reader.activity).saveCommand(cmd);
//            reader.activity.currentCommand = cmd;
//            try {
//                ((MainActivity) reader.activity).showCommands();
//            }
//            catch (Exception e){
//                System.out.println(e.getMessage());
//            }

//            System.out.println(cmd.getName());
//            cmd.setName("cmd from client");
//            cmd.setNote("Note from client");
//            hubConnection.send("Broadcast", "anhdv","hello signalr!!!");
//            hubConnection.send("ClientCommand", cmd);
        }, new TypeReference<Command>() { }.getType());
        try  {
            boolean b = hubConnection.start()
                    .doOnComplete(() -> {
                        hubConnection.stream(String.class, "TestStream", "hello stream").subscribe(s -> {
                                    log.info(s);
                                },
                                e ->
                                {
                                    log.error(e.getMessage());
                                }
                        );
                    })
                    .doOnError(e -> {
                        log.error(e.getMessage());
                    })
                    .blockingAwait(20, TimeUnit.SECONDS);
            if (b) {
                log.info("Connected to the server");
            }
            else {
                log.error("Could not connect to the server");
                return  false;
            }
        }
        catch (Exception e){
            return false;
        }

        hubConnection.send("Broadcast", "anhdv","hello signalr!!!");



        return true;
    }

    @Override
    public void receiveCommand(Command cmd) {

    }

    @Override
    public boolean sendComandExecutionDetail() {
        return false;
    }

    @Override
    public void onOperationComplete(TagOperation op) {

    }

    @Override
    public void onTagRead(TagResult tagResult) {

        hubConnection.send("TagRead", tagResult);
    }



    @Override
    public void onScanBarCode(String barcode) {

        hubConnection.send("BarCode", barcode);
        if(interceptor!=null) interceptor.onScanBarCode(barcode);
    }

    @Override
    public void ontakePicture(byte[] data) {
         hubConnection.send("Picture", data);
    }

    @Override
    public void onTagAccess(String action, int code, String epc, String data) {

    }

//    /////////CHAINWAY C72 READER//////////////////////////////////////////////
    public void onInventoryTag(String tid, String epc, String rssi) {
        TagResult tagResult = new TagResult();
        tagResult.epc = epc;
        tagResult.tid = tid;
        tagResult.rssi = rssi;
        if(interceptor!=null){
            interceptor.onTagRead(tagResult);
            hubConnection.send("CommandTagRead", epc);
        }
        onTagRead(tagResult);
    }
//
//    @Override
//    public void onInventoryTagEnd() {
//
//    }
/////////////////////UNITECH REAADER/////////////////////////////////////////
//    @Override
//    public void onRfidUhfAccessResult(BaseUHF uhf, int code, ActionState action, String epc, String data, Object params) {
//        if (interceptor!=null) {
//            if (code == ResultCode.NoError) {
//                interceptor.onTagAccess( action.name().toLowerCase().startsWith("write")?"WRITE":"READ", code, epc, data);
//            }
//        }
//    }
//
//    public void setInterceptor(IOperationListener interceptor) {
//
//        this.interceptor = interceptor;
//    }
//    //UNITECH READ TAGS
//    @Override
//    public void onRfidUhfReadTag(BaseUHF uhf, String tag, Object params) {
//        if (StringUtil.isNullOrEmpty(tag)) {
//            return;
//        }
//        float rssi = 0;
//        if (params != null) {
//            TagExtParam param = (TagExtParam) params;
//            rssi = param.getRssi();
//            reader.playTagReadSound();
//            TagResult tagResult = new TagResult();
//            tagResult.epc = tag;
//            tagResult.tid = param.getTID();
//            tagResult.rssi = "" + rssi;
//            if(interceptor!=null){
//                interceptor.onTagRead(tagResult);
//            }
//            onTagRead(tagResult);
//        }
//    }
//
//    @Override
//    public void onReaderActionChanged(BaseReader reader, ResultCode retCode, ActionState state, Object params) {
//
//    }
//
//    @Override
//    public void onReaderBatteryState(BaseReader reader, int batteryState, Object params) {
//
//    }
//
//    @Override
//    public void onReaderKeyChanged(BaseReader reader, KeyType type, KeyState state, Object params) {
//
//    }
//
//    @Override
//    public void onReaderStateChanged(BaseReader reader, ConnectState state, Object params) {
//
//    }
//
//    @Override
//    public void onNotificationState(NotificationState state, Object params) {
//
//    }
//
//    @Override
//    public void onReaderTemperatureState(BaseReader reader, double temperatureState, Object params) {
//
//    }

    public void finishCommand(Command command) {
        hubConnection.send("Broadcast", "anhdv", "connect ok!");
        try {
            Gson gs = new Gson();
            String json = gs.toJson(command);
            hubConnection.send("FinishCommand", command);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String json = mapper.writeValueAsString(command);
//            hubConnection.send("FinishCommand", json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }
}
