package eu.lucazanini.arpav.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.BindString;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

}
