package eu.lucazanini.arpav;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class XmlActivity extends AppCompatActivity {

    @BindView(R.id.button) Button button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
/*        } else {
            Timber.plant(new CrashReportingTree());*/
        }

        Timber.tag("LifeCycles");
        Timber.d("Activity Created");
    }

    @OnClick(R.id.button)
    public void loadXml(){
        Timber.i("A button with ID %s was clicked to say '%s'.", button1.getId(), button1.getText());
        Toast.makeText(this, "test", LENGTH_SHORT).show();
    }

}
