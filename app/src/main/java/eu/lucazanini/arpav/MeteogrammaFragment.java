package eu.lucazanini.arpav;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTitleStrip;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    @BindView(R.id.skyView)
    NetworkImageView mNetworkImageView;
    @BindView(R.id.txtCielo)
    TextView cielo;
    @BindView(R.id.txtTemperatura)
    TextView temperatura;
    @BindView(R.id.txtPrecipitazioni)
    TextView precipitazioni;
    @BindView(R.id.txtProbabilita)
    TextView probabilita;
    @BindView(R.id.txtAttendibilita)
    TextView attendibilita;
    @BindView(R.id.locationView)
    AppCompatAutoCompleteTextView actv;
    //TextView cielo;
    Subscription actvSub;
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
        View v = inflater.inflate(R.layout.fragment_meteogramma, container, false);
//        v.setTag("test");

        ButterKnife.bind(this, v);

        java.util.Observable currentLocation = CurrentLocation.getInstance();
        currentLocation.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                String name = (String)arg;
                Timber.d("OBSERVER "+ name);
                loadData();
            }
        });

//        mNetworkImageView = (NetworkImageView)v.findViewById(R.id.networkImageView);
//        cielo=(TextView)v.findViewById(R.id.txtCielo);

//        List<Town> towns = loadTowns();

//        String[] names = Town.getNames(getContext());

        String[] names = TownList.getInstance(getContext()).getNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, names);
//        actv.setThreshold(2);
        actv.setAdapter(adapter);

//        actv.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Timber.d("TEXT ADDED "+ actv.getText());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                Timber.d("TEXT CHANGED");
//            }
//        });

        Subscription actvSub2 = RxTextView.textChanges(actv).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                Timber.d("textChanges "+ actv.getText());
            }
        });

//        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Timber.d("TEXT CHANGED AT "+adapterView.getItemAtPosition(i));
//            }
//        });

        Observable test = RxAutoCompleteTextView.itemClickEvents(actv);

        Subscription actvSub = RxAutoCompleteTextView.itemClickEvents(actv).subscribe(new Action1<AdapterViewItemClickEvent>() {
            @Override
            public void call(AdapterViewItemClickEvent adapterViewItemClickEvent) {
                String name = actv.getText().toString();

                Timber.d("itemClickEvents "+ name);

                CurrentLocation currentLocation = CurrentLocation.getInstance();
                currentLocation.setTown(TownList.getInstance(context).getTown(name));


            }
        });

//        actvSub = RxAutoCompleteTextView.itemClickEvents(actv).subscribe(e ->
//                {
//                    Timber.d("TEXT CHANGED AT " e.clickedView());
//                }
//        );

        loadData();

        return v;

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
                        if(currentLocation.isDefined()){
                            Town town = currentLocation.getTown();
                            int zoneIdx = town.getZone()-1;

                            mNetworkImageView.setImageUrl(response.getMeteogramma()[zoneIdx].getScadenza()[0].getSimbolo(), mImageLoader);
                            cielo.setText(response.getMeteogramma()[zoneIdx].getScadenza()[0].getCielo());
//                            temperatura.setText(response.getMeteogramma()[zoneIdx].getScadenza()[0].getT);
                            precipitazioni.setText(response.getMeteogramma()[zoneIdx].getScadenza()[0].getPrecipitazioni());
                            probabilita.setText(response.getMeteogramma()[zoneIdx].getScadenza()[0].getProbabilitaPrecipitazione());
                            attendibilita.setText(response.getMeteogramma()[zoneIdx].getScadenza()[0].getAttendibilita());
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
        actvSub.unsubscribe();
    }

}
