package eu.lucazanini.arpav;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.List;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.task.ReportTask;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

import static eu.lucazanini.arpav.model.Meteogramma.SCADENZA_IDX;
import static eu.lucazanini.arpav.model.Previsione.MG_IDX;

;

/**
 * Created by luke on 23/01/17.
 */

public class MeteogrammaFragment extends Fragment {

    private Context context;
    private Unbinder unbinder;

    @BindView(R.id.text_location)
    protected AppCompatAutoCompleteTextView actvLocation;
    @BindView(R.id.image_daySky)
    protected NetworkImageView imgDaySky;
    @BindView(R.id.text_sky)
    protected TextView tvDaySky;
    @BindView(R.id.text_temperature1)
    protected TextView tvTemperature1;
    @BindView(R.id.text_temperature2)
    protected TextView tvTemperature2;
    @BindView(R.id.text_rain)
    protected TextView tvRain;
    @BindView(R.id.text_snow)
    protected TextView tvSnow;
    @BindView(R.id.text_wind)
    protected TextView tvWind;
    @BindView(R.id.text_reliability)
    protected TextView tvReliability;

    @BindView(R.id.image_temperature2)
    ImageView imgTemperature2;
    @BindView(R.id.image_snow)
    ImageView imgSnow;
    @BindView(R.id.image_wind)
    ImageView imgWind;

    private String location, daySkyUrl, daySky, temperature1, temperature2, rain, probability, snow, wind, reliability;

    //TextView tvDaySky;
    Subscription actvSub, actvSub2;
/*    @BindView(R.id.menu_item_search)
    SearchView searchView;*/

/*    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        setHasOptionsMenu(true);
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_mg_first_day, container, false);
//        v.setTag("test");

        unbinder = ButterKnife.bind(this, v);

        String townName = getCurrentLocation();

        if (townName != null) {
            actvLocation.setText(townName);
            CurrentLocation currentLocation = CurrentLocation.getInstance();
            currentLocation.setTown(TownList.getInstance(context).getTown(townName));
            loadData();
        }

        java.util.Observable currentLocation = CurrentLocation.getInstance();
        currentLocation.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                String name = (String) arg;
                Timber.d("OBSERVER " + name);
                loadData();
            }
        });

//        afternoonImage = (NetworkImageView)v.findViewById(R.id.networkImageView);
//        tvDaySky=(TextView)v.findViewById(R.id.txtCielo);

//        List<Town> towns = loadTowns();

//        String[] names = Town.getNames(getContext());

        String[] names = TownList.getInstance(getContext()).getNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, names);
//        actvLocation.setThreshold(2);
        actvLocation.setAdapter(adapter);

//        actvLocation.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Timber.d("TEXT ADDED "+ actvLocation.getText());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                Timber.d("TEXT CHANGED");
//            }
//        });

        actvSub2 = RxTextView.textChanges(actvLocation).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                Timber.d("textChanges " + actvLocation.getText());
            }
        });

//        actvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Timber.d("TEXT CHANGED AT "+adapterView.getItemAtPosition(i));
//            }
//        });

        Observable test = RxAutoCompleteTextView.itemClickEvents(actvLocation);

        actvSub = RxAutoCompleteTextView.itemClickEvents(actvLocation).subscribe(new Action1<AdapterViewItemClickEvent>() {
            @Override
            public void call(AdapterViewItemClickEvent adapterViewItemClickEvent) {
                String name = actvLocation.getText().toString();

                Timber.d("itemClickEvents " + name);

                CurrentLocation currentLocation = CurrentLocation.getInstance();
                currentLocation.setTown(TownList.getInstance(context).getTown(name));

                saveCurrentLocation(name);

                // Check if no view has focus:
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }


            }
        });

//        actvSub = RxAutoCompleteTextView.itemClickEvents(actvLocation).subscribe(e ->
//                {
//                    Timber.d("TEXT CHANGED AT " e.clickedView());
//                }
//        );

        loadData();

        return v;

    }

    private void saveCurrentLocation(String townName) {
//        Context context = getActivity();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.current_location), townName);
        editor.commit();

    }

    private String getCurrentLocation() {
//        Context context = getActivity();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String townName = sharedPref.getString(getString(R.string.current_location), "");
        return townName;
    }

    private List<Town> loadTowns() {
        List<Town> towns = TownList.getInstance(getContext()).loadTowns();
//        towns = Town.loadTowns(getContext());
        return towns;
    }

    private void loadData() {
        ReportTask reportTask = new ReportTask(getContext());
        final VolleySingleton volleyApp = VolleySingleton.getInstance(getContext());
        final ImageLoader mImageLoader = volleyApp.getImageLoader();

        BulletinRequest bulletinRequest = new BulletinRequest(Request.Method.GET, Previsione.URL_IT,
                new Response.Listener<Previsione>() {

                    //Action
                    final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
                        @Override
                        public void apply(View view, int index) {
                            view.setVisibility(View.GONE);
                        }
                    };

                    @Override
                    public void onResponse(Previsione response) {

                        Timber.d("found previsione for %s", response.getDataAggiornamento());


                        Meteogramma[] meteogrammi = response.getMeteogramma();
                        for (int i = 0; i < MG_IDX; i++) {
                            Meteogramma meteogramma = meteogrammi[i];

                            Meteogramma.Scadenza[] scadenze = meteogramma.getScadenza();
                            for (int j = 0; j < SCADENZA_IDX; j++) {
                                Meteogramma.Scadenza scadenza = scadenze[j];
                            }
                        }

                        if (response.getLanguage() == Previsione.Language.IT) {
                            Bollettino bollettino = response.getMeteoVeneto();
                            Bollettino.Giorno[] giorni = bollettino.getGiorni();
                            for (int i = 0; i < Bollettino.DAYS; i++) {
                                Bollettino.Giorno giorno = giorni[i];
                            }
                        }

                        for (int i = 0; i < MG_IDX; i++) {
                            for (int j = 0; j < SCADENZA_IDX; j++) {
                                String imgUrl = response.getMeteogramma()[i].getScadenza()[j].getSimbolo();
                                mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                        Timber.d("Image URL: " + response.getRequestUrl());
                                        Timber.d("Image Load completed: " + response.getRequestUrl());
                                    }

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Timber.e("Image Load Error: " + error.getMessage());
                                    }
                                });
                            }
                        }

                        CurrentLocation currentLocation = CurrentLocation.getInstance();
                        if (currentLocation.isDefined()) {
                            Town town = currentLocation.getTown();
                            int zoneIdx = town.getZone() - 1;

                            daySkyUrl = response.getMeteogramma()[zoneIdx].getScadenza()[0].getSimbolo();
                            daySky = response.getMeteogramma()[zoneIdx].getScadenza()[0].getCielo();
                            String[] temperatures = new String[4];
                            temperatures[0] = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProperty(Meteogramma.Scadenza.TEMPERATURA);
                            temperatures[1] = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProperty(Meteogramma.Scadenza.TEMPERATURA_1500);
                            temperatures[2] = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProperty(Meteogramma.Scadenza.TEMPERATURA_2000);
                            temperatures[3] = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProperty(Meteogramma.Scadenza.TEMPERATURA_3000);
                            String[] level = new String[4];
                            level[0] = "";
                            level[1] = " (1500 m.)";
                            level[2] = " (2000 m.)";
                            level[3] = " (3000 m.)";
                            temperature1="";
                            temperature2="";
                            int count = 0;
                            int index = 0;
                            while (count < 2 && index < 4) {
                                if (temperatures[index]!=null && !temperatures[index].equals("")) {
                                    if (temperature1.equals("")) {
                                        temperature1 = temperatures[index] + level[index];
                                    } else if (temperature2.equals("")) {
                                        temperature2 = temperatures[index] + level[index];
                                    }
                                    count++;
                                }
                                index++;
                            }
                            rain = response.getMeteogramma()[zoneIdx].getScadenza()[0].getPrecipitazioni();
                            probability = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProbabilitaPrecipitazione();
                            snow = response.getMeteogramma()[zoneIdx].getScadenza()[0].getQuotaNeve();
                            wind = response.getMeteogramma()[zoneIdx].getScadenza()[0].getProperty(Meteogramma.Scadenza.VENTO);
                            reliability = response.getMeteogramma()[zoneIdx].getScadenza()[0].getAttendibilita();


                            imgDaySky.setImageUrl(daySkyUrl, mImageLoader);
                            tvDaySky.setText(daySky);
                            tvTemperature1.setText(temperature1);
                            tvTemperature2.setText(temperature2);
                            tvRain.setText(rain + " (" + probability + ")");
                            tvSnow.setText(snow);
                            tvWind.setText(wind);
                            tvReliability.setText(reliability);

                            if(temperature2.equals("")) {
                                ButterKnife.apply(tvTemperature2, GONE);
                                ButterKnife.apply(imgTemperature2, GONE);
                            }

                            if(snow == null || snow.equals("")) {
                                ButterKnife.apply(tvSnow, GONE);
                                ButterKnife.apply(imgSnow, GONE);
                            }

                            if(wind==null || wind.equals("")) {
                                ButterKnife.apply(tvWind, GONE);
                                ButterKnife.apply(imgWind, GONE);
                            }

                            if(reliability==null || reliability.equals("")) {
                                ButterKnife.apply(tvReliability, GONE);
                            }
                        }


//                        PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) getActivity().findViewById(R.id.pager_title_strip);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
            }
        });

        volleyApp.addToRequestQueue(bulletinRequest);

        //TODO https://www.javadoc.io/doc/io.reactivex/rxjava/1.2.6 vedi create e fromEmitter per CurrentLocation

    }



/*    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.meteogramma_menu, menu);

        searchView.setSuggestionsAdapter(Town.getCursor(getContext()));
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
//        actvSub.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        actvSub.unsubscribe();
        actvSub2.unsubscribe();
    }
}
