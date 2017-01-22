package eu.lucazanini.arpav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.lucazanini.arpav.task.ReportTask;
import eu.lucazanini.arpav.model.Previsione;

import static android.widget.Toast.LENGTH_SHORT;

public class XmlActivity extends AppCompatActivity {

    private static final String URL = "http://www.arpa.veneto.it/previsioni/it/xml/bollettino_utenti.xml";
    private static final String PREVISIONE_IT = "previsione_it.xml";
    @BindView(R.id.button)
    Button button1;
    @BindString(R.string.connection_error)
    String connectionError;
    @BindString(R.string.xml_error)
    String xmlError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        ButterKnife.bind(this);

//        TestReceiver testReceiver = new TestReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(testReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("eu.lucazanini.arpav.TEST");
        registerReceiver(testReceiver, intentFilter);
    }

    @OnClick(R.id.button)
    public void loadXml() {
        if (isConnected()) {
            Toast.makeText(this, "Connected", LENGTH_SHORT).show();

            // AsyncTask subclass
//            new DownloadXmlTask().execute(URL);
            ReportTask reportTask = new ReportTask(this);
            reportTask.execute();

        } else {
            Toast.makeText(this, "Not connected", LENGTH_SHORT).show();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Previsione previsione = intent.getExtras().getParcelable("Previsione");
        }
    };

}
