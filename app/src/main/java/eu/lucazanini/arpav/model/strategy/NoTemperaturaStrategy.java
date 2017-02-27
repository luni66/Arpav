package eu.lucazanini.arpav.model.strategy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public class NoTemperaturaStrategy implements TemperatureStrategy {

    @Override
    public String getTemperatura() {
        return null;
    }

    @Override
    public void setTemperatura(String temperatura) {

    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setLabel(String label) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<NoTemperaturaStrategy> CREATOR
            = new Parcelable.Creator<NoTemperaturaStrategy>() {
        public NoTemperaturaStrategy createFromParcel(Parcel in) {
            return new NoTemperaturaStrategy(in);
        }

        public NoTemperaturaStrategy[] newArray(int size) {
            return new NoTemperaturaStrategy[size];
        }
    };

    private NoTemperaturaStrategy(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
