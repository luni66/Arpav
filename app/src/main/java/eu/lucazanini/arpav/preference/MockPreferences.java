package eu.lucazanini.arpav.preference;

import android.content.Context;

import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Previsione;

/**
 * Created by luke on 31/12/16.
 */

public class MockPreferences implements Preferences {

    private Context context;

    public MockPreferences(Context context) {
        this.context = context;
    }

//    @Override
//    public Previsione.Language getDefaultLanguage() {
//        return Previsione.Language.IT;
//    }

    @Override
    public Previsione.Language getDeviceLanguage() {
        return null;
    }

    @Override
    public Previsione.Language getLanguage() {
        return null;
    }

    @Override
    public Town getLocation() {
        return null;
    }

    @Override
    public void saveLocation(Town town) {

    }

    @Override
    public boolean isDefaultLanguageSelected() {
        return false;
    }

    @Override
    public boolean isAlertActivated() {
        return false;
    }

    @Override
    public boolean useGps() {
        return false;
    }
}
