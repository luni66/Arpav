package eu.lucazanini.arpav.location;


import android.content.Context;

import java.util.Observable;

import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import timber.log.Timber;

/**
 * Contains the current location
 */
public class CurrentLocation extends Observable {

    private static CurrentLocation instance;
    private Town town;

    private CurrentLocation(Context context) {
        Preferences preferences = new UserPreferences(context);

        Town town = preferences.getLocation();
        if (town != null) {
            this.town = town;
        } else {
            Timber.d("LOCATION CREATED BUT NOT DEFINED");
        }
    }

    public static synchronized CurrentLocation getInstance(Context context) {
        if (instance == null) {
            instance = new CurrentLocation(context);
        }
        return instance;
    }

    public static CurrentLocation getInstance() {
        return instance;
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;

        setChanged();
        notifyObservers(town.getName());
    }

    public void setTown(String name, Context context) {
        TownList townList = TownList.getInstance(context);
        Town town = townList.getTown(name);
        setTown(town);
    }

    public boolean isDefined() {
        return town == null ? false : true;
    }

}
