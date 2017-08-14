package eu.lucazanini.arpav.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.model.Previsione;
import timber.log.Timber;

public class AlarmHandler {

    public final static String RECEIVER_ACTION = "eu.lucazanini.arpav.UPDATE_TIME";
    public final static String RECEIVER_ACTION_1 = "eu.lucazanini.arpav.UPDATE_TIME_1";
    public final static String RECEIVER_ACTION_2 = "eu.lucazanini.arpav.UPDATE_TIME_2";
    public final static String RECEIVER_ACTION_3 = "eu.lucazanini.arpav.UPDATE_TIME_3";
    private AlarmManager alarmManager;
    private PendingIntent[] alarmIntent;
    private Calendar[] alarmTimes;

    public AlarmHandler(Context context){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Calendar[] alarmTimes = new Calendar[3];

        alarmIntent = new PendingIntent[Previsione.UPDATE_TIME_COUNT];
        Intent[] intent = new Intent[Previsione.UPDATE_TIME_COUNT];
        for(int i=0; i<Previsione.UPDATE_TIME_COUNT; i++){
            intent[i]=new Intent(context, AlarmReceiver.class);
            intent[i].setAction(RECEIVER_ACTION+"_"+Integer.toString(i));
            alarmIntent[i] = PendingIntent.getBroadcast(context, 0, intent[i], 0);
            //        intent.setPackage(context.getPackageName());
        }

        alarmTimes = new Calendar[Previsione.UPDATE_TIME_COUNT];
        for(int i=0; i<3; i++) {
            alarmTimes[i] = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            alarmTimes[i].setTimeInMillis(System.currentTimeMillis());
            alarmTimes[i].set(Calendar.HOUR_OF_DAY, Previsione.UPDATE_TIMES[i].getHours());
            alarmTimes[i].set(Calendar.MINUTE, Previsione.UPDATE_TIMES[i].getMinutes());
            Timber.d("time is %s", alarmTimes[i].getTime());
        }
    }

    public void setAlarm() {
        Timber.d("setting alarms");

        for (int i = 0; i < Previsione.UPDATE_TIME_COUNT; i++) {
/*//          only to test
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTimes[i].getTimeInMillis(), 1000 * 60, alarmIntent);*/
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTimes[i].getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent[i]);
//            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                    alarmTimes[i].getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent[i]);
        }
    }

    public void removeAlarm() {
        Timber.d("removing alarms");

        for (int i = 0; i < Previsione.UPDATE_TIME_COUNT; i++) {
            alarmManager.cancel(alarmIntent[i]);
        }
    }
}
