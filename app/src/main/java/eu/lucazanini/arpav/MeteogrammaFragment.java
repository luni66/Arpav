package eu.lucazanini.arpav;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.task.ReportTask;
import timber.log.Timber;

import static eu.lucazanini.arpav.model.Meteogramma.SCADENZA_IDX;
import static eu.lucazanini.arpav.model.Previsione.MG_IDX;

/**
 * Created by luke on 23/01/17.
 */

public class MeteogrammaFragment extends Fragment {

    NetworkImageView mNetworkImageView;
    @BindView(R.id.txtCielo) TextView cielo;
    @BindView(R.id.autoCompleteTextView1)
    AutoCompleteTextView actv;
//TextView cielo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_meteogramma, container, false);
//        v.setTag("test");

        ButterKnife.bind(this, v);

        mNetworkImageView = (NetworkImageView)v.findViewById(R.id.networkImageView);
//        cielo=(TextView)v.findViewById(R.id.txtCielo);

//        List<Town> towns = loadTowns();

        String[] names = Town.getNames(getActivity());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, names);
        actv.setAdapter(adapter);

        loadData();

        return v;

    }

    private List<Town> loadTowns(){
        List<Town> towns;
        towns = Town.loadTowns(getActivity());
        return towns;
    }

    private void loadData(){
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

                        for(int i = 0; i<MG_IDX; i++){
                            for(int j=0; j<SCADENZA_IDX; j++){
                                String imgUrl = response.getMeteogramma()[i].getScadenza()[j].getSimbolo();
                                mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                        Timber.d("Image URL: " + response.getRequestUrl());
                                        Timber.d("Image Load completed: "+ response.getRequestUrl());
                                    }

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Timber.e("Image Load Error: " + error.getMessage());
                                    }
                                });
                            }
                        }

                        mNetworkImageView.setImageUrl(response.getMeteogramma()[0].getScadenza()[0].getSimbolo(), mImageLoader);
cielo.setText(response.getMeteogramma()[0].getScadenza()[0].getCielo());
                        PagerTitleStrip pagerTitleStrip = (PagerTitleStrip)getActivity().findViewById(R.id.pager_title_strip);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
            }
        });

        volleyApp.addToRequestQueue(bulletinRequest);
    }

}
