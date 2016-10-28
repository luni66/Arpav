package eu.lucazanini.arpav;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.widget.Button;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.lucazanini.arpav.xml.Previsione;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class XmlActivity extends AppCompatActivity {

    private static final String URL = "http://www.arpa.veneto.it/previsioni/it/xml/bollettino_utenti.xml";
    private static final String PREVISIONE_IT = "previsione_it.xml";
    @BindView(R.id.button)
    Button button1;
    @BindString(R.string.connection_error)
    String connectionError;
    @BindString(R.string.xml_error)
    String xmlError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml);

        ButterKnife.bind(this);

        Timber.tag("LifeCycles");
        Timber.d("Activity Created");
    }

    @OnClick(R.id.button)
    public void loadXml() {
        Timber.d("A button with ID %s was clicked to say '%s'.", button1.getId(), button1.getText());

        if (isConnected()) {
            Toast.makeText(this, "Connected", LENGTH_SHORT).show();

            // AsyncTask subclass
            new DownloadXmlTask().execute(URL);

        } else {
            Toast.makeText(this, "Not connected", LENGTH_SHORT).show();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    @DebugLog
    private void save(BufferedInputStream stream) {
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(PREVISIONE_IT, Context.MODE_PRIVATE);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = stream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Timber.e("error saving file %s" + e.toString());
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    @DebugLog
    private BufferedInputStream load(String myurl) {
        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();
            Timber.d("The connection response is: %s", response);
            is = conn.getInputStream();

            bis = new BufferedInputStream(is);

        } catch (ProtocolException e) {
            Timber.e("error loading url %s" + e.toString());
            bis = null;
        } catch (MalformedURLException e) {
            Timber.e("error loading url %s" + e.toString());
            bis = null;
        } catch (IOException e) {
            Timber.e("error loading url %s" + e.toString());
        } finally {
            return bis;
        }
    }

    @DebugLog
    private Previsione parse(BufferedInputStream bis) {

        final String ns = null;
        Previsione previsione = null;
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
                        previsione = new Previsione();
                        break;

                    // at start of a tag: START_TAG
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();

                        if (tagName.equalsIgnoreCase(Previsione.TAG_DATA_EMISSIONE)) {
                            previsione.setDataEmissione(parser.getAttributeValue(null, Previsione.ATTR_DATA));
                            Timber.d("data emissione %s", previsione.getDataEmissione());
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_DATA_AGGIORNAMENTO)) {
                            previsione.setDataAggiornamento(parser.getAttributeValue(null, Previsione.ATTR_DATA));
                            Timber.d("data aggiornamento %s", previsione.getDataAggiornamento());
                        } else if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = parser.getAttributeValue(null, Previsione.Bollettino.ATTR_BOLLETTINO_ID);
                            if (bollettinoId.equalsIgnoreCase(Previsione.Bollettino.METEO_VENETO)) {
                                insideMeteoVeneto = true;
                                meteoVeneto = previsione.newMeteoVeneto();
                                Timber.d("creato bollettino meteo veneto");

                                meteoVeneto.setBollettinoId(bollettinoId);

                                meteoVeneto.setNome(parser.getAttributeValue(null, Previsione.Bollettino.ATTR_NOME));
                                meteoVeneto.setTitolo(parser.getAttributeValue(null, Previsione.Bollettino.ATTR_TITOLO));

                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_GIORNO) && insideMeteoVeneto) {
                            insideGiorno = true;
                            giorno = meteoVeneto.newGiorno();
                            if (giorno != null) {
                                giorno.setData(parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_DATA));
                                Timber.d("new Giorno %s", giorno.getData());
                            }
                        } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.Giorno.TAG_IMMAGINE) && insideMeteoVeneto && giorno != null && insideGiorno) {
                            giorno.setSorgente(parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_SORGENTE));
                            giorno.setDidascalia(parser.getAttributeValue(null, Previsione.Bollettino.Giorno.ATTR_DIDASCALIA));
                        }

                        break;

                    case XmlPullParser.TEXT:
                        if (insideMeteoVeneto) {
                            if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_EVOLUZIONE_GENERALE)) {
                                meteoVeneto.setEvoluzioneGenerale(parser.getText());
                                Timber.d("evoluzione generale %s", meteoVeneto.getEvoluzioneGenerale());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_AVVISO)) {
                                meteoVeneto.setAvviso(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.TAG_FENOMENI_PARTICOLARI)) {
                                meteoVeneto.setFenomeniParticolari(parser.getText());
                            } else if (tagName.equalsIgnoreCase(Previsione.Bollettino.Giorno.TAG_TESTO)) {
                                giorno.setTesto(parser.getText());
                                Timber.d("testo giorno %s %s", giorno.getData(), giorno.getTesto());
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase(Previsione.TAG_BOLLETTINO)) {
                            bollettinoId = null;
                            insideMeteoVeneto = false;
                            Timber.d("fine bollettino");
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
            Timber.e("error parsing xml %s" + e.toString());
            previsione = null;
        } catch (IOException e) {
            Timber.e("error parsing xml %s" + e.toString());
            previsione = null;
        } finally {
            return previsione;
        }

    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            BufferedInputStream bis = null;

            try {
                bis = load(urls[0]);
                if (bis != null) {
                    if (bis.markSupported()) {
                        bis.mark(150000);
                    }
                    Previsione previsione = parse(bis);
                    if (previsione != null) {
                        try {
                            bis.reset();
                        } catch (IOException e) {
                            bis = load(urls[0]);
                        } finally {
                            save(bis);
                        }
                    }
                }
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }
    }


}
