package beetech.app.core.notification;

public class Notification {
    public int id;
    public String title;
    public String message;
    public long timestamp;
    public NotificationSeverity severity; // Add severity

    public Notification(int id, String title, String message, NotificationSeverity severity) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.severity = severity;
    }
}
