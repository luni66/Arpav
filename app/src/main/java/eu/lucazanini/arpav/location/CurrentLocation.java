package eu.lucazanini.arpav.location;


import android.content.Context;

import java.util.Observable;

import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Contains the current location
 */
    public class CurrentLocation extends Observable {

    private static CurrentLocation instance;
    private Town town;

    private CurrentLocation(Context context) {
        Preferences preferences = new UserPreferences(context);

//            String townName = preferences.getLocation();
//        Timber.d("TOWN IN PREFERENCES %s", townName);
//            Town prefsTown = TownList.getInstance(context).getTown(townName);
        Town prefsTown = preferences.getLocation();
            if (prefsTown != null) {
                town = prefsTown;
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
