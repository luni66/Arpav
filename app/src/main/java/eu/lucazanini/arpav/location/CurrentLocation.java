package eu.lucazanini.arpav.location;


import java.util.Observable;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Contains the current location
 */
    public class CurrentLocation extends Observable {

    private static CurrentLocation instance = new CurrentLocation();
    private Town town;

    protected CurrentLocation() {}

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

    public boolean isDefined() {
        return town == null ? false : true;
    }

}
