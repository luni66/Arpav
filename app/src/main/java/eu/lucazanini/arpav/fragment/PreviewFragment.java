package eu.lucazanini.arpav.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.Observer;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.lucazanini.arpav.R;
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

public class PreviewFragment extends Fragment implements Observer {

    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGES = "pages";
    protected @BindView(R.id.text_avviso) TextView tvAvviso;
    protected @BindView(R.id.text_fenomeni) TextView tvFenomeni;
//    protected @BindView(R.id.image_daySky1) NetworkImageView imgDaySky1;
//    protected @BindView(R.id.image_daySky2a) NetworkImageView imgDaySky2a;
//    protected @BindView(R.id.image_daySky2b) NetworkImageView imgDaySky2b;
//    protected @BindView(R.id.image_daySky3a) NetworkImageView imgDaySky3a;
//    protected @BindView(R.id.image_daySky3b) NetworkImageView imgDaySky3b;
//    protected @BindView(R.id.image_daySky4) NetworkImageView imgDaySky4;
//    protected @BindView(R.id.image_daySky5) NetworkImageView imgDaySky5;
    protected @BindViews({R.id.image_daySky1, R.id.image_daySky2a,R.id.image_daySky2b,R.id.image_daySky3a,R.id.image_daySky3b,R.id.image_daySky4,R.id.image_daySky5}) NetworkImageView[] imgDays;
    protected @BindViews({R.id.text_date1, R.id.text_date2, R.id.text_date3, R.id.text_date4, R.id.text_date5}) TextView[] dates;
    protected @BindView((R.id.text_evolution)) TextView tvEvolution;
    protected @BindView(R.id.evolutionProgressBar) ProgressBar progressBar;
    private Unbinder unbinder;
    private CurrentLocation currentLocation;
    private Context context;
    private String[] daySkyUrl;
    private String[] scadenzaDate;
    private String avviso, fenomeni, date, evoluzione;
    private int pageNumber, pages, meteogrammaIndex;
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
        meteogrammaIndex = getMeteogrammaIndex();

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
//        return super.onCreateView(inflater, container, savedInstanceState);
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_preview, container, false);

        unbinder = ButterKnife.bind(this, v);

        daySkyUrl = new String[imgDays.length];
        scadenzaDate = new String[imgDays.length];

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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        loadData();
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

    private void loadMeteogrammaData(Previsione response) {
        Town town = currentLocation.getTown();
        int zoneIdx = town.getZone() - 1;

        Meteogramma[] meteogrammi = null;
        Meteogramma.Scadenza[] scadenze = null;

        meteogrammi = response.getMeteogramma();
        Meteogramma meteogramma = meteogrammi[zoneIdx];
        scadenze = meteogramma.getScadenza();

        date = response.getData();

        for(int i = 0; i<imgDays.length; i++) {
            daySkyUrl[i] = scadenze[i].getSimbolo();
            scadenzaDate[i] = scadenze[i].getShortDate();
        }
    }

    private void loadBollettinoData(Previsione response) {
        Bollettino bollettino = null;
//        Bollettino.Giorno[] giorni = null;

        bollettino = response.getMeteoVeneto();
//        giorni = bollettino.getGiorni();

        avviso = bollettino.getAvviso();
        fenomeni = bollettino.getFenomeniParticolari();
        evoluzione = bollettino.getEvoluzioneGenerale();
    }

    private void setMeteogrammaViews() {
        try {
            for(int i =0; i<dates.length; i++) {
                dates[i].setText(scadenzaDate[i]);
            }

//            tvDate.setText(AggiornatoLabel + ": " + date);
        } catch (NullPointerException e) {
            Timber.e(e.toString());
        }
    }

    private void setDayImageView(String daySkyUrl, NetworkImageView imgDay) {
            mImageLoader.get(daySkyUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    try {
                        imgDay.setImageUrl(daySkyUrl, mImageLoader);
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

    private void setBollettinoViews() {
        tvAvviso.setText(avviso);
        tvFenomeni.setText(fenomeni);
        tvEvolution.setText(evoluzione);
    }

    private int getMeteogrammaIndex() {
        if (pageNumber > 0 && pageNumber < pages) {
            return pageNumber - 1;
        } else {
            return -1;
        }
    }


    private class MeteogrammaResponseListener implements Response.Listener<Previsione> {

        @Override
        public void onResponse(Previsione response) {

            loadMeteogrammaData(response);
//            setTitleSlides();
            setMeteogrammaViews();
            for(int i = 0; i<imgDays.length; i++) {
                setDayImageView(daySkyUrl[i], imgDays[i]);
            }

            setViewVisibility(tvAvviso);
            setViewVisibility(tvFenomeni);

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

            setViewVisibility(tvEvolution);
        }
    }

}
