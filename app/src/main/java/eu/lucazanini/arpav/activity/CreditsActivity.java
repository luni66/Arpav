package eu.lucazanini.arpav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.fragment.CreditsFragment;
import eu.lucazanini.arpav.helper.LocaleHelper;

public class CreditsActivity extends AppCompatActivity {

    protected @BindView(R.id.toolbar) Toolbar toolbar;

    public static Intent getIntent(Context context) {
        return new Intent(context, CreditsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CreditsFragment()).commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
