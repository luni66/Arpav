package eu.lucazanini.arpav.task;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class ReportService extends IntentService {

    private final static String TAG = "Download Service";

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
    protected void onHandleIntent(Intent intent) {
        // deprecated
/*        ReportTask reportTask = new ReportTask(this);
        //TODO custom language
        reportTask.doTask(Previsione.Language.IT);*/
Timber.d("************ onHandleIntent *************");
    }
}
