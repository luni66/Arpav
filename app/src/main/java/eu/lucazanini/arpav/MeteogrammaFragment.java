package eu.lucazanini.arpav;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.ImageCacheManager;
import eu.lucazanini.arpav.network.VolleySingleton;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

import static eu.lucazanini.arpav.model.Meteogramma.SCADENZA_IDX;
import static eu.lucazanini.arpav.model.Previsione.MG_IDX;

/**
 * Created by luke on 23/01/17.
 */

public class MeteogrammaFragment extends Fragment implements Observer {

    public static final String NUMBER_PAGE = "number_page";
    protected static final int FIRST_DAY = 0;
    protected static final int SECOND_MORNING_DAY = 1;
    protected static final int SECOND_AFTERNOON_DAY = 2;
    protected static final int THIRD_MORNING_DAY = 3;
    protected static final int THIRD_AFTERNOON_DAY = 4;
    protected static final int FOURTH_DAY = 5;
    protected static final int FIFTH_DAY = 6;
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
    protected ImageView imgTemperature2;
    @BindView(R.id.image_snow)
    protected ImageView imgSnow;
    @BindView(R.id.image_wind)
    protected ImageView imgWind;
    @BindView(R.id.save_location)
    protected Button btnSaveLocation;
    private Context context;
    private Unbinder unbinder;
    private String location, daySkyUrl, daySky, temperature1, temperature2, rain, probability, snow, wind, reliability;
    private int mgIndex;

    private Subscription actvSub, actvSub2;

    private CurrentLocation currentLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Bundle args = getArguments();
        int numberPage = args.getInt(NUMBER_PAGE);
        mgIndex = numberPage;

        currentLocation = CurrentLocation.getInstance();
        currentLocation.addObserver(this);

//        loadData();

        Timber.d("fragment %s observers currentLocation", mgIndex);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_mg_first_day, container, false);

        unbinder = ButterKnife.bind(this, v);

        Town town = currentLocation.getTown();

        if (town == null) {
            String townName = getPrefsLocation();
            town = TownList.getInstance(context).getTown(townName);
            if (town != null) {
                currentLocation.setTown(town);
                actvLocation.setText(town.getName());
//                loadData();
            }
        } else {
            actvLocation.setText(town.getName());
//            loadData();
        }

/*        String townName = getPrefsLocation();

        if (townName != null && !townName.equals("")) {
            actvLocation.setText(townName);
            CurrentLocation currentLocation = CurrentLocation.getInstance();
            Town town = TownList.getInstance(context).getTown(townName);
            if (town != null) {
                currentLocation.setTown(TownList.getInstance(context).getTown(townName));
                loadData();
            }
        }*/

//        java.util.Observable currentLocation = CurrentLocation.getInstance();
//        currentLocation.addObserver(new Observer() {
//            @Override
//            public void update(java.util.Observable o, Object arg) {
//                String name = (String) arg;
//                Timber.d("OBSERVER " + name);
//                loadData();
//            }
//        });


        String[] names = TownList.getInstance(getContext()).getNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, names);
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
            }
        });

//        actvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Timber.d("TEXT CHANGED AT "+adapterView.getItemAtPosition(i));
//            }
//        });

//        Observable test = RxAutoCompleteTextView.itemClickEvents(actvLocation);

        actvSub = RxAutoCompleteTextView.itemClickEvents(actvLocation).subscribe(new Action1<AdapterViewItemClickEvent>() {
            @Override
            public void call(AdapterViewItemClickEvent adapterViewItemClickEvent) {
                String name = actvLocation.getText().toString();

                currentLocation.setTown(TownList.getInstance(context).getTown(name));

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

    @OnClick(R.id.save_location)
    protected void savePrefsLocation() {
        String townName = actvLocation.getText().toString();

        Town town = TownList.getInstance(context).getTown(townName);

        if (town != null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.current_location), townName);
            editor.commit();
        }
    }

    private String getPrefsLocation() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String townName = sharedPref.getString(getString(R.string.current_location), "");
        return townName;
    }

    private List<Town> loadTowns() {
        List<Town> towns = TownList.getInstance(getContext()).loadTowns();
        return towns;
    }

    private void loadData() {

        final VolleySingleton volleyApp = VolleySingleton.getInstance(getContext());
        final ImageLoader mImageLoader = volleyApp.getImageLoader();

        Cache.Entry entry = volleyApp.getRequestQueue().getCache().get(Previsione.getUrl(Previsione.Language.IT));

        if(entry!=null){
            Timber.d("found file in cache ");
            Date date = new Date(entry.serverDate);
            Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            Timber.d("serverDate " + format.format(date));

            Map<String, String> responseHeaders = entry.responseHeaders;

  PrettyPrintingMap test = new PrettyPrintingMap(responseHeaders);
            Timber.d(test.toString());


//            String s = new String(entry.data);
//            Timber.d(s);

            Previsione previsione = new Previsione(Previsione.URL_IT, new String(entry.data));

            if(previsione.isUpdate()){

            }

        } else {
            Timber.d("not found file in cache");
        }

        BulletinRequest bulletinRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT),
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

/*                        for (int i = 0; i < MG_IDX; i++) {
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
                        }*/

                        if (currentLocation.isDefined()) {
                            Town town = currentLocation.getTown();
                            int zoneIdx = town.getZone() - 1;

                            daySky = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getCielo();
                            String[] temperatures = new String[4];
                            temperatures[0] = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA);
                            temperatures[1] = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_1500);
                            temperatures[2] = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_2000);
                            temperatures[3] = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProperty(Meteogramma.Scadenza.TEMPERATURA_3000);
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
                            rain = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getPrecipitazioni();
                            probability = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProbabilitaPrecipitazione();
                            snow = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getQuotaNeve();
                            wind = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getProperty(Meteogramma.Scadenza.VENTO);
                            reliability = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getAttendibilita();

                            try {
                                tvDaySky.setText(daySky);
                                tvTemperature1.setText(temperature1);
                                tvTemperature2.setText(temperature2);
                                tvRain.setText(rain + " (" + probability + ")");
                                tvSnow.setText(snow);
                                tvWind.setText(wind);
                                tvReliability.setText(reliability);
                            } catch (NullPointerException e) {
                                Timber.e(e.toString());
                            }

//                            if (temperature2.equals("")) {
//                                ButterKnife.apply(tvTemperature2, GONE);
//                                ButterKnife.apply(imgTemperature2, GONE);
//                            }
//
//                            if (snow == null || snow.equals("")) {
//                                ButterKnife.apply(tvSnow, GONE);
//                                ButterKnife.apply(imgSnow, GONE);
//                            }
//
//                            if (wind == null || wind.equals("")) {
//                                ButterKnife.apply(tvWind, GONE);
//                                ButterKnife.apply(imgWind, GONE);
//                            }
//
//                            if (reliability == null || reliability.equals("")) {
//                                ButterKnife.apply(tvReliability, GONE);
//                            }

                            daySkyUrl = response.getMeteogramma()[zoneIdx].getScadenza()[mgIndex].getSimbolo();
                            mImageLoader.get(daySkyUrl, new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                    try {
                                        imgDaySky.setImageUrl(daySkyUrl, mImageLoader);
                                    } catch (NullPointerException e) {
                                        Timber.e(e.toString());
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Timber.e("Image Load Error: " + error.getMessage());
                                }
                            });

//                            ImageLoader imageLoader = ImageCacheManager.getInstance().getImageLoader();
//                            if(imageLoader!=null)
//                            imgDaySky.setImageUrl(daySkyUrl, imageLoader);


                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
            }
        }, Integer.toString(mgIndex));

        volleyApp.addToRequestQueue(bulletinRequest);
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
        currentLocation.deleteObserver(this);
        Timber.d("fragment %s doesn't observe currentLocation", mgIndex);

        String tag = Integer.toString(mgIndex);
        final VolleySingleton volleyApp = VolleySingleton.getInstance(getContext());
        volleyApp.getRequestQueue().cancelAll(tag);
//        actvSub.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // TODO verificare se senza unbinder non è necessario try/catch per view
        unbinder.unbind();
        actvSub.unsubscribe();
        actvSub2.unsubscribe();
    }

    @Override
    public void update(java.util.Observable o, Object arg) {
        if(actvLocation!=null)
            try {
                actvLocation.setText((String) arg);
            } catch (NullPointerException e) {
           Timber.e(e.toString());
            }
        loadData();
    }

    private class PrettyPrintingMap<K, V> {
        private Map<K, V> map;

        public PrettyPrintingMap(Map<K, V> map) {
            this.map = map;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<K, V> entry = iter.next();
                sb.append(entry.getKey());
                sb.append('=').append('"');
                sb.append(entry.getValue());
                sb.append('"');
                if (iter.hasNext()) {
                    sb.append(',').append(' ');
                }
            }
            return sb.toString();

        }
    }
}
