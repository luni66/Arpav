package eu.lucazanini.arpav;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.schedule.AlarmHandler2;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AlarmTest {

    @Test
    public void getNextAlarm() {

        final double oneMinute = 60000D;
        final int startDay = 0;
        final int endDay = 24;

        final int firstAlarm = 9;
        final int secondAlarm = 13;
        final int thirdAlarm = 16;

        for (int hour = startDay; hour < firstAlarm; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar correctTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                correctTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                correctTime.set(Calendar.MINUTE, 0);
                correctTime.set(Calendar.SECOND, 0);
                correctTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler2 alarmHandler = new AlarmHandler2(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertEquals("next alarm not correct", nextAlarmTime.getTimeInMillis(), correctTime.getTimeInMillis(), oneMinute);

            }
        }

        for (int hour = firstAlarm; hour < secondAlarm; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar correctTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                correctTime.set(Calendar.HOUR_OF_DAY, secondAlarm);
                correctTime.set(Calendar.MINUTE, 0);
                correctTime.set(Calendar.SECOND, 0);
                correctTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler2 alarmHandler = new AlarmHandler2(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertEquals("next alarm not correct", nextAlarmTime.getTimeInMillis(), correctTime.getTimeInMillis(), oneMinute);

            }
        }

        for (int hour = secondAlarm; hour < thirdAlarm; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar correctTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                correctTime.set(Calendar.HOUR_OF_DAY, thirdAlarm);
                correctTime.set(Calendar.MINUTE, 0);
                correctTime.set(Calendar.SECOND, 0);
                correctTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler2 alarmHandler = new AlarmHandler2(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertEquals("next alarm not correct", nextAlarmTime.getTimeInMillis(), correctTime.getTimeInMillis(), oneMinute);

            }
        }

        for (int hour = thirdAlarm; hour < endDay; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar correctTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                correctTime.add(Calendar.DAY_OF_YEAR, 1);
                correctTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                correctTime.set(Calendar.MINUTE, 0);
                correctTime.set(Calendar.SECOND, 0);
                correctTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler2 alarmHandler = new AlarmHandler2(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertEquals("next alarm not correct", nextAlarmTime.getTimeInMillis(), correctTime.getTimeInMillis(), oneMinute);

            }
        }

    }
}
