package eu.lucazanini.arpav.location;


import java.util.Observable;

/**
 * Created by luke on 14/02/17.
 */

public class CurrentLocation extends Observable {

    private static CurrentLocation instance = null;
    private Town town;
//    private rx.Observable<String> townName;

    protected CurrentLocation() {
        // Exists only to defeat instantiation.
    }

    public static CurrentLocation getInstance() {
        if (instance == null) {
            instance = new CurrentLocation();
        }
        return instance;
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
//        townName = rx.Observable.<String>just(town.getName());
        setChanged();
        notifyObservers(town.getName());
    }

    public boolean isDefined() {
        return town == null ? false : true;
    }

//    public rx.Observable<String> getTownName() {
//        return townName;
//    }
}
