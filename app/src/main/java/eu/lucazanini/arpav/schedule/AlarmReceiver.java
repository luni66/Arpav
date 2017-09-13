package eu.lucazanini.arpav.schedule;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.service.NotificationService;
import hugo.weaving.DebugLog;


public class AlarmReceiver extends WakefulBroadcastReceiver {
//public class AlarmReceiver extends BroadcastReceiver {

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        PreferenceHelper preferences = new PreferenceHelper(context);

        if (preferences.isAlertActive() && action.equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmHandler alarmHandler = new AlarmHandler(context);
            alarmHandler.setNextAlarm();
        } else if (action.startsWith(AlarmHandler.RECEIVER_ACTION)) {
//            context.startService(NotificationService.getIntent(context));
            startWakefulService(context, NotificationService.getIntent(context));
        }
    }
}
