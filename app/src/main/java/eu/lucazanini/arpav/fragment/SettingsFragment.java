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

import butterknife.BindString;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String languageKey;
    private String defaultLanguage;
    private SharedPreferences sharedPref;
    private String[] languageEntries, languageValues;

//    public SettingsFragment() {
//        // Required empty public constructor
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
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
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @DebugLog
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(languageKey)) {
            Preference languagePref = findPreference(key);
            String languageValue = sharedPreferences.getString(key, defaultLanguage);
            // Set summary to be the user-description for the selected value
            languagePref.setSummary(getEntry(languageEntries, languageValues, languageValue));
        }
    }

    private String getValue(String[] entries, String[] values, String entry){
        int i=0;
        for (String e: entries) {
            if(entry.equals(e)){
                return values[i];
            }
            i++;
        }
        return null;
    }

    private String getEntry(String[] entries, String[] values, String value){
        int i=0;
        for (String v: values) {
            if(value.equals(v)){
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

    /*    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }*/

}
