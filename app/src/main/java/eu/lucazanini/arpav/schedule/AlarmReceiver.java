package eu.lucazanini.arpav.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.task.ReportService;
import eu.lucazanini.arpav.xml.Previsione;
import timber.log.Timber;

/**
 * Created by luke on 09/11/16.
 */

public class AlarmReceiver extends BroadcastReceiver {

    public final static String RECEIVER_ACTION = "eu.lucazanini.arpav.UPDATE_TIME";

    public void setAlarm(Context context) {

        AlarmManager[] alarmMgr = new AlarmManager[3];
        PendingIntent alarmIntent;
        Calendar[] alarmTimes = new Calendar[3];

        Intent intent = new Intent(context, ReportService.class);
        intent.setAction(RECEIVER_ACTION);
        intent.setPackage(context.getPackageName());
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        for (int i = 0; i < alarmTimes.length; i++) {

            alarmTimes[i] = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            alarmTimes[i].setTimeInMillis(System.currentTimeMillis());
            alarmTimes[i].set(Calendar.HOUR_OF_DAY, Previsione.UPDATE_TIMES[i].getHours());
            alarmTimes[i].set(Calendar.MINUTE, Previsione.UPDATE_TIMES[i].getMinutes());

            alarmMgr[i] = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            alarmMgr[i].setRepeating(AlarmManager.RTC_WAKEUP,
//                    alarmTimes[i].getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
            alarmMgr[i].setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTimes[i].getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        }


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.i("Received broadcast intent: %s", action);
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarm(context);
        } else if (action.equals(RECEIVER_ACTION)) {
            Intent intent1 = new Intent(context, ReportService.class);
            context.startService(intent1);
        }
    }

}
