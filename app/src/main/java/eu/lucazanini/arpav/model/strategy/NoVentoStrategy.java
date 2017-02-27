package eu.lucazanini.arpav.model.strategy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public class NoVentoStrategy implements WindStrategy {

    public NoVentoStrategy(){};

    @Override
    public String getVento() {
        return null;
    }

    @Override
    public void setVento(String vento) {}

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<NoVentoStrategy> CREATOR
            = new Parcelable.Creator<NoVentoStrategy>() {
        public NoVentoStrategy createFromParcel(Parcel in) {
            return new NoVentoStrategy(in);
        }

        public NoVentoStrategy[] newArray(int size) {
            return new NoVentoStrategy[size];
        }
    };

    private NoVentoStrategy(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
