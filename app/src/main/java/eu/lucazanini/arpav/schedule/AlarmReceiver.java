package eu.lucazanini.arpav.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import eu.lucazanini.arpav.service.NotificationService;
import hugo.weaving.DebugLog;


public class AlarmReceiver extends BroadcastReceiver {

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Preferences preferences = new UserPreferences(context);

        if (preferences.isAlertActivated() && action.equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmHandler alarmHandler = new AlarmHandler(context);
            alarmHandler.setNextAlarm();
        } else if (action.startsWith(AlarmHandler.RECEIVER_ACTION)) {
            context.startService(NotificationService.getIntent(context));
        }
    }
}
