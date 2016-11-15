package eu.lucazanini.arpav.task;

import android.app.IntentService;
import android.content.Intent;

public class ReportService extends IntentService {

    private final static String TAG = "Download Service";

    private static final String URL = "http://www.arpa.veneto.it/previsioni/it/xml/bollettino_utenti.xml";
    private static final String PREVISIONE_IT = "previsione_it.xml";

    public ReportService() {
        super(TAG);
    }

    public ReportService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ReportTask reportTask = new ReportTask(this);
        reportTask.doTask(URL, PREVISIONE_IT);
    }
}
