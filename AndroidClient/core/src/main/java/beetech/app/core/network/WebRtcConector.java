package beetech.app.core.network;

import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import beetech.app.core.webrtc.WebRTCClient;
import beetech.app.core.webrtc.WebRtcMesage;
import io.reactivex.rxjava3.core.Single;

public  class  WebRtcConector {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnector.class);
    private final WebRTCClient rtcClient;
    public boolean connected;
    private Gson gson = new Gson();
    private String address;

    public WebRtcConector(WebRTCClient rcClient, String address) {
        this.rtcClient = rcClient;
        this.address = address;
    }
    private HubConnection hubConnection;

    public boolean connect() {
        hubConnection = HubConnectionBuilder.create(address)
                .withAccessTokenProvider(Single.defer(() -> {
                            // Your logic here.
                            return Single.just("An Access Token");
                        })
                )
                //.withHubProtocol(new MessagePackHubProtocol())
                .build();
        hubConnection.on("test", msg -> {
            System.out.println(msg);
        }, String.class);
        hubConnection.on("message", (params) -> {
            if(params.getType().equals("offer")){
                this.rtcClient.onRemoteSessionReceived(new SessionDescription(
                        SessionDescription.Type.OFFER, params.getSdp()
                ));
                this.rtcClient.answer("server");
            }
            else if(params.getType().equals("answer")){
                this.rtcClient.onRemoteSessionReceived(new SessionDescription(
                        SessionDescription.Type.ANSWER, params.getSdp()
                ));
            }
            else  if(params.getType().equals("candidate")) {
                try{
                    IceCandidate candidate = gson.fromJson(params.getSdp(),IceCandidate.class);
                    rtcClient.addIceCandidate(candidate);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, WebRtcMesage.class);
        hubConnection.<BigInteger>on("KeepAlive", (tick) ->{
            System.out.println("Keepalive msg: " + tick);
        }, BigInteger.class);

        try  {
            connected = hubConnection.start()
                    .doOnComplete(() -> {

                    })
                    .doOnError(e -> {
                        log.error(e.getMessage());
                    })
                    .blockingAwait(20, TimeUnit.SECONDS);
            if (connected) {
                log.info("Connected to the server");
            }
            else {
                log.error("Could not connect to the server");
            }
        }
        catch (Exception e){
            connected = false;
        }
        return  connected;
    }

    public void send(String sendMessage, Object data) {
        hubConnection.send(sendMessage, "room1", data);
    }
}

