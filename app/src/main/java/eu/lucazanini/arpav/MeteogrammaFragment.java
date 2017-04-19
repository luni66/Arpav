package eu.lucazanini.arpav;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.List;
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
import eu.lucazanini.arpav.model.Titles;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class MeteogrammaFragment extends Fragment implements Observer {

    public static final String NUMBER_PAGE = "number_page";
    protected @BindView(R.id.text_location) AppCompatAutoCompleteTextView actvLocation;
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
    protected @BindView(R.id.save_location) Button btnSaveLocation;
    private Context context;
    private Unbinder unbinder;
    private String daySkyUrl, daySky, temperature1, temperature2, rain, probability, snow, wind, reliability;
    private int pageNumber;
    private Subscription actvSub, actvSub2;
    private CurrentLocation currentLocation;
    private TitlesCallBack titlesCallBack;
    public final static int REQUEST_CODE = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Bundle args = getArguments();
        pageNumber = args.getInt(NUMBER_PAGE);

        setHasOptionsMenu(true);

        currentLocation = CurrentLocation.getInstance();
        currentLocation.addObserver(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_meteogramma, container, false);

        unbinder = ButterKnife.bind(this, v);

        Town town = currentLocation.getTown();

        if (town == null) {
            String townName = getPrefsLocation();
            town = TownList.getInstance(context).getTown(townName);
            if (town != null) {
                currentLocation.setTown(town);
                actvLocation.setText(town.getName());
            }
        } else {
            actvLocation.setText(town.getName());
        }

        String[] names = TownList.getInstance(getContext()).getNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, names);
        actvLocation.setAdapter(adapter);

        actvSub2 = RxTextView.textChanges(actvLocation).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
            }
        });

        actvSub = RxAutoCompleteTextView.itemClickEvents(actvLocation).subscribe(adapterViewItemClickEvent -> {
            String name = actvLocation.getText().toString();

            currentLocation.setTown(TownList.getInstance(context).getTown(name));

            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

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

        BulletinRequest bulletinRequest = new BulletinRequest(Previsione.getUrl(Previsione.Language.IT),
                new Response.Listener<Previsione>() {

                    final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
                        @Override
                        public void apply(View view, int index) {
                            view.setVisibility(View.GONE);
                        }
                    };

                    final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
                        @Override
                        public void apply(View view, int index) {
                            view.setVisibility(View.VISIBLE);
                        }
                    };

                    private void setViewVisibility(TextView text, View image) {
                        String caption = (String) text.getText();
                        if (caption == null || caption.equals("")) {
                            ButterKnife.apply(text, GONE);
                            ButterKnife.apply(image, GONE);
                        } else {
                            ButterKnife.apply(text, VISIBLE);
                            ButterKnife.apply(image, VISIBLE);
                        }
                    }

                    private void setViewVisibility(TextView text) {
                        String caption = (String) text.getText();
                        if (caption == null || caption.equals("")) {
                            ButterKnife.apply(text, GONE);
                        } else {
                            ButterKnife.apply(text, VISIBLE);
                        }
                    }

                    @Override
                    public void onResponse(Previsione response) {

                        if (currentLocation.isDefined()) {
                            Town town = currentLocation.getTown();
                            int zoneIdx = town.getZone() - 1;

                            Meteogramma[] meteogrammi = null;
                            Meteogramma.Scadenza[] scadenze = null;
                            Bollettino bollettino = null;
                            Bollettino.Giorno[] giorni = null;

                            meteogrammi = response.getMeteogramma();
                            Meteogramma meteogramma = meteogrammi[zoneIdx];
                            scadenze = meteogramma.getScadenza();

                            //TODO add bollettino
                            if (response.getLanguage() == Previsione.Language.IT) {
                                bollettino = response.getMeteoVeneto();
                                giorni = bollettino.getGiorni();
                            }

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

                            for (int i = 0; i < Titles.PAGES; i++) {
                                Titles titles = titlesCallBack.getTitles();
                                if (!titles.getTitle(i).equals(scadenze[i].getData())) {
                                    titles.setTitle(scadenze[i].getData(), i);
                                }
                            }

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

                            setViewVisibility(tvTemperature1, imgTemperature1);
                            setViewVisibility(tvTemperature2, imgTemperature2);
                            setViewVisibility(tvRain, imgRain);
                            setViewVisibility(tvSnow, imgSnow);
                            setViewVisibility(tvWind, imgWind);
                            setViewVisibility(tvReliability);

                            daySkyUrl = scadenze[pageNumber].getSimbolo();
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

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
            }
        }, Integer.toString(pageNumber));

        volleyApp.addToRequestQueue(bulletinRequest);
    }

    //    http://stackoverflow.com/questions/34291453/adding-searchview-in-fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.meteogramma_menu, menu);

//        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.search));

/*        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  Timber.d("onQueryTextSubmit");
                                                  return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  Timber.d("onQueryTextChange");
                                                  return false;
                                              }
                                          }

        );*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

                Intent intent = SearchableActivity.getIntent(context);
                getActivity().startActivityForResult(intent, REQUEST_CODE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

        @Override
        public void onDestroy () {
            super.onDestroy();
            currentLocation.deleteObserver(this);
            Timber.d("fragment %s doesn't observe currentLocation", pageNumber);

            String tag = Integer.toString(pageNumber);
            final VolleySingleton volleyApp = VolleySingleton.getInstance(getContext());
            volleyApp.getRequestQueue().cancelAll(tag);
//        actvSub.unsubscribe();
        }

        @Override
        public void onDestroyView () {
            super.onDestroyView();
            unbinder.unbind();
            actvSub.unsubscribe();
            actvSub2.unsubscribe();
        }

        @Override
        public void update (java.util.Observable o, Object arg){
            if (actvLocation != null)
                try {
                    actvLocation.setText((String) arg);
                } catch (NullPointerException e) {
                    Timber.e(e.toString());
                }
            loadData();
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }
}
