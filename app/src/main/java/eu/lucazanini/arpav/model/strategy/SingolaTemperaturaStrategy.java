package eu.lucazanini.arpav.model.strategy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public class SingolaTemperaturaStrategy implements TemperatureStrategy {

    protected String temperatura, label;

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<SingolaTemperaturaStrategy> CREATOR
            = new Parcelable.Creator<SingolaTemperaturaStrategy>() {
        public SingolaTemperaturaStrategy createFromParcel(Parcel in) {
            return new SingolaTemperaturaStrategy(in);
        }

        public SingolaTemperaturaStrategy[] newArray(int size) {
            return new SingolaTemperaturaStrategy[size];
        }
    };

    protected SingolaTemperaturaStrategy(Parcel in) {
        temperatura = in.readString();
        label = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(temperatura);
        dest.writeString(label);
    }

}
