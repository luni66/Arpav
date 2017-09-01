package eu.lucazanini.arpav.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.model.Previsione;
import hugo.weaving.DebugLog;

public class AlarmHandler {

    public final static String RECEIVER_ACTION = "eu.lucazanini.arpav.UPDATE_TIME";
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    public AlarmHandler(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(RECEIVER_ACTION);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void setNextAlarm() {
        Calendar alarmTime = getNextAlarmTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
        }
    }

    public void removeAlarm() {
        alarmManager.cancel(alarmIntent);
    }

    @DebugLog
    private Calendar getNextAlarmTime() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
        Calendar nextTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);

        double currentHour = currentTime.get(Calendar.HOUR_OF_DAY) + currentTime.get(Calendar.MINUTE) / 60D;

        final double startDay = 0D;
        final double endDay = 24D;
        double[] updateHour = new double[Previsione.UPDATE_TIME_COUNT];
        for (int i = 0; i < updateHour.length; i++) {
            updateHour[i] = Previsione.UPDATE_TIMES[i].getHours() + Previsione.UPDATE_TIMES[i].getMinutes() / 60D;
        }
        Arrays.sort(updateHour);

        if (currentHour >= startDay && currentHour < updateHour[0]) {
            nextTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[0]));
            nextTime.set(Calendar.MINUTE, getFrac(updateHour[0]));
            nextTime.set(Calendar.SECOND, 0);
            nextTime.set(Calendar.MILLISECOND, 0);
        } else if (currentHour >= updateHour[updateHour.length - 1] && currentHour < endDay) {
            nextTime.add(Calendar.DAY_OF_YEAR, 1);
            nextTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[0]));
            nextTime.set(Calendar.MINUTE, getFrac(updateHour[0]));
            nextTime.set(Calendar.SECOND, 0);
            nextTime.set(Calendar.MILLISECOND, 0);
        } else {
            for (int i = 1; i < updateHour.length; i++) {
                if (currentHour >= updateHour[i - 1] && currentHour < updateHour[i]) {
                    nextTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[i]));
                    nextTime.set(Calendar.MINUTE, getFrac(updateHour[i]));
                    nextTime.set(Calendar.SECOND, 0);
                    nextTime.set(Calendar.MILLISECOND, 0);
                    break;
                }
            }
        }

        return nextTime;
    }

    private int getInt(double d) {
        return (int) d;
    }

    private int getFrac(double d) {
        return (int) ((d - getInt(d)) * 100D / 60D);
    }

}
