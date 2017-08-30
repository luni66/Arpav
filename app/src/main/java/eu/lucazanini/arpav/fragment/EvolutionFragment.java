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
import eu.lucazanini.arpav.activity.ActivityCallBack;
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

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class EvolutionFragment extends Fragment implements Observer {

    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGES = "pages";
    protected @BindView(R.id.text_avviso) TextView tvAvviso;
    protected @BindView(R.id.text_fenomeni) TextView tvFenomeni;
    /**
     * The seven day image views
     */
    protected @BindViews({R.id.image_daySky1, R.id.image_daySky2a, R.id.image_daySky2b, R.id.image_daySky3a, R.id.image_daySky3b, R.id.image_daySky4, R.id.image_daySky5}) NetworkImageView[] imgDays;
    /**
     * The five date text views (the second and third dates have two images)
     */
    protected @BindViews({R.id.text_date1, R.id.text_date2, R.id.text_date3, R.id.text_date4, R.id.text_date5}) TextView[] tvDates;
    protected @BindView((R.id.text_evolution)) TextView tvEvolution;
    protected @BindView(R.id.evolutionProgressBar) ProgressBar progressBar;
    private Unbinder unbinder;
    private CurrentLocation currentLocation;
    private Context context;
    /**
     * The seven urls where there are images
     */
    private String[] daySkyUrl;
    /**
     * The five dates
     */
    private String[] dates;
    private String avviso, fenomeni, date, evoluzione;
    private int pageNumber, pages;
    private Preferences preferences;
    private VolleySingleton volleyApp;
    private ImageLoader mImageLoader;
    private Previsione.Language appLanguage;
    private ActivityCallBack activityCallBack;

    protected @BindView(R.id.swipe_container) SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        setHasOptionsMenu(true);

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

        View v = inflater.inflate(R.layout.fragment_preview, container, false);

        unbinder = ButterKnife.bind(this, v);

        daySkyUrl = new String[imgDays.length];
        dates = new String[tvDates.length];

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
            downloadData();
            activityCallBack.keepFragments(pageNumber);
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
        downloadData();
    }

    private void downloadData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (currentLocation.isDefined()) {
            BulletinRequest meteogrammaRequest = new BulletinRequest(Previsione.getUrl(appLanguage),
                    new MeteogrammaResponseListener(), new ErrorResponseListener(), Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(meteogrammaRequest);
        }

        if (appLanguage != Previsione.Language.IT) {
            BulletinRequest BollettinoRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT),
                    new BollettinoResponseListener(), new ErrorResponseListener(), Integer.toString(pageNumber));
            volleyApp.addToRequestQueue(BollettinoRequest);
        }
    }

    private void loadMeteogrammaData(Previsione response) {
        Town town = currentLocation.getTown();
        int zoneIdx = town.getZone() - 1;

        Meteogramma[] meteogrammi = response.getMeteogramma();
        Meteogramma meteogramma = meteogrammi[zoneIdx];
        Meteogramma.Scadenza[] scadenze = meteogramma.getScadenza();

        date = response.getData();

        for (int i = 0; i < imgDays.length; i++) {
            daySkyUrl[i] = scadenze[i].getSimbolo();
        }
        for (int i = 0; i < tvDates.length; i++) {
            dates[i] = scadenze[toDateIndex(i)].getShortDate();
        }
    }

    private void loadBollettinoData(Previsione response) {
        Bollettino bollettino = response.getMeteoVeneto();

        avviso = bollettino.getAvviso();
        fenomeni = bollettino.getFenomeniParticolari();
        evoluzione = bollettino.getEvoluzioneGenerale();
    }

    private void setDayImageView(String daySkyUrl, NetworkImageView imgDay) {
        mImageLoader.get(daySkyUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imgDay.setImageUrl(daySkyUrl, mImageLoader);
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
        for (int i = 0; i < tvDates.length; i++) {
            tvDates[i].setText(dates[i]);
        }

        for (int i = 0; i < imgDays.length; i++) {
            setDayImageView(daySkyUrl[i], imgDays[i]);
        }
    }

    private void setBollettinoViews() {
        setViewText(tvAvviso, avviso);
        setViewText(tvFenomeni, fenomeni);
        setViewText(tvEvolution, evoluzione);

        setViewVisibility(tvAvviso);
        setViewVisibility(tvFenomeni);
        setViewVisibility(tvEvolution);
    }

    private int toDateIndex(int meteogrammaIndex) {
        switch (meteogrammaIndex) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 5;
            case 4:
                return 6;
            default:
                return -1;
        }
    }

    private void setViewText(TextView view, String text) {
        if (text != null && text.length() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.setText(Html.fromHtml(text, FROM_HTML_MODE_LEGACY));
            } else {
                view.setText(Html.fromHtml(text));
            }
        } else {
            view.setText("");
        }
    }

    private void setViewVisibility(TextView view, View image) {
        String caption = view.getText().toString();
        if (caption == null || caption.equals("")) {
            ButterKnife.apply(image, GONE);
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(image, VISIBLE);
            ButterKnife.apply(view, VISIBLE);
        }
    }

    private void setViewVisibility(TextView view) {
        String caption = view.getText().toString();
        if (caption == null || caption.equals("")) {
            ButterKnife.apply(view, GONE);
        } else {
            ButterKnife.apply(view, VISIBLE);
        }
    }

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

    private class MeteogrammaResponseListener implements Response.Listener<Previsione> {

        @Override
        public void onResponse(Previsione response) {

            loadMeteogrammaData(response);
            setMeteogrammaViews();

            if (appLanguage == Previsione.Language.IT) {
                loadBollettinoData(response);
                setBollettinoViews();;
            }
        }


    }

    private class BollettinoResponseListener extends MeteogrammaResponseListener {

        @Override
        public void onResponse(Previsione response) {
            loadBollettinoData(response);
            setBollettinoViews();

            setViewVisibility(tvAvviso);
            setViewVisibility(tvFenomeni);
            setViewVisibility(tvEvolution);
        }
    }

    private class ErrorResponseListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            Timber.e(error);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

}
