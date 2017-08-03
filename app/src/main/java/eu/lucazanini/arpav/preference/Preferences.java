package eu.lucazanini.arpav.preference;

import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Previsione;

public interface Preferences {

    Previsione.Language getDeviceLanguage();

    Previsione.Language getLanguage();

    Town getLocation();

    void saveLocation(Town town);

    boolean isDefaultLanguageSelected();

    boolean isAlertActivated();

    boolean useGps();

}
