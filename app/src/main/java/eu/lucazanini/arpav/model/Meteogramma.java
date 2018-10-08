package eu.lucazanini.arpav.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Contains scadenza that it is the forecast for the 18 zones of Veneto
 */
public class Meteogramma implements Parcelable {

    public final static String ATTR_ZONE_ID = "zoneid";
    public final static String ATTR_NOME = "name";
    public final static String TAG_SCADENZA = "scadenza";
    public final static int SCADENZA_NUMBER = 7;
    public static final Creator<Meteogramma> CREATOR = new Creator<Meteogramma>() {
        @Override
        public Meteogramma createFromParcel(Parcel in) {
            return new Meteogramma(in);
        }

        @Override
        public Meteogramma[] newArray(int size) {
            return new Meteogramma[size];
        }
    };
    private String zoneId;
    private String name;
    private Scadenza[] giorni = new Scadenza[SCADENZA_NUMBER];

    public Meteogramma(String zoneId) {
        this.zoneId = zoneId;
    }

    protected Meteogramma(Parcel in) {
        zoneId = in.readString();
        name = in.readString();
        giorni = in.createTypedArray(Scadenza.CREATOR);
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scadenza newScadenza() {
        for (int i = 0; i < giorni.length; i++) {
            if (giorni[i] == null) {
                giorni[i] = new Scadenza();
                return giorni[i];
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(zoneId);
        parcel.writeString(name);
        parcel.writeTypedArray(giorni, i);
    }

    public Scadenza[] getScadenza() {
        return giorni;
    }

    public String[] getDateGiorni() {
        String[] dates = new String[SCADENZA_NUMBER];
        for (int i = 0; i < SCADENZA_NUMBER; i++) {
            dates[i] = giorni[i].getData();
        }
        return dates;
    }

    public static class Scadenza implements Parcelable {

        public final static String TAG_PREVISIONE = "previsione";
        public final static String ATTR_DATA = "data";
        public final static String ATTR_TITLE = "title";
        public final static String ATTR_VALUE = "value";
        public final static String SIMBOLO = "Simbolo";
        public final static String CIELO = "Cielo";
        public final static String TEMPERATURA = "Temperatura";
        public final static String TEMPERATURA_1500 = "Temperatura 1500m";
        public final static String TEMPERATURA_2000 = "Temperatura 2000m";
        public final static String TEMPERATURA_3000 = "Temperatura 3000m";
        public final static String PRECIPITAZIONI = "Precipitazioni";
        public final static String PROBABILITA_PRECIPITAZIONE = "Probabilita' precipitazione";
        public final static String QUOTA_NEVE = "Quota neve";
        public final static String VENTO = "Vento";
        public final static String ATTENDIBILITA = "Attendibilita'";
        public static final Creator<Scadenza> CREATOR = new Creator<Scadenza>() {
            @Override
            public Scadenza createFromParcel(Parcel in) {
                return new Scadenza(in);
            }

            @Override
            public Scadenza[] newArray(int size) {
                return new Scadenza[size];
            }
        };
        private String data, simbolo, cielo, temperatura2000, temperatura3000, precipitazioni,
                probabilitaPrecipitazione, quotaNeve, attendibilita;
        private Map<String, String> properties = new HashMap<>();

        public Scadenza() {
        }

        protected Scadenza(Parcel in) {
            data = in.readString();
            simbolo = in.readString();
            cielo = in.readString();
            temperatura2000 = in.readString();
            temperatura3000 = in.readString();
            precipitazioni = in.readString();
            probabilitaPrecipitazione = in.readString();
            quotaNeve = in.readString();
            attendibilita = in.readString();

            final int propertiesIdx = in.readInt();
            for (int i = 0; i < propertiesIdx; i++) {
                String key = in.readString();
                String value = in.readString();
                properties.put(key, value);
            }
        }

        public String getProperty(String key) {
            return properties.get(key);
        }

        public void setProperty(String key, String value) {
            properties.put(key, value);
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getShortDate() {
            Pattern p = Pattern.compile("\\w+[ ]+\\d+");
            Matcher m = p.matcher(data);
            if (m.find()) {
                return m.group();
            } else {
                return data;
            }
        }

        public String getSimbolo() {
            return simbolo;
        }

        public void setSimbolo(String simbolo) {
            this.simbolo = simbolo;
        }

        public String getCielo() {
            return cielo;
        }

        public void setCielo(String cielo) {
            this.cielo = cielo;
        }

        public String getTemperatura2000() {
            return temperatura2000;
        }

        public void setTemperatura2000(String temperatura2000) {
            this.temperatura2000 = temperatura2000;
        }

        public String getTemperatura3000() {
            return temperatura3000;
        }

        public void setTemperatura3000(String temperatura3000) {
            this.temperatura3000 = temperatura3000;
        }

        public String getPrecipitazioni() {
            return precipitazioni;
        }

        public void setPrecipitazioni(String precipitazioni) {
            this.precipitazioni = precipitazioni;
        }

        public String getProbabilitaPrecipitazione() {
            return probabilitaPrecipitazione;
        }

        public void setProbabilitaPrecipitazione(String probabilitaPrecipitazione) {
            this.probabilitaPrecipitazione = probabilitaPrecipitazione;
        }

        public String getQuotaNeve() {
            return quotaNeve;
        }

        public void setQuotaNeve(String quotaNeve) {
            this.quotaNeve = quotaNeve;
        }

        public String getAttendibilita() {
            return attendibilita;
        }

        public void setAttendibilita(String attendibilita) {
            this.attendibilita = attendibilita;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(data);
            parcel.writeString(simbolo);
            parcel.writeString(cielo);
            parcel.writeString(temperatura2000);
            parcel.writeString(temperatura3000);
            parcel.writeString(precipitazioni);
            parcel.writeString(probabilitaPrecipitazione);
            parcel.writeString(quotaNeve);
            parcel.writeString(attendibilita);

            int propertiesIdx = properties.size();
            parcel.writeInt(propertiesIdx);
            if (propertiesIdx > 0) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    parcel.writeString(entry.getKey());
                    parcel.writeString(entry.getValue());
                }
            }
        }
    }
}
