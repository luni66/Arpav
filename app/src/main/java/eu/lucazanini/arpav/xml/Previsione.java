package eu.lucazanini.arpav.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
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
public class Previsione {

    public final static String TAG_DATA_EMISSIONE = "data_emissione";
    public final static String TAG_DATA_AGGIORNAMENTO = "data_aggiornamento";
    public final static String TAG_BOLLETTINO = "bollettino";

    public final static String ATTR_DATA = "date";
    public static UpdateTime[] UPDATE_TIMES = new UpdateTime[3];
    public static String RELEASE_TIME = "13:00";
    public static String FIRST_UPDATE_TIME = "16:00";
    public static String SECOND_UPDATE_TIME = "09:00";

    static {
        UPDATE_TIMES[0] = new UpdateTime(RELEASE_TIME);
        UPDATE_TIMES[1] = new UpdateTime(FIRST_UPDATE_TIME);
        UPDATE_TIMES[2] = new UpdateTime(SECOND_UPDATE_TIME);
    }

    private String dataEmissione, dataAggiornamento;
    private Bollettino meteoVeneto;
    private Calendar releaseDate, updateDate;

    /**
     * Parses the xml file passed as argument
     *
     * @param bis
     */
    public void parse(BufferedInputStream bis) {
        String tagName = null;
        Previsione.Bollettino meteoVeneto = null;
        Previsione.Bollettino.Giorno giorno = null;
        boolean insideMeteoVeneto = false;
        boolean insideGiorno = false;
        String bollettinoId = null;

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

    public Calendar getReleaseDate() {
        return releaseDate;
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

        try {
            DateFormat df = new SimpleDateFormat("dd/MM 'alle' HH.mm");

            Date date = df.parse(dataAggiornamento);

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

    public String getDataEmissione() {
        return dataEmissione;
    }

    public void setDataEmissione(String dataEmissione) {
        this.dataEmissione = dataEmissione;

        try {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy 'alle' HH:mm");

            Date date = df.parse(dataEmissione);

            releaseDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            releaseDate.setTime(date);
            releaseDate.getTimeInMillis();
        } catch (ParseException e) {
            Timber.e(e.toString());
            releaseDate = null;
        }
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

    public class Bollettino {
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

        public class Giorno {

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

}

