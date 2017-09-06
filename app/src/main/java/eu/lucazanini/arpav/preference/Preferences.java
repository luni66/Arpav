package eu.lucazanini.arpav.preference;

import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Previsione;

public interface Preferences {

    Previsione.Language getDeviceLanguage();

    Previsione.Language getLanguage();

    String getLanguageCode();

    boolean isDefaultLanguage();

    Town getLocation();

    void saveLocation(Town town);

    boolean isAlertActive();

    boolean useGps();

    boolean isBulletinDisplayed();

}
