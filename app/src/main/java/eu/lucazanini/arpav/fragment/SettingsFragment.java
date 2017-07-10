package eu.lucazanini.arpav.fragment;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.lucazanini.arpav.R;
import hugo.weaving.DebugLog;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String languageKey;
    private String defaultLanguage;
    private SharedPreferences sharedPref;
    private String[] languageEntries, languageValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Resources resources = getResources();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        languageKey = resources.getString(R.string.pref_language_key);
        defaultLanguage = resources.getString(R.string.pref_language_default);
        languageEntries = resources.getStringArray(R.array.pref_language_entries);
        languageValues = resources.getStringArray(R.array.pref_language_values);
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
