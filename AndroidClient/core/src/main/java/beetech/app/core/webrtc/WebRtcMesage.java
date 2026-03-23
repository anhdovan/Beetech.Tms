package beetech.app.core.webrtc;

public class WebRtcMesage {
    private String type;
    private String sdp;

    public WebRtcMesage(){
        setType("");
        setSdp("");
    }

    public WebRtcMesage(String type, String sdp) {
        this.setType(type);
        this.setSdp(sdp);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }
}
