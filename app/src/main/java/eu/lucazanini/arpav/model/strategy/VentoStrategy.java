package eu.lucazanini.arpav.model.strategy;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 17/02/17.
 */

public class VentoStrategy implements WindStrategy {

    private String vento;

    public VentoStrategy(){};

    public VentoStrategy(String vento){
        this.vento=vento;
    }

    @Override
    public String getVento() {
        return vento;
    }

    @Override
    public void setVento(String vento) {
        this.vento=vento;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<VentoStrategy> CREATOR
            = new Parcelable.Creator<VentoStrategy>() {
        public VentoStrategy createFromParcel(Parcel in) {
            return new VentoStrategy(in);
        }

        public VentoStrategy[] newArray(int size) {
            return new VentoStrategy[size];
        }
    };

    private VentoStrategy(Parcel in) {
        vento = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vento);
    }
}
