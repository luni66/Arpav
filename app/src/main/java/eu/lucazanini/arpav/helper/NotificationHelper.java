package eu.lucazanini.arpav.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;

import eu.lucazanini.arpav.R;

@TargetApi(26)
public class NotificationHelper extends ContextWrapper {

    private NotificationManager notifManager;

//Set the channel’s ID//

    public static final String ARPAV_CHANNEL_ID = "eu.lucazanini.arpav.update";

//Set the channel’s user-visible name//

    public static final String ARPAV_CHANNEL_NAME = "Arpav Channel";

    public NotificationHelper(Context base) {
        super(base);
        createChannel();
    }

    public void createChannel() {
        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ARPAV_CHANNEL_ID,
                ARPAV_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.YELLOW);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(androidChannel);
    }

    public Notification.Builder getNotification1(String title, String body) {
        return new Notification.Builder(getApplicationContext(), ARPAV_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);
    }

    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notifManager;
    }
}
