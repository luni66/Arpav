package eu.lucazanini.arpav.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import org.acra.ACRA;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import eu.lucazanini.arpav.model.Previsione;

public class AlarmHandler {

    private final String TAG = this.getClass().getName();
    public final static String RECEIVER_ACTION = "eu.lucazanini.arpav.UPDATE_TIME";
    public final static int MINUTE_INTERVAL = 30;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Calendar startingTime;
    private Calendar nextAlarmTime;

    public AlarmHandler(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent = new Intent(context, AlarmReceiver_Sdk_22.class);
        } else {
            intent = new Intent(context, AlarmReceiver.class);
        }
        intent.setAction(RECEIVER_ACTION);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public AlarmHandler(Context context, Calendar startingTime) {
        this(context);
        this.startingTime = startingTime;
    }

    public void setNextAlarm() {
        setNextAlarmTime();

//        ACRA.getErrorReporter().clearCustomData();
//        ACRA.getErrorReporter().putCustomData("TAG", TAG);
//        ACRA.getErrorReporter().putCustomData("MESSAGE", "setting next alarm " + nextAlarmTime);
//        ACRA.getErrorReporter().handleException(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), alarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), alarmIntent);
        }
    }

    public void removeAlarm() {
        alarmManager.cancel(alarmIntent);
    }

    public Calendar getNextAlarmTime() {
        return nextAlarmTime;
    }

    public void setNextAlarmTime(Calendar nextAlarmTime) {
        this.nextAlarmTime = nextAlarmTime;
    }

    private void setNextAlarmTime() {
        if (startingTime == null) {
            startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
        }
        nextAlarmTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);

        double currentHour = startingTime.get(Calendar.HOUR_OF_DAY) + startingTime.get(Calendar.MINUTE) / 60D;

        final double startDay = 0D;
        final double endDay = 24D;
        double[] updateHour = new double[Previsione.UPDATE_TIME_COUNT];
        for (int i = 0; i < updateHour.length; i++) {
            updateHour[i] = Previsione.UPDATE_TIMES[i].getHours() + Previsione.UPDATE_TIMES[i].getMinutes() / 60D;
        }
        Arrays.sort(updateHour);

        int delay = getRandomDelay(MINUTE_INTERVAL);

        if (currentHour >= startDay && currentHour < updateHour[0]) {
            nextAlarmTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[0]));
            nextAlarmTime.set(Calendar.MINUTE, getFrac(updateHour[0]));
            nextAlarmTime.add(Calendar.MINUTE, delay);
            nextAlarmTime.set(Calendar.SECOND, 0);
            nextAlarmTime.set(Calendar.MILLISECOND, 0);
        } else if (currentHour >= updateHour[updateHour.length - 1] && currentHour < endDay) {
            nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
            nextAlarmTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[0]));
            nextAlarmTime.set(Calendar.MINUTE, getFrac(updateHour[0]));
            nextAlarmTime.add(Calendar.MINUTE, delay);
            nextAlarmTime.set(Calendar.SECOND, 0);
            nextAlarmTime.set(Calendar.MILLISECOND, 0);
        } else {
            for (int i = 1; i < updateHour.length; i++) {
                if (currentHour >= updateHour[i - 1] && currentHour < updateHour[i]) {
                    nextAlarmTime.set(Calendar.HOUR_OF_DAY, getInt(updateHour[i]));
                    nextAlarmTime.set(Calendar.MINUTE, getFrac(updateHour[i]));
                    nextAlarmTime.add(Calendar.MINUTE, delay);
                    nextAlarmTime.set(Calendar.SECOND, 0);
                    nextAlarmTime.set(Calendar.MILLISECOND, 0);
                    break;
                }
            }
        }

        // only in order to sync time with fields
        nextAlarmTime.getTime();
    }

    private int getRandomDelay(int interval) {
        Random random = new Random(SystemClock.uptimeMillis());
        return random.nextInt(interval + 1);
    }

    private int getInt(double d) {
        return (int) d;
    }

    private int getFrac(double d) {
        return (int) ((d - getInt(d)) * 100D / 60D);
    }

}
