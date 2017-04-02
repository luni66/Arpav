package eu.lucazanini.arpav.location;


import java.util.Observable;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by luke on 14/02/17.
 */

//public class CurrentLocation {
    public class CurrentLocation extends Observable {

    private static CurrentLocation instance = new CurrentLocation();
    private Town town;

    protected CurrentLocation() {
        // Exists only to defeat instantiation.
    }

    public static CurrentLocation getInstance() {
//        if (instance == null) {
//            instance = new CurrentLocation();
//        }
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
