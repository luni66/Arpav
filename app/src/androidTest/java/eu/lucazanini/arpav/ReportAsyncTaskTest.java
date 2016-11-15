package eu.lucazanini.arpav;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import eu.lucazanini.arpav.task.ReportTask;

/**
 * Created by luke on 09/11/16.
 */

@RunWith(AndroidJUnit4.class)
public class ReportAsyncTaskTest {

    private static final String URL = "http://www.arpa.veneto.it/previsioni/it/xml/bollettino_utenti.xml";
    private static final String PREVISIONE_IT = "previsione_it.xml";

    @Test
    public void downloadXml() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ReportTask reportTask = new ReportTask(appContext);

        reportTask.execute(URL, PREVISIONE_IT);
    }

}
