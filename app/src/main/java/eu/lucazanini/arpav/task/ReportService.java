package eu.lucazanini.arpav.task;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import java.util.HashMap;
import java.util.Map;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.activity.MainActivity;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.schedule.AlarmHandler;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportService extends IntentService {

    private final static String TAG = "Download Service";
    //    protected @BindString(R.string.report_file) String reportFile;
    private String reportFile, reportDate, reportAlert, alertTitle;

    public ReportService() {
        super(TAG);
    }

    public ReportService(String name) {
        super(name);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ReportService.class);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        ButterKnife.bind(getApplicationContext());
        Resources resources = getResources();
        reportFile = resources.getString(R.string.report_file);
        reportDate = resources.getString(R.string.report_date);
        reportAlert = resources.getString(R.string.report_alert);
        alertTitle = resources.getString(R.string.alert_title);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("********* service **********");
        VolleySingleton volleyApp = VolleySingleton.getInstance(this);

        BulletinRequest serviceRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT), new ServiceResponseListener(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
                AlarmHandler alarmHandler = new AlarmHandler(getApplicationContext());
                alarmHandler.setNextAlarm();
            }
        }, "SERVICE TAG");
        volleyApp.addToRequestQueue(serviceRequest);

    }

    private class ServiceResponseListener implements Response.Listener<Previsione> {
        @Override
        public void onResponse(Previsione response) {
            // read the file containg tha last alert
            Map<String, String> lastData = getLast();

            // download the current alert
            Map<String, String> currentData = new HashMap<>();
            currentData.put(reportDate, response.getUpdateDate().toString());
            currentData.put(reportAlert, response.getMeteoVeneto().getAvviso());

            if(isNewNotification(currentData, lastData)){
                createNotification(currentData);
            } else {
                createTestNotification("Test notification "+response.getUpdateDate().getTime());
            }

            // save the current alert in the file
            setLast(currentData);

            AlarmHandler alarmHandler = new AlarmHandler(getApplicationContext());
            alarmHandler.setNextAlarm();
        }
    }

    private boolean isNewNotification(Map<String, String> currentData, Map<String, String> lastData) {
        String currentAlert, lastAlert;
        if(lastData == null){
            return true;
        }
        if (currentData.get(reportAlert) != null) {
            currentAlert = currentData.get(reportAlert);
        } else {
            currentAlert = "";
        }
        if (lastData.get(reportAlert) != null) {
            lastAlert = lastData.get(reportAlert);
        } else {
            lastAlert = "";
        }

        if (currentAlert.equals("")) {
            return false;
        } else {
            if (currentAlert.equals(lastAlert)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void setLast(Map<String, String> lastData) {
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
            Timber.e(e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Timber.e(e.toString());
        }
    }

    private Map<String, String> getLast() {
        Map<String, String> lastData = new HashMap<>();
        try {
            InputStream in = openFileInput(reportFile);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                String value = reader.nextString();
                lastData.put(name, value);
            }
            reader.endObject();
            reader.endArray();
        } catch (FileNotFoundException e) {
            Timber.e(e.toString());
        } catch (UnsupportedEncodingException e) {
            Timber.e(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastData;
    }

    private void createNotification(Map<String, String> data) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(alertTitle)
                .setContentText(data.get(reportAlert));

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int mNotificationId = 0;
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    private void createTestNotification(String message){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(alertTitle)
                .setContentText(message);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int mNotificationId = 0;
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }
}
