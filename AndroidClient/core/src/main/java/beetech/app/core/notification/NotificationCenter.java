package beetech.app.core.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationCenter {
    public static List<Notification> notifications = new ArrayList<>();
    private static List<NotificationListener> listeners = new ArrayList<>();

    public static void postNotification(Notification notification) {
        notifications.add(notification);
        for (NotificationListener listener : listeners) {
            listener.onNotificationReceived(notification);
        }
    }

    public static List<Notification> getNotifications() {
        return notifications;
    }

    public static void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public interface NotificationListener {
        void onNotificationReceived(Notification notification);
    }
}
