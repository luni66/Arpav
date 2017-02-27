package eu.lucazanini.arpav.model.strategy;

import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public interface WindStrategy extends Parcelable {

    String VENTO = "Vento";

    String getVento();

    void setVento(String vento);

}
