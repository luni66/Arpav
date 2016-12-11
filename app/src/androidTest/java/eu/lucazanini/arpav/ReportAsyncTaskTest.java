package eu.lucazanini.arpav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.lucazanini.arpav.task.ReportTask;
import eu.lucazanini.arpav.xml.Previsione;
import hugo.weaving.DebugLog;
import timber.log.Timber;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by luke on 09/11/16.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ReportAsyncTaskTest {

    Context appContext;

    @Test
    public void downloadXml() {
//        Context appContext = InstrumentationRegistry.getTargetContext();
        ReportTask reportTask = new ReportTask(appContext);

//        reportTask.test("bollettino_utenti.xml");

        reportTask.doTask(Previsione.Language.IT, "bollettino_utenti.xml");

//                            try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
    }

    @Before
    public void init() {
        appContext = InstrumentationRegistry.getTargetContext();
        IntentFilter intentFilter = new IntentFilter("eu.lucazanini.arpav.TEST");
        appContext.registerReceiver(testReceiver, intentFilter);
    }

    @After
    public void destroy(){
        appContext.unregisterReceiver(testReceiver);
    }

    private BroadcastReceiver testReceiver = new BroadcastReceiver() {

        @DebugLog
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("TEST");
            Previsione previsione = intent.getExtras().getParcelable("Previsione");

            assertThat(previsione.getDataEmissione(), is("19/10/2016 alle 13:00"));

        }
    };

}
