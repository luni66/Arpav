package eu.lucazanini.arpav.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.Previsione;

public class PreferenceHelper {

    private Context context;
    private Resources resources;
    private SharedPreferences sharedPreferences;

    public PreferenceHelper(Context context) {
        this.context = context;
        this.resources = context.getResources();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isDefaultLanguage() {
        String defaultLanguage = resources.getString(R.string.pref_language_default);
        String languageValue = sharedPreferences.getString(resources.getString(R.string.pref_language_key), defaultLanguage);
        return languageValue.equals(defaultLanguage);
    }

    public boolean isAlertActive() {
        return sharedPreferences.getBoolean(resources.getString(R.string.pref_alert_key), false);
    }

    public boolean isBulletinDisplayed() {
        return sharedPreferences.getBoolean(resources.getString(R.string.pref_bulletin_key), true);
    }

    public boolean useGps() {
        return sharedPreferences.getBoolean(resources.getString(R.string.pref_gps_key), true);
    }

    public Previsione.Language getDeviceLanguage() {
        String languageValue = Locale.getDefault().getLanguage();
        switch (languageValue) {
            case "it":
                return Previsione.Language.IT;
            case "en":
                return Previsione.Language.EN;
            case "fr":
                return Previsione.Language.FR;
            case "de":
                return Previsione.Language.DE;
            default:
                return Previsione.Language.EN;
        }
    }

    public Previsione.Language getLanguage() {
        String defaultLanguage = resources.getString(R.string.pref_language_default);
        String languageValue = sharedPreferences.getString(resources.getString(R.string.pref_language_key), defaultLanguage);
        switch (languageValue) {
            case "en":
                return Previsione.Language.EN;
            case "fr":
                return Previsione.Language.FR;
            case "de":
                return Previsione.Language.DE;
            case "it":
                return Previsione.Language.IT;
            case "default":
            default:
                return getDeviceLanguage();
        }
    }

    public String getLanguageCode() {
        String defaultLanguage = resources.getString(R.string.pref_language_default);
        String languageValue = sharedPreferences.getString(resources.getString(R.string.pref_language_key), defaultLanguage);
        switch (languageValue) {
            case "en":
            case "fr":
            case "de":
            case "it":
                return languageValue;
            case "default":
            default:
                return "en";
        }
    }

    public Town getLocation() {
        String townName = sharedPreferences.getString(resources.getString(R.string.current_location), "");
        return TownList.getInstance(context).getTown(townName);
    }

    public void saveLocation(Town town) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(resources.getString(R.string.current_location), town.getName());
        editor.apply();
    }
}
