package eu.lucazanini.arpav.preference;

import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Previsione;

/**
 * Created by luke on 31/12/16.
 */

public interface Preferences {

    Previsione.Language getDeviceLanguage();
    Previsione.Language getLanguage();
    Town getLocation();
    void saveLocation(Town town);
    boolean isDefaultLanguageSelected();

//    Previsione.Language getDefaultLanguage();

}
