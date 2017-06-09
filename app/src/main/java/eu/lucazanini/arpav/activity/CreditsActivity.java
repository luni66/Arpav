package eu.lucazanini.arpav.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import eu.lucazanini.arpav.R;

public class CreditsActivity extends AppCompatActivity {

    private final static String DEVELOPER_TITLE = "Sviluppatore";
    private final static String DEVELOPER_BODY = "Meteo Veneto è sviluppata da <a href=\"http://www.lucazanini.eu\">Luca Zanini</a>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        setTextViewContent(R.id.developerTitle, DEVELOPER_TITLE);
        setTextViewContent(R.id.developerBody, DEVELOPER_BODY, true);
    }

    private void setTextViewContent(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }

    private void setTextViewContent(int id, String text, boolean link) {
        if (link) {
            TextView tv = (TextView) findViewById(id);
            tv.setText(Html.fromHtml(text));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setLinksClickable(true);
        } else {
            setTextViewContent(id, text);
        }
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, CreditsActivity.class);
        return intent;
    }
}
