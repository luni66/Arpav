package eu.lucazanini.arpav.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.activity.SearchableActivity;
import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.schedule.AlarmHandler;
import timber.log.Timber;

import static eu.lucazanini.arpav.activity.SearchableActivity.FAVOURITE_TOWN_CODE;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String languageKey, alertKey, townKey, bulletinKey;
    private String savedLocation;
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
        bulletinKey = resources.getString(R.string.pref_bulletin_key);
        reportFile = resources.getString(R.string.report_file);
        townKey = resources.getString(R.string.pref_town_key);
        savedLocation = resources.getString(R.string.current_location);
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
        } else if (key.equals(bulletinKey)) {
            boolean isBulletinDisplayed = sharedPreferences.getBoolean(bulletinKey, false);

            if (isBulletinDisplayed) {
                Timber.d("BULLETIN ON");

            } else {
                Timber.d("BULLETIN OFF");
                TwoStatePreference alert = (TwoStatePreference) findPreference(alertKey);
                alert.setChecked(false);
            }

        } else if (key.equals(alertKey)) {
            boolean isAlertActive = sharedPreferences.getBoolean(alertKey, false);

            AlarmHandler alarmHandler = new AlarmHandler(getActivity().getApplicationContext());

            if (isAlertActive) {
                Timber.d("ALERT ON");
                boolean success = getActivity().deleteFile(reportFile);
                alarmHandler.setNextAlarm();
            } else {
                Timber.d("ALERT OFF");
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

        Preference languagePref = findPreference(languageKey);
        String languageValue = sharedPref.getString(languageKey, defaultLanguage);
        languagePref.setSummary(getEntry(languageEntries, languageValues, languageValue));

        Preference townPref = findPreference(townKey);
        String townName = sharedPref.getString(savedLocation, "");
        townPref.setSummary(townName);

        townPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = SearchableActivity.getIntent(getActivity());
                startActivityForResult(intent, FAVOURITE_TOWN_CODE);
                return true;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * This method run after the user selects a town in {@link SearchableActivity}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FAVOURITE_TOWN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String townName = data.getStringExtra(SearchableActivity.TOWN_NAME);
                CurrentLocation currentLocation = CurrentLocation.getInstance();
                currentLocation.setTown(townName, getActivity());

                Town town = TownList.getInstance(getActivity()).getTown(townName);

                PreferenceHelper preferences = new PreferenceHelper(getActivity());
                preferences.saveLocation(town);

                Preference townPref = findPreference(townKey);
                townPref.setSummary(townName);
            }
        }
    }
}
