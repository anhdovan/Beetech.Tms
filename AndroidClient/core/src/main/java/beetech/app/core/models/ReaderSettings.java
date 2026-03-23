package beetech.app.core.models;

public class ReaderSettings {
    public float power = 15;
    public int rssi = -55;
    public boolean continuousMode = false;
    public int maxQ = 2;
    public int minQ = 1;
    public int startQ = 1;
    public int session = 0; // 0:S0, 1:S1, 2:S2, 3:S3
    public int target = 0; // 0:A, 1:B
    public boolean toggleTarget;
    public int readMode = 0;
    public String readerClass; // New field to store the selected reader class

    public ReaderSettings() {}

    public ReaderSettings(float power, int rssi, boolean continuousMode, int maxQ, int minQ, int startQ,
                          int session, int target, boolean toggleTarget, int readMode, String readerClass) {
        this.power = power;
        this.rssi = rssi;
        this.continuousMode = continuousMode;
        this.maxQ = maxQ;
        this.minQ = minQ;
        this.startQ = startQ;
        this.session = session;
        this.target = target;
        this.toggleTarget = toggleTarget;
        this.readMode = readMode;
        this.readerClass = readerClass;
    }
}
