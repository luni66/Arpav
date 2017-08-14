package eu.lucazanini.arpav.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.OutputStream;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.schedule.AlarmHandler;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String languageKey, alertKey;
    private String defaultLanguage;
    private SharedPreferences sharedPref;
    private String[] languageEntries, languageValues;
    private String reportFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_kitkat);
        }

        Resources resources = getResources();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        languageKey = resources.getString(R.string.pref_language_key);
        defaultLanguage = resources.getString(R.string.pref_language_default);
        languageEntries = resources.getStringArray(R.array.pref_language_entries);
        languageValues = resources.getStringArray(R.array.pref_language_values);
        alertKey = resources.getString(R.string.pref_alert_key);
        reportFile = resources.getString(R.string.report_file);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(languageKey)) {
            Preference languagePref = findPreference(key);
            String languageValue = sharedPreferences.getString(key, defaultLanguage);
            languagePref.setSummary(getEntry(languageEntries, languageValues, languageValue));
        } else if (key.equals(alertKey)) {
            boolean isAlertActivated = sharedPreferences.getBoolean(alertKey, false);
            Timber.d("Alarm is %s", isAlertActivated);
            AlarmHandler alarmHandler = new AlarmHandler(getActivity().getApplicationContext());
            if (isAlertActivated) {
                boolean success = getActivity().deleteFile(reportFile);
                Timber.d("deletion file %s is %s", reportFile, success);
                alarmHandler.setAlarm();
            } else {
                alarmHandler.removeAlarm();
            }
        }
    }

    private String getValue(String[] entries, String[] values, String entry) {
        int i = 0;
        for (String e : entries) {
            if (entry.equals(e)) {
                return values[i];
            }
            i++;
        }
        return null;
    }

    private String getEntry(String[] entries, String[] values, String value) {
        int i = 0;
        for (String v : values) {
            if (value.equals(v)) {
                return entries[i];
            }
            i++;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Preference connectionPref = findPreference(languageKey);
        String languageValue = sharedPref.getString(languageKey, defaultLanguage);
        connectionPref.setSummary(getEntry(languageEntries, languageValues, languageValue));

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
