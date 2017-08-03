package eu.lucazanini.arpav.model;

import android.os.Parcel;
import android.os.Parcelable;

import timber.log.Timber;

/**
 * class associated to the Meteo Bulletin, one ot three bulletin in the xml file
 */
public class Bollettino implements Parcelable {
    public final static String ATTR_BOLLETTINO_ID = "bollettinoid";
    public final static String ATTR_NOME = "name";
    public final static String ATTR_TITOLO = "title";
    public final static String TAG_EVOLUZIONE_GENERALE = "evoluzionegenerale";
    public final static String TAG_AVVISO = "avviso";
    public final static String TAG_FENOMENI_PARTICOLARI = "fenomeniparticolari";
    public final static String TAG_GIORNO = "giorno";
    public final static int DAY_NUMBER = 5;
    public final static String METEO_VENETO = "MV";
    public static final Creator<Bollettino> CREATOR = new Creator<Bollettino>() {
        @Override
        public Bollettino createFromParcel(Parcel in) {
            return new Bollettino(in);
        }

        @Override
        public Bollettino[] newArray(int size) {
            return new Bollettino[size];
        }
    };
    private String bollettinoId, nome, titolo, evoluzioneGenerale, avviso, fenomeniParticolari;
    private Giorno[] giorni = new Giorno[DAY_NUMBER];

    protected Bollettino(Parcel in) {
        bollettinoId = in.readString();
        nome = in.readString();
        titolo = in.readString();
        evoluzioneGenerale = in.readString();
        avviso = in.readString();
        fenomeniParticolari = in.readString();
        giorni = in.createTypedArray(Giorno.CREATOR);
    }

    public Bollettino() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bollettinoId);
        parcel.writeString(nome);
        parcel.writeString(titolo);
        parcel.writeString(evoluzioneGenerale);
        parcel.writeString(avviso);
        parcel.writeString(fenomeniParticolari);
        parcel.writeTypedArray(giorni, 0);
    }

    public String getBollettinoId() {
        return bollettinoId;
    }

    public void setBollettinoId(String bollettinoId) {
        this.bollettinoId = bollettinoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getEvoluzioneGenerale() {
        return evoluzioneGenerale;
    }

    public void setEvoluzioneGenerale(String evoluzioneGenerale) {
        this.evoluzioneGenerale = evoluzioneGenerale;
    }

    public String getAvviso() {
        if (avviso == null) {
            return "";
        } else {
            return avviso;
        }
    }

    public void setAvviso(String avviso) {
        this.avviso = avviso;
    }

    public String getFenomeniParticolari() {
        if (fenomeniParticolari == null) {
            return null;
        } else {
            return fenomeniParticolari;
        }
    }

    public void setFenomeniParticolari(String fenomeniParticolari) {
        this.fenomeniParticolari = fenomeniParticolari;
    }

    public Giorno[] getGiorni() {
        return giorni;
    }

    public void setGiorno(Giorno[] giorni) {
        this.giorni = giorni;
    }

    public Giorno newGiorno() {
        for (int i = 0; i < giorni.length; i++) {
            if (giorni[i] == null) {
                giorni[i] = new Giorno();
                return giorni[i];
            }
        }
        return null;
    }

    public static class Giorno implements Parcelable {
        public final static String ATTR_DATA = "data";
        public final static String TAG_IMMAGINE = "img";
        public final static String ATTR_IMMAGINE = "src";
        public final static String ATTR_DIDASCALIA = "caption";
        public final static String TAG_TESTO = "text";
        public static final Creator<Giorno> CREATOR = new Creator<Giorno>() {
            @Override
            public Giorno createFromParcel(Parcel in) {
                return new Giorno(in);
            }

            @Override
            public Giorno[] newArray(int size) {
                return new Giorno[size];
            }
        };
        private String data, testo;
        private String[] sorgente, didascalia;
        private int imgIndex;

        public Giorno() {
            sorgente = new String[2];
            didascalia = new String[2];
        }

        protected Giorno(Parcel in) {
            data = in.readString();
            testo = in.readString();
            sorgente = in.createStringArray();
            didascalia = in.createStringArray();
            imgIndex = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(data);
            parcel.writeString(testo);
            parcel.writeStringArray(sorgente);
            parcel.writeStringArray(didascalia);
            parcel.writeInt(imgIndex);
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public void addImmagine(String sorgente, String didascalia) {
            switch (imgIndex) {
                case 0:
                    this.sorgente[0] = sorgente;
                    this.didascalia[0] = didascalia;
                    imgIndex++;
                    break;
                case 1:
                    this.sorgente[1] = sorgente;
                    this.didascalia[1] = didascalia;
                    imgIndex++;
                    break;
                default:
                    Timber.e("immagine out of index");
            }
        }


        public String getImageUrl(int index) {
            return sorgente[index];
        }

        public String getImageFile(int index) {
            String fileName = sorgente[index].substring(sorgente[index].lastIndexOf('/') + 1, sorgente[index].length());
            return fileName;
        }

        public String[] getSorgente() {
            return sorgente;
        }

        public void setSorgente(String[] sorgente) {
            this.sorgente = sorgente;
        }

        public String[] getDidascalia() {
            return didascalia;
        }

        public void setDidascalia(String[] didascalia) {
            this.didascalia = didascalia;
        }

        public String getTesto() {
            return testo;
        }

        public void setTesto(String testo) {
            this.testo = testo;
        }

        public int getImgIndex() {
            return imgIndex;
        }
    }
}
