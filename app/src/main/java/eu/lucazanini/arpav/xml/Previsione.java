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

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class Previsione {

    public final static OrarioAggiornamento[] AGGIORNAMENTI = new OrarioAggiornamento[3];

    public final static String TAG_DATA_EMISSIONE = "data_emissione";
    public final static String TAG_DATA_AGGIORNAMENTO = "data_aggiornamento";
    public final static String TAG_BOLLETTINO = "bollettino";

    public final static String ATTR_DATA = "date";

    private String dataEmissione, dataAggiornamento;
    private Bollettino meteoVeneto;
    private Calendar releaseDate, updateDate;

    public Previsione() {
        AGGIORNAMENTI[0] = new OrarioAggiornamento("13:00");
        AGGIORNAMENTI[1] = new OrarioAggiornamento("16:00");
        AGGIORNAMENTI[2] = new OrarioAggiornamento("09:00");
    }

    @DebugLog
    public void parse(BufferedInputStream bis) {

        final String ns = null;
//        Previsione previsione = null;
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

            // get event type
            int eventType = parser.getEventType();
            // process tag while not reaching the end of document

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // at start of document: START_DOCUMENT
                    case XmlPullParser.START_DOCUMENT:
//                        previsione = new Previsione();
                        break;

                    // at start of a tag: START_TAG
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

                // jump to next event
                eventType = parser.next();
            }

            // exception stuffs
        } catch (XmlPullParserException e) {
            Timber.e("error parsing xml %s", e.toString());
//            previsione = null;
        } catch (IOException e) {
            Timber.e("error parsing xml %s", e.toString());
//            previsione = null;
//        } finally {
//            return previsione;
        }

    }

    @DebugLog
    public boolean isUpdate() {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
        Calendar lastTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);

        double currentHour = currentTime.get(Calendar.HOUR_OF_DAY) + currentTime.get(Calendar.MINUTE) / 60D;

        Timber.d("current hour %s", currentHour);

        if (currentHour >= 0 && currentHour < 9) {
            lastTime.add(Calendar.DAY_OF_YEAR, -1);
            lastTime.set(Calendar.HOUR_OF_DAY, AGGIORNAMENTI[1].getHours());
            lastTime.set(Calendar.MINUTE, AGGIORNAMENTI[1].getMinutes());
            lastTime.set(Calendar.SECOND, AGGIORNAMENTI[1].getSeconds());
            lastTime.set(Calendar.MILLISECOND, AGGIORNAMENTI[1].getMilliSeconds());
        } else if (currentHour >= 9 && currentHour < 13) {
            lastTime.set(Calendar.HOUR_OF_DAY, AGGIORNAMENTI[2].getHours());
            lastTime.set(Calendar.MINUTE, AGGIORNAMENTI[2].getMinutes());
            lastTime.set(Calendar.SECOND, AGGIORNAMENTI[2].getSeconds());
            lastTime.set(Calendar.MILLISECOND, AGGIORNAMENTI[2].getMilliSeconds());
        } else if (currentHour >= 13 && currentHour < 16) {
            lastTime.set(Calendar.HOUR_OF_DAY, AGGIORNAMENTI[0].getHours());
            lastTime.set(Calendar.MINUTE, AGGIORNAMENTI[0].getMinutes());
            lastTime.set(Calendar.SECOND, AGGIORNAMENTI[0].getSeconds());
            lastTime.set(Calendar.MILLISECOND, AGGIORNAMENTI[0].getMilliSeconds());
        } else if (currentHour >= 16 && currentHour < 24) {
            lastTime.set(Calendar.HOUR_OF_DAY, AGGIORNAMENTI[1].getHours());
            lastTime.set(Calendar.MINUTE, AGGIORNAMENTI[1].getMinutes());
            lastTime.set(Calendar.SECOND, AGGIORNAMENTI[1].getSeconds());
            lastTime.set(Calendar.MILLISECOND, AGGIORNAMENTI[1].getMilliSeconds());
        } else {
            throw new ArrayIndexOutOfBoundsException("hour time not in range 0-24");
        }

        lastTime.getTimeInMillis();

        return getUpdateDate().compareTo(lastTime) >= 0;
    }

    public String getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(String dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;

        try {
//            updateDate = null;
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
        Timber.d("data aggiornamento %s", updateDate);
    }

    public String getDataEmissione() {
        return dataEmissione;
    }

    public void setDataEmissione(String dataEmissione) {
        this.dataEmissione = dataEmissione;

        try {
//            releaseDate = null;
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy 'alle' HH:mm");

            Date date = df.parse(dataEmissione);

            releaseDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
            releaseDate.setTime(date);
            releaseDate.getTimeInMillis();
        } catch (ParseException e) {
            Timber.e(e.toString());
            releaseDate = null;
        }
        Timber.d("data emissione %s", releaseDate);
    }

    public void setMeteoVeneto() {
        meteoVeneto = new Bollettino();
    }

    public Bollettino newMeteoVeneto() {
        meteoVeneto = new Bollettino();
        return meteoVeneto;
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

    public class OrarioAggiornamento {

        private Calendar time;

        public OrarioAggiornamento(String time) {
            try {
                DateFormat df = new SimpleDateFormat("HH:mm");
                Date date = df.parse(time);
                this.time = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"), Locale.ITALY);
                this.time.setTime(date);
            } catch (ParseException e) {
                Timber.e(e.toString());
            }
        }

        public int getHours() {
            int hours = time.get(Calendar.HOUR_OF_DAY);
            return hours;
        }

        public int getMinutes() {
            int minutes = time.get(Calendar.MINUTE);
            return minutes;
        }

        public int getSeconds() {
            return 0;
        }

        public int getMilliSeconds() {
            return 0;
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

            @DebugLog
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

