package eu.lucazanini.arpav;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.schedule.AlarmHandler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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

                Calendar lowerCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                lowerCorrectTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                lowerCorrectTime.set(Calendar.MINUTE, 0);
                lowerCorrectTime.set(Calendar.SECOND, 0);
                lowerCorrectTime.set(Calendar.MILLISECOND, 0);

                Calendar upperCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                upperCorrectTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                upperCorrectTime.set(Calendar.MINUTE, AlarmHandler.MINUTE_INTERVAL);
                upperCorrectTime.set(Calendar.SECOND, 0);
                upperCorrectTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler alarmHandler = new AlarmHandler(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertTrue("next alarm out of range", nextAlarmTime.getTimeInMillis()>=lowerCorrectTime.getTimeInMillis() && nextAlarmTime.getTimeInMillis()<=upperCorrectTime.getTimeInMillis());

            }
        }

        for (int hour = firstAlarm; hour < secondAlarm; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar lowerCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                lowerCorrectTime.set(Calendar.HOUR_OF_DAY, secondAlarm);
                lowerCorrectTime.set(Calendar.MINUTE, 0);
                lowerCorrectTime.set(Calendar.SECOND, 0);
                lowerCorrectTime.set(Calendar.MILLISECOND, 0);

                Calendar upperCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                upperCorrectTime.set(Calendar.HOUR_OF_DAY, secondAlarm);
                upperCorrectTime.set(Calendar.MINUTE, AlarmHandler.MINUTE_INTERVAL);
                upperCorrectTime.set(Calendar.SECOND, 0);
                upperCorrectTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler alarmHandler = new AlarmHandler(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertTrue("next alarm out of range", nextAlarmTime.getTimeInMillis()>=lowerCorrectTime.getTimeInMillis() && nextAlarmTime.getTimeInMillis()<=upperCorrectTime.getTimeInMillis());

            }
        }

        for (int hour = secondAlarm; hour < thirdAlarm; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar lowerCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                lowerCorrectTime.set(Calendar.HOUR_OF_DAY, thirdAlarm);
                lowerCorrectTime.set(Calendar.MINUTE, 0);
                lowerCorrectTime.set(Calendar.SECOND, 0);
                lowerCorrectTime.set(Calendar.MILLISECOND, 0);

                Calendar upperCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                upperCorrectTime.set(Calendar.HOUR_OF_DAY, thirdAlarm);
                upperCorrectTime.set(Calendar.MINUTE, AlarmHandler.MINUTE_INTERVAL);
                upperCorrectTime.set(Calendar.SECOND, 0);
                upperCorrectTime.set(Calendar.MILLISECOND, 0);

                AlarmHandler alarmHandler = new AlarmHandler(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

//                assertEquals("next alarm not correct", nextAlarmTime.getTimeInMillis(), lowerCorrectTime.getTimeInMillis(), oneMinute);
                assertTrue("next alarm out of range", nextAlarmTime.getTimeInMillis()>=lowerCorrectTime.getTimeInMillis() && nextAlarmTime.getTimeInMillis()<=upperCorrectTime.getTimeInMillis());

            }
        }

        for (int hour = thirdAlarm; hour < endDay; hour++) {
            for (int minute = 0; minute < 60; minute+=15) {

                Calendar startingTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                startingTime.set(Calendar.HOUR_OF_DAY, hour);
                startingTime.set(Calendar.MINUTE, minute);

                Calendar lowerCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                lowerCorrectTime.add(Calendar.DAY_OF_YEAR, 1);
                lowerCorrectTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                lowerCorrectTime.set(Calendar.MINUTE, 0);
                lowerCorrectTime.set(Calendar.SECOND, 0);
                lowerCorrectTime.set(Calendar.MILLISECOND, 0);

                Calendar upperCorrectTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
                upperCorrectTime.add(Calendar.DAY_OF_YEAR, 1);
                upperCorrectTime.set(Calendar.HOUR_OF_DAY, firstAlarm);
                upperCorrectTime.set(Calendar.MINUTE, AlarmHandler.MINUTE_INTERVAL);
                upperCorrectTime.set(Calendar.SECOND, 0);
                upperCorrectTime.set(Calendar.MILLISECOND, 0);


                AlarmHandler alarmHandler = new AlarmHandler(InstrumentationRegistry.getTargetContext(), startingTime);

                alarmHandler.setNextAlarm();

                Calendar nextAlarmTime = alarmHandler.getNextAlarmTime();

                assertTrue("next alarm out of range " + nextAlarmTime.getTime() + " is not in ["
                        + lowerCorrectTime.getTime() + " - " + upperCorrectTime +"]", nextAlarmTime.getTimeInMillis()>=lowerCorrectTime.getTimeInMillis() && nextAlarmTime.getTimeInMillis()<=upperCorrectTime.getTimeInMillis());

            }
        }

    }
}
