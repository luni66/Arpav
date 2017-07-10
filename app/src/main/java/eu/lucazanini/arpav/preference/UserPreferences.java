package eu.lucazanini.arpav.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.Previsione;

public class UserPreferences implements Preferences {

    private Context context;
    private Resources resources;
    private SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        this.context = context;
        this.resources = context.getResources();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isDefaultLanguageSelected() {
        String defaultLanguage = resources.getString(R.string.pref_language_default);
        String languageValue = sharedPreferences.getString(resources.getString(R.string.pref_language_key), defaultLanguage);
        if (languageValue.equals(defaultLanguage)) {
            return true;
        } else {
            return false;
        }
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

    public Town getLocation() {
        String townName = sharedPreferences.getString(resources.getString(R.string.current_location), "");
        Town town = TownList.getInstance(context).getTown(townName);
        return town;
    }

    @Override
    public void saveLocation(Town town) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(resources.getString(R.string.current_location), town.getName());
        editor.commit();
    }
}
