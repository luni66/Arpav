package eu.lucazanini.arpav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;

public class CreditsActivity extends AppCompatActivity {

    protected @BindView(R.id.developerTitle) TextView tvDeveloperTitle;
    protected @BindView(R.id.developerBody) TextView tvDeveloperBody;
    protected @BindString(R.string.developer_title) String developerTitle;
    protected @BindString(R.string.developer_body) String developerBody;
    protected @BindString(R.string.developer_site) String developerSite;
    protected @BindString(R.string.developer_name) String developerName;
    protected @BindView(R.id.toolbar) Toolbar toolbar;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, CreditsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDeveloperTitle.setText(developerTitle);

        SpannableString styledString = new SpannableString(developerBody);
        int start = developerBody.indexOf(developerName);
        int end = start + developerName.length();
        styledString.setSpan(new URLSpan(developerSite), start, end, 0);
        tvDeveloperBody.setMovementMethod(LinkMovementMethod.getInstance());
        tvDeveloperBody.setText(styledString);
    }
}
