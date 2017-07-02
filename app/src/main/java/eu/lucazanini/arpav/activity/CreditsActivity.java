package eu.lucazanini.arpav.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;

public class CreditsActivity extends AppCompatActivity {

//    private final static String developerTitle = "Sviluppatore";
//    private final static String developerBody = "Meteo Veneto è sviluppata da <a href=\"http://www.lucazanini.eu\">Luca Zanini</a>";

    protected @BindView(R.id.developerTitle) TextView tvDeveloperTitle;
    protected @BindView(R.id.developerBody) TextView tvDeveloperBody;
    protected @BindString(R.string.developer_title) String developerTitle;
    protected @BindString(R.string.developer_body) String developerBody;
    protected @BindString(R.string.developer_site) String developerSite;
    protected @BindString(R.string.developer_name) String developerName;
    protected @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDeveloperTitle.setText(developerTitle);
//        tvDeveloperBody.setText(developerBody);

        SpannableString styledString = new SpannableString(developerBody);
        int start = developerBody.indexOf(developerName);
        int end = start + developerName.length();
        styledString.setSpan(new URLSpan(developerSite), start, end, 0);
        tvDeveloperBody.setMovementMethod(LinkMovementMethod.getInstance());
        tvDeveloperBody.setText(styledString);

//        setTextViewContent(R.id.developerTitle, developerTitle);
//        setTextViewContent(R.id.developerBody, developerBody, true);
    }

//    private void setTextViewContent(int id, String text) {
//        TextView tv = (TextView) findViewById(id);
//        tv.setText(text);
//    }

//    private void setTextViewContent(int id, String text, boolean link) {
//        if (link) {
//            TextView tv = (TextView) findViewById(id);
//            tv.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
//            tv.setMovementMethod(LinkMovementMethod.getInstance());
//            tv.setLinksClickable(true);
//        } else {
//            setTextViewContent(id, text);
//        }
//    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, CreditsActivity.class);
        return intent;
    }
}
