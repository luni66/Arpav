package eu.lucazanini.arpav;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.task.ReportTask;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
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
        Context appContext = InstrumentationRegistry.getTargetContext();
        ReportTask reportTask = new ReportTask(appContext);

//        reportTask.test("bollettino_utenti.xml");

//        Previsione previsione = reportTask.getPrevisione(Previsione.Language.IT, "test/bollettino_utenti.xml");

        Previsione previsione = reportTask.getPrevisione(Previsione.Language.EN, "test/bollettino_en.xml");

        assertThat(previsione.getDataEmissione(), containsString("alle"));
//        assertThat(previsione.getDataAggiornamento(), containsString("alle"));
        assertThat(previsione.getDataAggiornamento(), anyOf(is(nullValue()), containsString("alle")));

        Meteogramma[] meteogrammi = previsione.getMeteogramma();
        for (int i = 0; i < Previsione.MG_IDX; i++) {
            Meteogramma meteogramma = meteogrammi[i];
            assertThat(meteogramma, is(notNullValue()));

            Meteogramma.Scadenza[] scadenze = meteogramma.getScadenza();
            for (int j = 0; j < Meteogramma.SCADENZA_IDX; j++) {
                Meteogramma.Scadenza scadenza = scadenze[j];
                assertThat(scadenza, is(notNullValue()));
                assertThat(scadenza.getData(), not(isEmptyOrNullString()));
            }
        }

        if (previsione.getLanguage() == Previsione.Language.IT) {
            Bollettino bollettino = previsione.getMeteoVeneto();
            assertThat(bollettino, is(notNullValue()));
            Bollettino.Giorno[] giorni = bollettino.getGiorni();
            for (int i = 0; i < Bollettino.DAYS; i++) {
                Bollettino.Giorno giorno = giorni[i];
                assertThat(giorno, is(notNullValue()));
            }
        }



    }

//    @Before
//    public void init() {
//        appContext = InstrumentationRegistry.getTargetContext();
//        IntentFilter intentFilter = new IntentFilter("eu.lucazanini.arpav.TEST");
//        appContext.registerReceiver(testReceiver, intentFilter);
//    }
//
//    @After
//    public void destroy(){
//        appContext.unregisterReceiver(testReceiver);
//    }
//
//    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Timber.d("RECEIVED BROADCAST");
//
//            Previsione previsione = intent.getExtras().getParcelable("Previsione");
//
//            assertThat(previsione.getDataEmissione(), is("19/10/2016 alle 13:00"));
//
//            assertThat(previsione.getMeteogramma()[0], is(notNullValue()));
//
//            assertThat(previsione.getMeteogramma()[0].getScadenza()[0].getData(), is("19 ottobre pomeriggio"));
//1
//        }
//    };

}
