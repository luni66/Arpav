package eu.lucazanini.arpav.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import eu.lucazanini.arpav.task.ReportService;
import hugo.weaving.DebugLog;
import timber.log.Timber;


public class AlarmReceiver extends BroadcastReceiver {

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.i("Received broadcast intent: %s", action);

        Preferences preferences = new UserPreferences(context);

        if (preferences.isAlertActivated() && action.equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmHandler alarmHandler = new AlarmHandler(context.getApplicationContext());
            alarmHandler.setNextAlarm();
        } else if(action.startsWith(AlarmHandler.RECEIVER_ACTION)){
            Timber.d("onReceive for %s", action);
            context.startService(ReportService.getIntent(context));
        }
    }
}
