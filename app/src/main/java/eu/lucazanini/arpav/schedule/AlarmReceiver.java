package eu.lucazanini.arpav.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.acra.ACRA;

import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.service.NotificationService;
import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG=this.getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);


        Timber.d("Class %s", TAG);

//        ACRA.getErrorReporter().clearCustomData();
//        ACRA.getErrorReporter().putCustomData("TAG", TAG);
//        ACRA.getErrorReporter().handleException(null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            String action = intent.getAction();

            PreferenceHelper preferences = new PreferenceHelper(context);

            if (preferences.isAlertActive() && action.equals("android.intent.action.BOOT_COMPLETED")) {
                AlarmHandler alarmHandler = new AlarmHandler(context);
                alarmHandler.setNextAlarm();
            } else if (action.startsWith(AlarmHandler.RECEIVER_ACTION)) {

//                ACRA.getErrorReporter().clearCustomData();
//                ACRA.getErrorReporter().putCustomData("TAG", TAG);
//                ACRA.getErrorReporter().putCustomData("MESSAGE", "AlarmReceiver_Sdk");
//                ACRA.getErrorReporter().handleException(null);

                AlarmHandler alarmHandler = new AlarmHandler(context);
                alarmHandler.setNextAlarm();
                context.startService(NotificationService.getIntent(context));
            }
        }
    }
}
