package eu.lucazanini.arpav.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import eu.lucazanini.arpav.task.ReportService;
import timber.log.Timber;


public class AlarmReceiver extends BroadcastReceiver {

//    public final static String RECEIVER_ACTION = "eu.lucazanini.arpav.UPDATE_TIME";
//    public final static String ALERT_ACTION = "eu.lucazanini.arpav.ALERT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.i("Received broadcast intent: %s", action);

        Preferences preferences = new UserPreferences(context);

        if (preferences.isAlertActivated() && action.equals("android.intent.action.BOOT_COMPLETED")) {
//            setAlarm(context);
            AlarmHandler alarmHandler = new AlarmHandler();
            alarmHandler.setAlarm(context);
        } else if (action.equals(AlarmHandler.RECEIVER_ACTION)) { // only for test
            context.startService(ReportService.getIntent(context));
        }
    }

/*    private void setAlarm(Context context) {
        Timber.d("setting alarms");

//        AlarmManager[] alarmMgr = new AlarmManager[3];
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent;
        Calendar[] alarmTimes = new Calendar[3];

        //TODO why ReportService .class here and in onReceive?
//        Intent intent = new Intent(context, ReportService.class);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(RECEIVER_ACTION);
//        intent.setPackage(context.getPackageName());
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        for (int i = 0; i < alarmTimes.length; i++) {

            alarmTimes[i] = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            alarmTimes[i].setTimeInMillis(System.currentTimeMillis());
            alarmTimes[i].set(Calendar.HOUR_OF_DAY, Previsione.UPDATE_TIMES[i].getHours());
            alarmTimes[i].set(Calendar.MINUTE, Previsione.UPDATE_TIMES[i].getMinutes());

//            alarmManager[i] = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //only to test
//            alarmManager[i].setRepeating(AlarmManager.RTC_WAKEUP,
//                    alarmTimes[i].getTimeInMillis(), 1000 * 60, alarmIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTimes[i].getTimeInMillis(), 1000 * 60, alarmIntent);
//            alarmMgr[i].setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                    alarmTimes[i].getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        }
    }*/

/*
    private void removeAlarm(Context context) {
        Timber.d("removing alarms");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(RECEIVER_ACTION);
//        intent.setPackage(context.getPackageName());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(alarmIntent);
    }
*/

}
