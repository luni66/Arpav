package eu.lucazanini.arpav.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.activity.TitlesCallBack;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import timber.log.Timber;

public class MeteogrammaFragment extends Fragment implements Observer {
//public class MeteogrammaFragment extends Fragment {

    public final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sit amet arcu ultricies, porttitor libero in, fringilla erat. Proin sollicitudin in lacus eu pharetra. Duis ultricies justo gravida ligula lacinia. ";
    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGES = "pages";
    public final static int REQUEST_CODE = 0;
    protected @BindView(R.id.image_daySky) NetworkImageView imgDaySky;
    protected @BindView(R.id.text_sky) TextView tvDaySky;
    protected @BindView(R.id.text_temperature1) TextView tvTemperature1;
    protected @BindView(R.id.text_temperature2) TextView tvTemperature2;
    protected @BindView(R.id.text_rain) TextView tvRain;
    protected @BindView(R.id.text_snow) TextView tvSnow;
    protected @BindView(R.id.text_wind) TextView tvWind;
    protected @BindView(R.id.text_reliability) TextView tvReliability;
    protected @BindView(R.id.image_temperature1) ImageView imgTemperature1;
    protected @BindView(R.id.image_temperature2) ImageView imgTemperature2;
    protected @BindView(R.id.image_rain) ImageView imgRain;
    protected @BindView(R.id.image_snow) ImageView imgSnow;
    protected @BindView(R.id.image_wind) ImageView imgWind;
    protected @BindView(R.id.downloadProgressBar) ProgressBar progressBar;
    protected @BindView(R.id.text_date) TextView tvDate;
    protected @BindString(R.string.attendibilita) String AttendibilitaLabel;
    protected @BindString(R.string.aggiornato) String AggiornatoLabel;
    protected @BindView(R.id.text_description) TextView tvDescription;
    private Context context;
    private Unbinder unbinder;
    private String daySkyUrl, daySky, temperature1, temperature2, rain, probability, snow, wind, reliability, date, description;
    private String scadenzaDate;
    //    private SlideTitles slideTitles;
    private int pageNumber, pages;
    private CurrentLocation currentLocation;
    private TitlesCallBack titlesCallBack;
    private Preferences preferences;

    private VolleySingleton volleyApp;
    private ImageLoader mImageLoader;
    private Previsione.Language appLanguage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Bundle args = getArguments();
        pageNumber = args.getInt(PAGE_NUMBER);
        pages = args.getInt(PAGES);

        currentLocation = CurrentLocation.getInstance(context);
        currentLocation.addObserver(this);

        preferences = new UserPreferences(context);

        appLanguage = preferences.getLanguage();

        volleyApp = VolleySingleton.getInstance(getContext());
        mImageLoader = volleyApp.getImageLoader();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_meteogramma, container, false);

        unbinder = ButterKnife.bind(this, v);

        Town town = currentLocation.getTown();

        if (town == null) {
            town = preferences.getLocation();
            if (town != null) {
                currentLocation.setTown(town);
            }
        }

        loadData();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof TitlesCallBack) {
            titlesCallBack = (TitlesCallBack) activity;
        }
    }

    private void loadData() {

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (currentLocation.isDefined()) {
            BulletinRequest meteogrammaRequest = new BulletinRequest(Previsione.getUrl(appLanguage), new MeteogrammaResponseListener(), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Timber.e(error);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(meteogrammaRequest);
        }

        if (appLanguage != Previsione.Language.IT) {
            BulletinRequest BollettinoRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT), new BollettinoResponseListener(), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Timber.e(error);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(BollettinoRequest);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentLocation.deleteObserver(this);

        String tag = Integer.toString(pageNumber);
        final VolleySingleton volleyApp = VolleySingleton.getInstance(getContext());
        volleyApp.getRequestQueue().cancelAll(tag);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        loadData();
    }

    private class MeteogrammaResponseListener implements Response.Listener<Previsione> {

        @Override
        public void onResponse(Previsione response) {

            loadMeteogrammaData(response);
            setTitleSlides();
            setMeteogrammaViews();
            setDayImageView();

            setViewVisibility(tvTemperature1, imgTemperature1);
            setViewVisibility(tvTemperature2, imgTemperature2);
            setViewVisibility(tvRain, imgRain);
            setViewVisibility(tvSnow, imgSnow);
            setViewVisibility(tvWind, imgWind);
            setViewVisibility(tvReliability);
            setViewVisibility(tvDate);

            if (appLanguage == Previsione.Language.IT) {
                loadBollettinoData(response);
                setBollettinoViews();
            }
        }

        protected final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull View view, int index) {
                view.setVisibility(View.GONE);
            }
        };

        protected final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull View view, int index) {
                view.setVisibility(View.VISIBLE);
            }
        };

        protected void setViewVisibility(TextView text, View image) {
            String caption = (String) text.getText();
            if (caption == null || caption.equals("")) {
                ButterKnife.apply(image, GONE);
                ButterKnife.apply(text, GONE);
            } else {
                ButterKnife.apply(image, VISIBLE);
                ButterKnife.apply(text, VISIBLE);
            }
        }

        protected void setViewVisibility(TextView text) {
            String caption = (String) text.getText();
            if (caption == null || caption.equals("")) {
                ButterKnife.apply(text, GONE);
            } else {
                ButterKnife.apply(text, VISIBLE);
            }
        }
    }

    private class BollettinoResponseListener extends MeteogrammaResponseListener {

        @Override
        public void onResponse(Previsione response) {
            loadBollettinoData(response);
            setBollettinoViews();
        }
    }

    private void loadMeteogrammaData(Previsione response) {
        Town town = currentLocation.getTown();
        int zoneIdx = town.getZone() - 1;

        Meteogramma[] meteogrammi = null;
        Meteogramma.Scadenza[] scadenze = null;

        meteogrammi = response.getMeteogramma();
        Meteogramma meteogramma = meteogrammi[zoneIdx];
        scadenze = meteogramma.getScadenza();

        daySky = scadenze[pageNumber].getCielo();
        String[] temperatures = new String[4];
        temperatures[0] = scadenze[pageNumber].getProperty(Meteogramma.Scadenza.TEMPERATURA);
        temperatures[1] = scadenze[pageNumber].getProperty(Meteogramma.Scadenza.TEMPERATURA_1500);
        temperatures[2] = scadenze[pageNumber].getProperty(Meteogramma.Scadenza.TEMPERATURA_2000);
        temperatures[3] = scadenze[pageNumber].getProperty(Meteogramma.Scadenza.TEMPERATURA_3000);
        String[] level = new String[4];
        level[0] = "";
        level[1] = " (1500 m.)";
        level[2] = " (2000 m.)";
        level[3] = " (3000 m.)";
        temperature1 = "";
        temperature2 = "";
        int count = 0;
        int index = 0;
        while (count < 2 && index < 4) {
            if (temperatures[index] != null && !temperatures[index].equals("")) {
                if (temperature1.equals("")) {
                    temperature1 = temperatures[index] + level[index];
                } else if (temperature2.equals("")) {
                    temperature2 = temperatures[index] + level[index];
                }
                count++;
            }
            index++;
        }
        rain = scadenze[pageNumber].getPrecipitazioni();
        probability = scadenze[pageNumber].getProbabilitaPrecipitazione();
        snow = scadenze[pageNumber].getQuotaNeve();
        wind = scadenze[pageNumber].getProperty(Meteogramma.Scadenza.VENTO);
        reliability = scadenze[pageNumber].getAttendibilita();
        date = response.getData();

        daySkyUrl = scadenze[pageNumber].getSimbolo();

        scadenzaDate = scadenze[pageNumber].getData();
    }

    private void loadBollettinoData(Previsione response) {
        Bollettino bollettino = null;
        Bollettino.Giorno[] giorni = null;

        bollettino = response.getMeteoVeneto();
        giorni = bollettino.getGiorni();

        if (response.getLanguage() == Previsione.Language.IT) {
            int dayIdx = 0;
            switch (pageNumber) {
                case 0:
                    dayIdx = 0;
                    break;
                case 1:
                case 2:
                    dayIdx = 1;
                    break;
                case 3:
                case 4:
                    dayIdx = 2;
                    break;
                case 5:
                    dayIdx = 3;
                    break;
                case 6:
                    dayIdx = 4;
                    break;

            }

            description = giorni[dayIdx].getTesto();
        }
    }

    private void setTitleSlides() {
        titlesCallBack.setTitle(scadenzaDate, pageNumber);
    }

    private void setDayImageView() {
        mImageLoader.get(daySkyUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                try {
                    imgDaySky.setImageUrl(daySkyUrl, mImageLoader);
                } catch (NullPointerException e) {
                    Timber.e(e.toString());
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e("Image Load Error: %s", error.getMessage());
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setMeteogrammaViews() {
        try {
            tvDaySky.setText(daySky);
            tvTemperature1.setText(temperature1);
            tvTemperature2.setText(temperature2);
            tvRain.setText(rain + " (" + probability + ")");
            tvSnow.setText(snow);
            tvWind.setText(wind);
            if (reliability.length() > 0)
                tvReliability.setText(AttendibilitaLabel + ": " + reliability);
            else
                tvReliability.setText(reliability);
            tvDate.setText(AggiornatoLabel + ": " + date);
        } catch (NullPointerException e) {
            Timber.e(e.toString());
        }
    }

    private void setBollettinoViews() {
        if (description != null && description.length() > 0) {
            tvDescription.setText(Html.fromHtml(description));
            tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
            tvDescription.setLinksClickable(true);
            tvDescription.setVisibility(View.VISIBLE);
        }
    }
}
