package eu.lucazanini.arpav.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import eu.lucazanini.arpav.preference.Preferences;
import timber.log.Timber;

/**
 * Java class associated to the bulletin.
 * the xml file contains 18 Meteogrammi and 3 bollettini
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

    public final static String TAG_DATA_EMISSIONE = "data_emissione";
    public final static String TAG_DATA_AGGIORNAMENTO = "data_aggiornamento";
    public final static String TAG_BOLLETTINO = "bollettino";
    public final static String TAG_METEOGRAMMA = "meteogramma";
    public final static String ATTR_DATA = "date";

    public final static UpdateTime[] UPDATE_TIMES = new UpdateTime[3];
    public final static String RELEASE_TIME = "13:00";
    public final static String FIRST_UPDATE_TIME = "16:00";
    public final static String SECOND_UPDATE_TIME = "09:00";
    public final static int METEOGRAMMI_NUMBER = 18;
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

    static {
        UPDATE_TIMES[0] = new UpdateTime(RELEASE_TIME);
        UPDATE_TIMES[1] = new UpdateTime(FIRST_UPDATE_TIME);
        UPDATE_TIMES[2] = new UpdateTime(SECOND_UPDATE_TIME);
    }

    private final String url;
    private final String uri;
    private final boolean isTest;
    private String dataEmissione, dataAggiornamento;
    private Bollettino meteoVeneto;
    private Calendar releaseDate, updateDate;
    private Language language;
    private Meteogramma[] meteogramma = new Meteogramma[METEOGRAMMI_NUMBER];

    public Previsione(Language language, String text) {
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
        isTest = false;
        parse(text);
    }

    public Previsione(String url, String text) {
        this.url = url;
        switch (url) {
            case URL_EN:
                uri = URI_EN;
                language = Language.EN;
                break;
            case URL_FR:
                uri = URI_FR;
                language = Language.FR;
                break;
            case URL_DE:
                uri = URI_DE;
                language = Language.DE;
                break;
            case URL_IT:
            default:
                uri = URI_IT;
                language = Language.IT;
        }
        isTest = false;
        parse(text);
    }

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

        isTest = in.readByte() != 0;
    }

    @Deprecated
    public Previsione() {
        this.language = Language.IT;
        uri = URI_IT;
        url = URL_IT;
        isTest = false;
    }

    @Deprecated
    public Previsione(Preferences preferences) {
        this.language = preferences.getDefaultLanguage();
        uri = URI_IT;
        url = URL_IT;
        isTest = false;
    }

    @Deprecated
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
        isTest = false;
    }

    public static String getUrl(Language language) {
        switch (language) {
            case EN:
                return URL_EN;
            case FR:
                return URL_FR;
            case DE:
                return URL_DE;
            case IT:
            default:
                return URL_IT;
        }
    }

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

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public boolean isTest() {
        return isTest;
    }

    public void parse(String text) {
        String tagName = null;
        Bollettino meteoVeneto = null;
        Bollettino.Giorno giorno = null;
        Meteogramma.Scadenza scadenza = null;
        Meteogramma meteogramma = null;
        boolean insideMeteoVeneto = false;
        boolean insideGiorno = false;
        String bollettinoId = null;
        String zoneId = null;
        String lastData=null;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(text));

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
                            zoneId = parser.getAttributeValue(null, Meteogramma.ATTR_ZONE_ID);
                            meteogramma = newMeteogramma(zoneId);
                            meteogramma.setName(parser.getAttributeValue(null, Meteogramma.ATTR_NOME));
                            lastData=null;
                        } else if (meteogramma != null && tagName.equalsIgnoreCase(Meteogramma.TAG_SCADENZA)) {
                            scadenza = meteogramma.newScadenza();
                            String data = parser.getAttributeValue(null, Meteogramma.Scadenza.ATTR_DATA);
                            if(data.endsWith(" ")) {
                                switch (language) {
                                    case EN:
                                        if(lastData==null || lastData.equals(data)){
                                            scadenza.setData(data+"afternoon");
                                        } else {
                                            scadenza.setData(data+"morning");
                                        }
                                        break;
                                    case FR:
                                        if(lastData==null || lastData.equals(data)){
                                            scadenza.setData(data+"après-midi");
                                        } else {
                                            scadenza.setData(data+"matin");
                                        }
                                        break;
                                    case DE:
                                        if(lastData==null || lastData.equals(data)){
                                            scadenza.setData(data+"nachmittag");
                                        } else {
                                            scadenza.setData(data+"morgen");
                                        }
                                        break;
                                    case IT:
                                    default:
                                        scadenza.setData(data);
                                }
                            } else {
                                scadenza.setData(data);
                            }
                            lastData = data;
                        } else if (meteogramma != null && tagName.equalsIgnoreCase(Meteogramma.Scadenza.TAG_PREVISIONE)) {
                            String title = parser.getAttributeValue(null, Meteogramma.Scadenza.ATTR_TITLE);
                            String value = parser.getAttributeValue(null, Meteogramma.Scadenza.ATTR_VALUE);
                            switch (title) {
                                case Meteogramma.Scadenza.SIMBOLO:
                                    scadenza.setSimbolo(value);
                                    break;
                                case Meteogramma.Scadenza.CIELO:
                                    scadenza.setCielo(value);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA:
                                    scadenza.setTemperatura2000(value);
                                    scadenza.setProperty(Meteogramma.Scadenza.TEMPERATURA, value);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA_1500:
                                    scadenza.setTemperatura2000(value);
                                    scadenza.setProperty(Meteogramma.Scadenza.TEMPERATURA_1500, value);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA_2000:
                                    scadenza.setTemperatura2000(value);
                                    scadenza.setProperty(Meteogramma.Scadenza.TEMPERATURA_2000, value);
                                    break;
                                case Meteogramma.Scadenza.TEMPERATURA_3000:
                                    scadenza.setTemperatura3000(value);
                                    scadenza.setProperty(Meteogramma.Scadenza.TEMPERATURA_3000, value);
                                    break;
                                case Meteogramma.Scadenza.PRECIPITAZIONI:
                                    scadenza.setPrecipitazioni(value);
                                    break;
                                case Meteogramma.Scadenza.PROBABILITA_PRECIPITAZIONE:
                                    scadenza.setProbabilitaPrecipitazione(value);
                                    break;
                                case Meteogramma.Scadenza.QUOTA_NEVE:
                                    scadenza.setQuotaNeve(value);
                                    break;
                                case Meteogramma.Scadenza.VENTO:
                                    scadenza.setProperty(Meteogramma.Scadenza.VENTO, value);
                                    break;
                                case Meteogramma.Scadenza.ATTENDIBILITA:
                                    scadenza.setAttendibilita(value);
                                    break;
                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = parser.getAttributeValue(null, Bollettino.ATTR_BOLLETTINO_ID);
                            if (bollettinoId.equalsIgnoreCase(Bollettino.METEO_VENETO)) {
                                insideMeteoVeneto = true;
                                meteoVeneto = newMeteoVeneto();
                                meteoVeneto.setBollettinoId(bollettinoId);
                                meteoVeneto.setNome(parser.getAttributeValue(null, Bollettino.ATTR_NOME));
                                meteoVeneto.setTitolo(parser.getAttributeValue(null, Bollettino.ATTR_TITOLO));
                            }
                        } else if (tagName.equalsIgnoreCase(Bollettino.TAG_GIORNO) && insideMeteoVeneto) {
                            insideGiorno = true;
                            giorno = meteoVeneto.newGiorno();
                            if (giorno != null) {
                                giorno.setData(parser.getAttributeValue(null, Bollettino.Giorno.ATTR_DATA));
                            }
                        } else if (tagName.equalsIgnoreCase(Bollettino.Giorno.TAG_IMMAGINE) && insideMeteoVeneto && giorno != null && insideGiorno) {
                            giorno.addImmagine(parser.getAttributeValue(null, Bollettino.Giorno.ATTR_IMMAGINE), parser.getAttributeValue(null, Bollettino.Giorno.ATTR_DIDASCALIA));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (insideMeteoVeneto) {
                            if (tagName.equalsIgnoreCase(Bollettino.TAG_EVOLUZIONE_GENERALE)) {
                                meteoVeneto.setEvoluzioneGenerale(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Bollettino.TAG_AVVISO)) {
                                meteoVeneto.setAvviso(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Bollettino.TAG_FENOMENI_PARTICOLARI)) {
                                meteoVeneto.setFenomeniParticolari(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Bollettino.Giorno.TAG_TESTO)) {
                                giorno.setTesto(parser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = null;
                            insideMeteoVeneto = false;
                        } else if (tagName.equalsIgnoreCase(Bollettino.TAG_GIORNO)) {
                            insideGiorno = false;
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_METEOGRAMMA)) {
                            meteogramma = null;
                        } else if (tagName.equalsIgnoreCase(Meteogramma.TAG_SCADENZA)) {
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

    public Calendar getReleaseDate() {
        return releaseDate;
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

    public Calendar getUpdateDate() {
        if (updateDate != null)
            return updateDate;
        else
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

    public String getData() {
        if (dataAggiornamento != null && dataAggiornamento.length() > 0)
            return dataAggiornamento;
        else
            return dataEmissione;
    }

    public Bollettino getMeteoVeneto() {
        return meteoVeneto;
    }

    public Meteogramma newMeteogramma(String zoneId) {
        for (int i = 0; i < meteogramma.length; i++) {
            if (meteogramma[i] == null) {
                meteogramma[i] = new Meteogramma(zoneId);
                return meteogramma[i];
            }
        }
        return null;
    }


    public Meteogramma[] getMeteogramma() {
        return meteogramma;
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

    public long getCacheExpiration() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
        Calendar nextTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);

        double currentHour = currentTime.get(Calendar.HOUR_OF_DAY) + currentTime.get(Calendar.MINUTE) / 60D;

        if (currentHour >= 0 && currentHour < 9) {
//            nextTime.add(Calendar.DAY_OF_YEAR, -1);
            nextTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[2].getHours() - 1);
            nextTime.set(Calendar.MINUTE, UPDATE_TIMES[2].getMinutes());
            nextTime.set(Calendar.SECOND, UPDATE_TIMES[2].getSeconds());
            nextTime.set(Calendar.MILLISECOND, UPDATE_TIMES[2].getMilliSeconds());
        } else if (currentHour >= 9 && currentHour < 13) {
            nextTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[0].getHours() - 1);
            nextTime.set(Calendar.MINUTE, UPDATE_TIMES[0].getMinutes());
            nextTime.set(Calendar.SECOND, UPDATE_TIMES[0].getSeconds());
            nextTime.set(Calendar.MILLISECOND, UPDATE_TIMES[0].getMilliSeconds());
        } else if (currentHour >= 13 && currentHour < 16) {
            nextTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[1].getHours() - 1);
            nextTime.set(Calendar.MINUTE, UPDATE_TIMES[1].getMinutes());
            nextTime.set(Calendar.SECOND, UPDATE_TIMES[1].getSeconds());
            nextTime.set(Calendar.MILLISECOND, UPDATE_TIMES[1].getMilliSeconds());
        } else if (currentHour >= 16 && currentHour < 24) {
            nextTime.add(Calendar.DAY_OF_YEAR, 1);
            nextTime.set(Calendar.HOUR_OF_DAY, UPDATE_TIMES[2].getHours() - 1);
            nextTime.set(Calendar.MINUTE, UPDATE_TIMES[2].getMinutes());
            nextTime.set(Calendar.SECOND, UPDATE_TIMES[2].getSeconds());
            nextTime.set(Calendar.MILLISECOND, UPDATE_TIMES[2].getMilliSeconds());
        } else {
            throw new ArrayIndexOutOfBoundsException("hour time not in range 0-24");
        }

        return nextTime.getTimeInMillis();
    }

    public enum Language {
        IT, EN, FR, DE
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

}

