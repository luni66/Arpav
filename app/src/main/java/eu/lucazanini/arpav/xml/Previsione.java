package eu.lucazanini.arpav.xml;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Xml;
import android.widget.Switch;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

/**
 * Java class associated to the bulletin
 */
public class Previsione implements Parcelable {

    public final static String URL_IT = "http://www.arpa.veneto.it/previsioni/it/xml/bollettino_utenti.xml";
    public final static String URL_EN = "http://www.arpa.veneto.it/previsioni/en/xml/bollettino_utenti.xml";
    public final static String URL_FR = "http://www.arpa.veneto.it/previsioni/fr/xml/bollettino_utenti.xml";
    public final static String URL_DE = "http://www.arpa.veneto.it/previsioni/de/xml/bollettino_utenti.xml";

    public final static String URI_IT = "previsione_it.xml";
    public final static String URI_EN = "previsione_en.xml";
    public final static String URI_FR = "previsione_fr.xml";
    public final static String URI_DE = "previsione_de.xml";

    private final String url;
    private final String uri;

    private final boolean isTest;

    public final static String TAG_DATA_EMISSIONE = "data_emissione";
    public final static String TAG_DATA_AGGIORNAMENTO = "data_aggiornamento";
    public final static String TAG_BOLLETTINO = "bollettino";
    public final static String TAG_METEOGRAMMA = "meteogramma";

    public final static String ATTR_DATA = "date";
    public final static UpdateTime[] UPDATE_TIMES = new UpdateTime[3];
    public final static String RELEASE_TIME = "13:00";
    public final static String FIRST_UPDATE_TIME = "16:00";
    public final static String SECOND_UPDATE_TIME = "09:00";

    static {
        UPDATE_TIMES[0] = new UpdateTime(RELEASE_TIME);
        UPDATE_TIMES[1] = new UpdateTime(FIRST_UPDATE_TIME);
        UPDATE_TIMES[2] = new UpdateTime(SECOND_UPDATE_TIME);
    }

    public enum Language {
        IT, EN, FR, DE
    }

    private String dataEmissione, dataAggiornamento;
    private Bollettino meteoVeneto;
    private Calendar releaseDate, updateDate;
    private Language language;
    private Meteogramma[] meteogramma = new Meteogramma[18];

    protected Previsione(Parcel in) {
        language = Language.valueOf(in.readString());
        switch (language) {
            case EN:
                uri = URI_EN;
                url = URL_EN;
                break;
            case FR:
                uri = URI_FR;
                url = URL_FR;
                break;
            case DE:
                uri = URI_DE;
                url = URL_DE;
                break;
            case IT:
            default:
                uri = URI_IT;
                url = URL_IT;
        }
        dataEmissione = in.readString();
        setReleaseDate(dataEmissione);
        dataAggiornamento = in.readString();
        setUpdateDate(dataAggiornamento);

        meteoVeneto = in.readParcelable(Bollettino.class.getClassLoader());

        isTest = in.readByte() !=0;
    }

    public static final Creator<Previsione> CREATOR = new Creator<Previsione>() {
        @Override
        public Previsione createFromParcel(Parcel in) {
            return new Previsione(in);
        }

        @Override
        public Previsione[] newArray(int size) {
            return new Previsione[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(language.name());
        parcel.writeString(dataEmissione);
        parcel.writeString(dataAggiornamento);

        parcel.writeParcelable(meteoVeneto, 0);

        parcel.writeByte((byte) (isTest ? 1 : 0));
    }

    @Deprecated
    public Previsione() {
        this.language = Language.IT;
        uri = URI_IT;
        url = URL_IT;
        isTest=false;
    }

    public Previsione(Language language, String assets){
        this.language = language;
        url = assets;
        switch (language) {
            case EN:
                uri = URI_EN;
                break;
            case FR:
                uri = URI_FR;
                break;
            case DE:
                uri = URI_DE;
                break;
            case IT:
            default:
                uri = URI_IT;
        }
        isTest=true;
    }

    public Previsione(Language language) {
        this.language = language;
        switch (language) {
            case EN:
                uri = URI_EN;
                url = URL_EN;
                break;
            case FR:
                uri = URI_FR;
                url = URL_FR;
                break;
            case DE:
                uri = URI_DE;
                url = URL_DE;
                break;
            case IT:
            default:
                uri = URI_IT;
                url = URL_IT;
        }
        isTest=false;
    }

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public boolean isTest() {
        return isTest;
    }

    /**
     * Parses the xml file passed as argument
     *
     * @param bis
     */
    public void parse(BufferedInputStream bis) {
        String tagName = null;
        Previsione.Bollettino meteoVeneto = null;
        Previsione.Bollettino.Giorno giorno = null;
        Previsione.Meteogramma.Scadenza scadenza = null;
        boolean insideMeteoVeneto = false;
        boolean insideGiorno = false;
        String bollettinoId = null;
        int zoneId = 0, scadenzaIndex = 0;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(bis, null);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase(Previsione.TAG_DATA_EMISSIONE)) {
                            setDataEmissione(parser.getAttributeValue(null, Previsione.ATTR_DATA));
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_DATA_AGGIORNAMENTO)) {
                            setDataAggiornamento(parser.getAttributeValue(null, Previsione.ATTR_DATA));
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_METEOGRAMMA)) {
                            zoneId = Integer.parseInt(parser.getAttributeValue(null, Meteogramma.ATTR_ZONE_ID));
                            meteogramma[zoneId - 1] = new Meteogramma(zoneId);
                            meteogramma[zoneId-1].setName(parser.getAttributeValue(null, Meteogramma.ATTR_NOME));
                        } else if (zoneId > 0 && tagName.equalsIgnoreCase(Meteogramma.TAG_SCADENZA)) {
                            scadenza = meteogramma[zoneId - 1].newScadenza();
                            scadenza.setData(parser.getAttributeValue(null, Meteogramma.Scadenza.ATTR_DATA));
                        } else if (zoneId > 0 && tagName.equalsIgnoreCase(Meteogramma.Scadenza.TAG_PREVISIONE)) {
                            String title = parser.getAttributeValue(null, Meteogramma.Scadenza.ATTR_TITLE);
                            switch (title){
                                case Meteogramma.Scadenza.SIMBOLO:
                                    scadenza.setSimbolo(title);
                                    break;
                                case Meteogramma.Scadenza.CIELO:
                                    scadenza.setCielo(title);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA_2000:
                                    scadenza.setTemperatura2000(title);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA_3000:
                                    scadenza.setTemperatura3000(title);
                                    break;
                                case Meteogramma.Scadenza.PRECIPITAZIONI:
                                    scadenza.setPrecipitazioni(title);
                                    break;
                                case Meteogramma.Scadenza.PROBABILITA_PRECIPITAZIONE:
                                    scadenza.setProbabilitaPrecipitazione(title);
                                    break;
                                case Meteogramma.Scadenza.QUOTA_NEVE:
                                    scadenza.setQuotaNeve(title);
                                    break;
                                case Meteogramma.Scadenza.ATTENDIBILITA:
                                    scadenza.setAttendibilita(title);
                                    break;
                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = parser.getAttributeValue(null, Previsione.Bollettino.ATTR_BOLLETTINO_ID);
                            if (bollettinoId.equalsIgnoreCase(Previsione.Bollettino.METEO_VENETO)) {
                                insideMeteoVeneto = true;
                                meteoVeneto = newMeteoVeneto();
                                meteoVeneto.setBollettinoId(bollettinoId);
                                meteoVeneto.setNome(parser.getAttributeValue(null, Previsione.Bollettino.ATTR_NOME));
                                meteoVeneto.setTitolo(parser.getAttributeValue(null, Previsione.Bollettino.ATTR_TITOLO));
                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_GIORNO) && insideMeteoVeneto) {
                            insideGiorno = true;
                            giorno = meteoVeneto.newGiorno();
                            if (giorno != null) {
                                giorno.setData(parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_DATA));
                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.Giorno.TAG_IMMAGINE) && insideMeteoVeneto && giorno != null && insideGiorno) {
                            giorno.addImmagine(parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_IMMAGINE), parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_DIDASCALIA));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (insideMeteoVeneto) {
                            if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_EVOLUZIONE_GENERALE)) {
                                meteoVeneto.setEvoluzioneGenerale(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_AVVISO)) {
                                meteoVeneto.setAvviso(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_FENOMENI_PARTICOLARI)) {
                                meteoVeneto.setFenomeniParticolari(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.Giorno.TAG_TESTO)) {
                                giorno.setTesto(parser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = null;
                            insideMeteoVeneto = false;
                        } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_GIORNO)) {
                            insideGiorno = false;
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_METEOGRAMMA)) {
                            zoneId = 0;
                            scadenzaIndex = 0;
                        } else if (tagName.equalsIgnoreCase(Meteogramma.TAG_SCADENZA)) {
                            scadenzaIndex++;
                        } else if (tagName.equalsIgnoreCase(Meteogramma.Scadenza.TAG_PREVISIONE)) {
                        }
                        tagName = "";
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Timber.e("error parsing xml %s", e.toString());
        } catch (IOException e) {
            Timber.e("error parsing xml %s", e.toString());
        }
    }


    /**
     * Checks if the bulletin is update
     *
     * @return returns true if is update, otherwise returns false
     */
    public boolean isUpdate() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
        Calendar lastTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);

        double currentHour = currentTime.get(Calendar.HOUR_OF_DAY) + currentTime.get(Calendar.MINUTE) / 60D;

        Timber.d("current hour %s", currentHour);

        if (currentHour >= 0 && currentHour < 9) {
            lastTime.add(Calendar.DAY_OF_YEAR, -1);
            lastTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[1].getHours());
            lastTime.set(Calendar.MINUTE, UPDATE_TIMES[1].getMinutes());
            lastTime.set(Calendar.SECOND, UPDATE_TIMES[1].getSeconds());
            lastTime.set(Calendar.MILLISECOND, UPDATE_TIMES[1].getMilliSeconds());
        } else if (currentHour >= 9 && currentHour < 13) {
            lastTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[2].getHours());
            lastTime.set(Calendar.MINUTE, UPDATE_TIMES[2].getMinutes());
            lastTime.set(Calendar.SECOND, UPDATE_TIMES[2].getSeconds());
            lastTime.set(Calendar.MILLISECOND, UPDATE_TIMES[2].getMilliSeconds());
        } else if (currentHour >= 13 && currentHour < 16) {
            lastTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[0].getHours());
            lastTime.set(Calendar.MINUTE, UPDATE_TIMES[0].getMinutes());
            lastTime.set(Calendar.SECOND, UPDATE_TIMES[0].getSeconds());
            lastTime.set(Calendar.MILLISECOND, UPDATE_TIMES[0].getMilliSeconds());
        } else if (currentHour >= 16 && currentHour < 24) {
            lastTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[1].getHours());
            lastTime.set(Calendar.MINUTE, UPDATE_TIMES[1].getMinutes());
            lastTime.set(Calendar.SECOND, UPDATE_TIMES[1].getSeconds());
            lastTime.set(Calendar.MILLISECOND, UPDATE_TIMES[1].getMilliSeconds());
        } else {
            throw new ArrayIndexOutOfBoundsException("hour time not in range 0-24");
        }

        lastTime.getTimeInMillis();

        Calendar fileTime = getUpdateDate();

        if (fileTime != null) {
            return getUpdateDate().compareTo(lastTime) >= 0;
        } else {
            return false;
        }
    }

    public Language getLanguage() {
        return language;
    }

    private void setReleaseDate(String data) {
        try {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy 'alle' HH:mm");

            Date date = df.parse(data);

            releaseDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            releaseDate.setTime(date);
            releaseDate.getTimeInMillis();
        } catch (ParseException e) {
            Timber.e(e.toString());
            releaseDate = null;
        }
    }

    public Calendar getReleaseDate() {
        return releaseDate;
    }

    private void setUpdateDate(String data) {
        try {
            DateFormat df = new SimpleDateFormat("dd/MM 'alle' HH.mm");

            Date date = df.parse(data);

            updateDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            updateDate.setTime(date);
            updateDate.set(Calendar.YEAR, releaseDate.get(Calendar.YEAR));
            if (updateDate.before(releaseDate)) {
                updateDate.set(Calendar.YEAR, releaseDate.get(Calendar.YEAR) + 1);
            }
            updateDate.getTimeInMillis();
        } catch (ParseException e) {
            Timber.e(e.toString());
            updateDate = null;
        }
    }

    public Calendar getUpdateDate() {
        if (updateDate != null)
            return updateDate;
        else
            return releaseDate;
    }

    public Bollettino getMeteoVeneto() {
        return meteoVeneto;
    }

    public String getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(String dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;

        setUpdateDate(dataAggiornamento);
    }

    public String getDataEmissione() {
        return dataEmissione;
    }

    public void setDataEmissione(String dataEmissione) {
        this.dataEmissione = dataEmissione;

        setReleaseDate(dataEmissione);
    }

    public void setMeteoVeneto() {
        meteoVeneto = new Bollettino();
    }

    public Bollettino newMeteoVeneto() {
        meteoVeneto = new Bollettino();
        return meteoVeneto;
    }

    public static class UpdateTime {

        private int hours, minutes, seconds, milliSeconds;

        public UpdateTime(String time) {
            String[] separated = time.split(":");
            hours = Integer.parseInt(separated[0]);
            minutes = Integer.parseInt(separated[1]);
            seconds = 0;
            milliSeconds = 0;
        }

        public int getHours() {
            return hours;
        }

        public int getMinutes() {
            return minutes;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getMilliSeconds() {
            return milliSeconds;
        }

    }

    //TODO verify static modifier added for parcelable
    public static class Bollettino implements Parcelable {
        public final static String ATTR_BOLLETTINO_ID = "bollettinoid";
        public final static String ATTR_NOME = "name";
        public final static String ATTR_TITOLO = "title";
        public final static String TAG_EVOLUZIONE_GENERALE = "evoluzionegenerale";
        public final static String TAG_AVVISO = "avviso";
        public final static String TAG_FENOMENI_PARTICOLARI = "fenomeniparticolari";
        public final static String TAG_GIORNO = "giorno";
        public final static int DAYS = 5;
        public final static String METEO_VENETO = "MV";
        private String bollettinoId, nome, titolo, evoluzioneGenerale, avviso, fenomeniParticolari;
        private Giorno[] giorni = new Giorno[DAYS];

        protected Bollettino(Parcel in) {
            bollettinoId = in.readString();
            nome = in.readString();
            titolo = in.readString();
            evoluzioneGenerale = in.readString();
            avviso = in.readString();
            fenomeniParticolari = in.readString();

            giorni = in.createTypedArray(Giorno.CREATOR);
        }

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

        @Override
        public int describeContents() {
            return 0;
        }

        public Bollettino() {
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
            return avviso;
        }

        public void setAvviso(String avviso) {
            this.avviso = avviso;
        }

        public String getFenomeniParticolari() {
            return fenomeniParticolari;
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
                        Timber.e("immagini out of index");
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

    public static class Meteogramma implements Parcelable{

        public final static String ATTR_ZONE_ID = "zoneid";
        public final static String ATTR_NOME = "name";
        public final static String TAG_SCADENZA = "scadenza";

        private int zoneId;
        private String name;
        private Scadenza[] giorni = new Scadenza[7];

        public Meteogramma(int zoneId) {
            this.zoneId = zoneId;
        }

        protected Meteogramma(Parcel in) {
            zoneId = in.readInt();
            name = in.readString();
            giorni = in.createTypedArray(Scadenza.CREATOR);
        }

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

        public int getZoneId() {
            return zoneId;
        }

        public void setZoneId(int zoneId) {
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
            parcel.writeInt(zoneId);
            parcel.writeString(name);
            parcel.writeTypedArray(giorni, i);
        }

        public static class Scadenza implements Parcelable{
            public final static String TAG_PREVISIONE = "previsione";
            public final static String ATTR_DATA = "data";
            public final static String ATTR_TITLE = "title";

            public final static String SIMBOLO = "Simbolo";
            public final static String CIELO = "Cielo";
            public final static String TEMPERATURA_2000 = "Temperatura 2000m";
            public final static String TEMPERATURA_3000 = "Temperatura 3000m";
            public final static String PRECIPITAZIONI = "Precipitazioni";
            public final static String PROBABILITA_PRECIPITAZIONE = "Probabilita' precipitazione";
            public final static String QUOTA_NEVE = "Quota neve";
            public final static String ATTENDIBILITA = "Attendibilita";

            private String data, simbolo, cielo, temperatura2000, temperatura3000, precipitazioni, probabilitaPrecipitazione, quotaNeve, attendibilita;

            public Scadenza(){}

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
            }

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

            public String getData() {
                return data;
            }

            public void setData(String data) {
                this.data = data;
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
            }
        }

    }

}

