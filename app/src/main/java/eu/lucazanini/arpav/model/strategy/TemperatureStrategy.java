package eu.lucazanini.arpav.model.strategy;

import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public interface TemperatureStrategy extends Parcelable {

    public final static String TEMPERATURA = "Temperatura";
    public final static String TEMPERATURA_1500 = "Temperatura 1500m";
    public final static String TEMPERATURA_2000 = "Temperatura 2000m";
    public final static String TEMPERATURA_3000 = "Temperatura 3000m";

    String getTemperatura();

    void setTemperatura(String temperatura);

    String getLabel();

    void setLabel(String label);

}
