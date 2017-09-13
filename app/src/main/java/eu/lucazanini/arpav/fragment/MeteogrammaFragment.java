package eu.lucazanini.arpav.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import eu.lucazanini.arpav.activity.ActivityCallBack;
import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import timber.log.Timber;

public class MeteogrammaFragment extends Fragment implements Observer {

    public final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam sit amet arcu ultricies, porttitor libero in, fringilla erat. Proin sollicitudin in lacus eu pharetra. Duis ultricies justo gravida ligula lacinia. ";
    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGES = "pages";
    private final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            view.setVisibility(View.GONE);
        }
    };
    private final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    };
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
    //    protected @BindView(R.id.downloadProgressBar) ProgressBar progressBar;
    protected @BindView(R.id.text_date) TextView tvDate;
    protected @BindString(R.string.attendibilita) String AttendibilitaLabel;
    protected @BindString(R.string.aggiornato) String AggiornatoLabel;
    protected @BindView(R.id.text_description) TextView tvDescription;
    protected @BindView(R.id.swipe_container) SwipeRefreshLayout mSwipeRefreshLayout;
    private Context context;
    private Unbinder unbinder;
    private String daySkyUrl, daySky, temperature1, temperature2, rain, probability, snow, wind, reliability, date, description;
    private String scadenzaDate;
    private int pageNumber, pages, meteogrammaIndex;
    private CurrentLocation currentLocation;
    private ActivityCallBack activityCallBack;
    private PreferenceHelper preferences;
    private VolleySingleton volleyApp;
    private ImageLoader mImageLoader;
    private Previsione.Language appLanguage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        pageNumber = args.getInt(PAGE_NUMBER);
        pages = args.getInt(PAGES);
        meteogrammaIndex = getMeteogrammaIndex();

        currentLocation = CurrentLocation.getInstance(context);
        currentLocation.addObserver(this);

        preferences = new PreferenceHelper(context);

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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
                activityCallBack.keepFragments(pageNumber);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        downloadData();

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    downloadData();
                    activityCallBack.keepFragments(pageNumber);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof ActivityCallBack) {
            activityCallBack = (ActivityCallBack) activity;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
    public void update(java.util.Observable o, Object arg) {
        downloadData();
    }

    private void setTitleSlides() {
        activityCallBack.setTitle(scadenzaDate, pageNumber);
    }

    private void downloadData() {
//        if (progressBar != null) {
//            progressBar.setVisibility(View.VISIBLE);
//        }

        if (currentLocation.isDefined()) {
            BulletinRequest meteogrammaRequest = new BulletinRequest(Previsione.getUrl(appLanguage),
                    new MeteogrammaResponseListener(), new ErrorResponseListener(), Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(meteogrammaRequest);
        }

        PreferenceHelper preferences = new PreferenceHelper(context);
        if (appLanguage != Previsione.Language.IT && preferences.isBulletinDisplayed()) {
            BulletinRequest BollettinoRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT),
                    new BollettinoResponseListener(), new ErrorResponseListener(), Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(BollettinoRequest);
        }
    }

    private void hideRefreshWidget() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
    }

    private void loadMeteogrammaData(Previsione response) {
        Town town = currentLocation.getTown();
        int zoneIdx = town.getZone() - 1;

        Meteogramma[] meteogrammi = response.getMeteogramma();
        Meteogramma meteogramma = meteogrammi[zoneIdx];
        Meteogramma.Scadenza[] scadenze = meteogramma.getScadenza();

        daySky = scadenze[meteogrammaIndex].getCielo();
        String[] temperatures = new String[4];
        temperatures[0] = scadenze[meteogrammaIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA);
        temperatures[1] = scadenze[meteogrammaIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_1500);
        temperatures[2] = scadenze[meteogrammaIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_2000);
        temperatures[3] = scadenze[meteogrammaIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_3000);
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
        rain = scadenze[meteogrammaIndex].getPrecipitazioni();
        probability = scadenze[meteogrammaIndex].getProbabilitaPrecipitazione();
        snow = scadenze[meteogrammaIndex].getQuotaNeve();
        wind = scadenze[meteogrammaIndex].getProperty(Meteogramma.Scadenza.VENTO);
        reliability = scadenze[meteogrammaIndex].getAttendibilita();
        date = response.getData();

        daySkyUrl = scadenze[meteogrammaIndex].getSimbolo();

        scadenzaDate = scadenze[meteogrammaIndex].getData();
    }

    private void loadBollettinoData(Previsione response) {
        Bollettino bollettino = response.getMeteoVeneto();
        Bollettino.Giorno[] giorni = bollettino.getGiorni();

        if (response.getLanguage() == Previsione.Language.IT) {
            int dayIdx = 0;
            switch (pageNumber) {
                case 1:
                    dayIdx = 0;
                    break;
                case 2:
                case 3:
                    dayIdx = 1;
                    break;
                case 4:
                case 5:
                    dayIdx = 2;
                    break;
                case 6:
                    dayIdx = 3;
                    break;
                case 7:
                    dayIdx = 4;
                    break;

            }

            description = giorni[dayIdx].getTesto();
        }
    }

    private void setDayImageView() {
        mImageLoader.get(daySkyUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                try {
                    imgDaySky.setImageUrl(daySkyUrl, mImageLoader);
                } catch (NullPointerException e) {
                    Timber.e(e.getLocalizedMessage());
                }
//                if (progressBar != null) {
//                    progressBar.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error.getMessage());
//                if (progressBar != null) {
//                    progressBar.setVisibility(View.GONE);
//                }
            }
        });
    }

    private void setMeteogrammaViews() {
        setViewText(tvDaySky, daySky);
        setViewText(tvTemperature1, temperature1);
        setViewText(tvTemperature2, temperature2);
        if (probability.length() > 0)
            setViewText(tvRain, rain + " (" + probability + ")");
        else
            setViewText(tvRain, rain + "");
        setViewText(tvSnow, snow);
        setViewText(tvWind, wind);
        if (reliability.length() > 0)
            setViewText(tvReliability, AttendibilitaLabel + ": " + reliability);
        else
            setViewText(tvReliability, "");
        if (tvDate.length() > 0)
            setViewText(tvDate, AggiornatoLabel + ": " + date);
        else
            setViewText(tvDate, "");

        setViewVisibility(tvTemperature1, imgTemperature1);
        setViewVisibility(tvTemperature2, imgTemperature2);
        setViewVisibility(tvRain, imgRain);
        setViewVisibility(tvSnow, imgSnow);
        setViewVisibility(tvWind, imgWind);
        setViewVisibility(tvReliability);
        setViewVisibility(tvDate);
    }

    private void setBollettinoViews() {
        setViewText(tvDescription, description);
        tvDescription.setVisibility(View.VISIBLE);
    }

    private int getMeteogrammaIndex() {
        if (pageNumber > 0 && pageNumber < pages) {
            return pageNumber - 1;
        } else {
            return -1;
        }
    }

    private void setViewText(TextView view, String text) {
        if (text != null && text.length() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                view.setText(Html.fromHtml(text));
            }
        } else {
            view.setText("");
        }
    }

    private void setViewVisibility(TextView view, View image) {
        String caption = view.getText().toString();
        if (caption.equals("")) {
            ButterKnife.apply(image, GONE);
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(image, VISIBLE);
            ButterKnife.apply(view, VISIBLE);
        }
    }

    private void setViewVisibility(TextView view) {
        String caption = view.getText().toString();
        if (caption.equals("")) {
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(view, VISIBLE);
        }
    }

    private class MeteogrammaResponseListener implements Response.Listener<Previsione> {

        @Override
        public void onResponse(Previsione response) {

            loadMeteogrammaData(response);
            setTitleSlides();
            setMeteogrammaViews();
            setDayImageView();

//            setViewVisibility(tvTemperature1, imgTemperature1);
//            setViewVisibility(tvTemperature2, imgTemperature2);
//            setViewVisibility(tvRain, imgRain);
//            setViewVisibility(tvSnow, imgSnow);
//            setViewVisibility(tvWind, imgWind);
//            setViewVisibility(tvReliability);
//            setViewVisibility(tvDate);

            PreferenceHelper preferences = new PreferenceHelper(context);
            if (appLanguage == Previsione.Language.IT && preferences.isBulletinDisplayed()) {
                loadBollettinoData(response);
                setBollettinoViews();
            }

            hideRefreshWidget();
        }
    }

    private class BollettinoResponseListener extends MeteogrammaResponseListener {

        @Override
        public void onResponse(Previsione response) {
            loadBollettinoData(response);
            setBollettinoViews();

            hideRefreshWidget();
        }
    }

    private class ErrorResponseListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            hideRefreshWidget();

            String defaultError = "Volley generic error";
            String errorMessage = error.getLocalizedMessage();
            if (errorMessage != null && !errorMessage.equals("")) {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), defaultError, Toast.LENGTH_SHORT).show();
            }

//        if (progressBar != null) {
//            progressBar.setVisibility(View.GONE);
//        }
        }
    }
}
