package eu.lucazanini.arpav.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.service.NotificationService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            String action = intent.getAction();

            PreferenceHelper preferences = new PreferenceHelper(context);

            if (preferences.isAlertActive() && action.equals("android.intent.action.BOOT_COMPLETED")) {
                AlarmHandler alarmHandler = new AlarmHandler(context);
                alarmHandler.setNextAlarm();
            } else if (action.startsWith(AlarmHandler.RECEIVER_ACTION)) {
                AlarmHandler alarmHandler = new AlarmHandler(context);
                alarmHandler.setNextAlarm();
                context.startService(NotificationService.getIntent(context));
            }
        }
    }
}
