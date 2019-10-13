package eu.lucazanini.arpav.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.activity.MainActivity;
import eu.lucazanini.arpav.helper.NotificationHelper;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.schedule.AlarmReceiver_Sdk_22;
import timber.log.Timber;

public class NotificationService_Oreo extends JobIntentService {

    private final static String TAG = NotificationService_Oreo.class.getName();
    private String reportFile, reportDate, reportAlert, reportPhenomena, alertTitle;
    private Intent alarmIntent;
    static final int JOB_ID = 1000; //Unique job ID.
    private NotificationHelper notificationHelper;

    public static void enqueueWork(Context context, Intent work) {
        Timber.d("called notification service in Oreo %s", TAG);
        enqueueWork(context, NotificationService_Oreo.class, JOB_ID, work);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, NotificationService_Oreo.class);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.d("started notification service in Oreo %s", TAG);

        Resources resources = getResources();

        alarmIntent = intent;
        reportFile = resources.getString(R.string.report_file);
        reportDate = resources.getString(R.string.report_date);
        reportAlert = resources.getString(R.string.report_alert);
        reportPhenomena = resources.getString(R.string.report_phenomena);
        alertTitle = resources.getString(R.string.alert_title);

        VolleySingleton volleyApp = VolleySingleton.getInstance(this);

        BulletinRequest serviceRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT),
                new NotificationService_Oreo.ServiceResponseListener(), new NotificationService_Oreo.ErrorListener(), TAG);
        volleyApp.addToRequestQueue(serviceRequest);
    }

//    private void releaseWakeLock() {
//        AlarmReceiver_Sdk_22.completeWakefulIntent(alarmIntent);
//    }

    private boolean isNewNotification(Map<String, String> currentData, Map<String, String> lastData) {
        String currentAlert, lastAlert, currentPhenomena, lastPhenomena;
        if (lastData == null) {
            return true;
        }

        if (currentData == null) {
            return false;
        }

        if (lastData.get(reportAlert) != null) {
            lastAlert = lastData.get(reportAlert);
        } else {
            lastAlert = "";
        }

        if (lastData.get(reportPhenomena) != null) {
            lastPhenomena = lastData.get(reportPhenomena);
        } else {
            lastPhenomena = "";
        }

        if (currentData.get(reportAlert) != null) {
            currentAlert = currentData.get(reportAlert);
        } else {
            currentAlert = "";
        }

        if (currentData.get(reportPhenomena) != null) {
            currentPhenomena = currentData.get(reportPhenomena);
        } else {
            currentPhenomena = "";
        }

        if (!currentAlert.equals("") && !currentAlert.equals(lastAlert)) {
            return true;
        }
        return !currentPhenomena.equals("") && !currentPhenomena.equals(lastPhenomena);

    }

    private Map<String, String> getLast() {
        Map<String, String> lastData = new HashMap<>();
        boolean found = false;
        try {
            String[] files = fileList();
            for (int i = 0; i < files.length; i++) {
                if (files[i].equals(reportFile)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                InputStream in = openFileInput(reportFile);
                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                reader.beginArray();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    String value = reader.nextString();
                    if (name != null && value != null) {
                        lastData.put(name, value);
                    }
                }
                reader.endObject();
                reader.endArray();
                reader.close();
            } else {
                return null;
            }
        } catch (FileNotFoundException e) {
            Timber.e(e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            Timber.e(e.getLocalizedMessage());
        } catch (IOException e) {
            Timber.e(e.getLocalizedMessage());
        }

        return lastData;
    }

    private synchronized void setLast(Map<String, String> lastData) {
        try {
            OutputStream out = openFileOutput(reportFile, Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.setIndent("    ");
            writer.beginArray();
            writer.beginObject();
            for (String name : lastData.keySet()) {
                String value = lastData.get(name);
                writer.name(name).value(value);
            }
            writer.endObject();
            writer.endArray();
            writer.close();
        } catch (FileNotFoundException e) {
            Timber.e(e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            Timber.e(e.getLocalizedMessage());
        } catch (IOException e) {
            Timber.e(e.getLocalizedMessage());
        }
    }

    private void createNotification(Map<String, String> data) {
        Timber.d("creating notification in %s", TAG);

        Notification.Builder notificationBuilder = null;

        notificationHelper = new NotificationHelper(this);

        notificationBuilder = notificationHelper.getNotification1("titolo",
                "descrizione");

        if (notificationBuilder != null) {
            notificationHelper.notify(102, notificationBuilder);
        }

/*        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //TODO http://thetechnocafe.com/how-to-use-workmanager-in-android/
        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "R.string.channel_name";
            String description = "R.string.channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String CHANNEL_ID = "2";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

//            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
//            mNotificationManager.createNotificationChannel(channel);

//            CharSequence name = "channel_name";
//            String description = "channel_description";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel("1", name, importance);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//            mNotificationManager = getSystemService(NotificationManager.class);
//            mNotificationManager.createNotificationChannel(channel);

        }

        String message = decodeHtml(data.get(reportAlert));
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(alertTitle)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int mNotificationId = 0;
        mNotificationManager.notify(mNotificationId, mBuilder.build());

        Timber.d("end notification in %s: %s", TAG, message);

//        mNotificationManager.notify(1, notification.build());*/
    }

    private String decodeHtml(String text) {
        if (text != null && text.length() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                return Html.fromHtml(text).toString();
            }
        } else {
            return "";
        }
    }

    private class ServiceResponseListener implements Response.Listener<Previsione> {
        @Override
        public void onResponse(Previsione response) {
            try {
                Timber.d("onResponse in %s", TAG);

                Map<String, String> lastData = getLast();

                // download the current alert
                Map<String, String> currentData = new HashMap<>();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                String date = df.format(response.getUpdateDate().getTime());
                currentData.put(reportDate, date);
                String reportAlertValue = response.getMeteoVeneto().getAvviso();
                if (!reportAlertValue.equals("")) {
                    currentData.put(reportAlert, reportAlertValue);
                }
                String reportPhenomenaValue = response.getMeteoVeneto().getFenomeniParticolari();
                if (!reportPhenomenaValue.equals("")) {
                    currentData.put(reportPhenomena, reportPhenomenaValue);
                }

                // TODO commentato per test
//                if (isNewNotification(currentData, lastData)) {
//                    createNotification(currentData);
//                }

                // commentare a fine test
                Map<String, String> testData = new HashMap<>();
                String testDate = df.format(response.getUpdateDate().getTime());
                testData.put(reportDate, testDate);
                String testReportAlertValue = "TEST";
                testData.put(reportAlert, testReportAlertValue);
                createNotification(testData);

                // save the current alert in the file
                setLast(currentData);
            } catch (Throwable t) {
                deleteFile(reportFile);
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    releaseWakeLock();
                }
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Timber.e(error.getLocalizedMessage());
        }
    }
}
