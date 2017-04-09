package eu.lucazanini.arpav.location;

public interface TownLocation {

    /**
     * Get the latitude, in degrees
     */
    double getLatitude();

    /**
     * Set the latitude, in degrees
     */
    void setLatitude(double latitude);

    /**
     * Get the longitude, in degrees
     */
    double getLongitude();

    /**
     * Set the longitude, in degrees
     */
    void setLongitude(double longitude);

    /**
     * Returns the approximate distance in meters between this location and the
     * given location
     */
    float distanceTo(TownLocation dest);

    /**
     * Get the name of the town of the location
     */
    String getName();

    /**
     * Get the number of the zone of the location
     */
    int getZone();

    /**
     * Set the number of the zone of the location
     */
    void setZone(int zone);

    /**
     * Get the name of the province of the location
     */
    Province getProvince();

    /**
     * Set the name of the province of the location
     */
    void setProvince(Province province);

    enum Province {
        BELLUNO, PADOVA, ROVIGO, TREVISO, VENEZIA, VERONA, VICENZA
    }

}
