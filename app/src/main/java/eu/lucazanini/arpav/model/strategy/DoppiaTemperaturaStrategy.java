package eu.lucazanini.arpav.model.strategy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public class DoppiaTemperaturaStrategy extends SingolaTemperaturaStrategy implements SecondTemperatureStrategy {

    protected String secondaTemperatura, secondaLabel;

    @Override
    public String getSecondTemperatura() {
        return secondaTemperatura;
    }

    @Override
    public void setSecondTemperatura(String secondaTemperatura) {
        this.secondaTemperatura = secondaTemperatura;
    }

    @Override
    public String getSecondLabel() {
        return secondaLabel;
    }

    @Override
    public void setSecondLabel(String secondaLabel) {
        this.secondaLabel = secondaLabel;
    }

    public static final Parcelable.Creator<DoppiaTemperaturaStrategy> CREATOR
            = new Parcelable.Creator<DoppiaTemperaturaStrategy>() {
        public DoppiaTemperaturaStrategy createFromParcel(Parcel in) {
            return new DoppiaTemperaturaStrategy(in);
        }

        public DoppiaTemperaturaStrategy[] newArray(int size) {
            return new DoppiaTemperaturaStrategy[size];
        }
    };

    private DoppiaTemperaturaStrategy(Parcel in) {
        super(in);
        secondaTemperatura = in.readString();
        secondaLabel = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(secondaTemperatura);
        dest.writeString(secondaLabel);
    }
}
