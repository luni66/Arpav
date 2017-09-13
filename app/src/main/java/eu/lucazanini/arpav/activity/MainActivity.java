package eu.lucazanini.arpav.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.fragment.EvolutionFragment;
import eu.lucazanini.arpav.fragment.MeteogrammaFragment;
import eu.lucazanini.arpav.helper.LocaleHelper;
import eu.lucazanini.arpav.helper.PreferenceHelper;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.model.SlideTitles;

import static eu.lucazanini.arpav.activity.SearchableActivity.REQUEST_CODE;

/**
 * The main activity containing the fragments with data populated from xml files downloaded from Arpav site
 * <p>
 * It extends {@link ActivityCallBack ActivityCallBack} interface to manage title labels with the tvDates
 * and {@link java.util.Observer Observer} interface to manage the app title containing the name of the town
 * <p>
 */
public class MainActivity extends AppCompatActivity implements ActivityCallBack, Observer {

    private static final int PAGES = 8;
    private static final int PAGES_LIMIT = 7;
    private final static int LOCATION_REQUEST = 1;
    private static final int EXPIRATION_TIME = 30000;
    private final static int UPDATE_LIMIT = 1;
    protected @BindView(R.id.pager) ViewPager pager;
    protected @BindView(R.id.mainToolbar) Toolbar mainToolbar;
    protected @BindString(R.string.action_title) String defaultTitle;
    private CollectionPagerAdapter collectionPagerAdapter;
    private ActionBar actionBar;
    private Town town = null;
    private PreferenceHelper preferences;
    private CurrentLocation currentLocation;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), this);

        pager.setOffscreenPageLimit(PAGES_LIMIT);
        pager.setAdapter(collectionPagerAdapter);

        setSupportActionBar(mainToolbar);
        actionBar = getSupportActionBar();

        preferences = new PreferenceHelper(this);

        currentLocation = CurrentLocation.getInstance(this);
        currentLocation.addObserver(this);

        // set the app title the first time
        if (currentLocation.isDefined()) {
            actionBar.setTitle(currentLocation.getTown().getName());
        } else {
            actionBar.setTitle(defaultTitle);
        }

        checkPermission();
        checkGps();
        startLocationUpdates();

//        if (preferences.getLanguage().equals(Previsione.Language.IT)) {
//            Context context = LocaleHelper.setLocale(this, "it");
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        currentLocation.deleteObserver(this);
        collectionPagerAdapter.stopObserving();
    }

    /**
     * This method run after the user selects a town in {@link SearchableActivity}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String town = data.getStringExtra(SearchableActivity.TOWN_NAME);
                currentLocation.setTown(town, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meteogramma_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Town town;
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = SearchableActivity.getIntent(this);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            case R.id.action_gps:
                if (isGpsAvailable() && locationPermissionGranted) {
//                    googleLocator.requestUpdates();
                    startLocationUpdates();
                }
                return true;
            case R.id.action_save_location:
                town = currentLocation.getTown();
                if (town != null) {
                    preferences.saveLocation(town);
                }
                return true;
            case R.id.action_home:
                town = preferences.getLocation();
                if (town != null) {
                    currentLocation.setTown(town);
                }
                return true;
            case R.id.action_settings:
                Intent settingsIntent = SettingsActivity.getIntent(this);
                startActivity(settingsIntent);
                return true;
            case R.id.action_credits:
                Intent creditsIntent = CreditsActivity.getIntent(this);
                startActivity(creditsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted, yay! Do the
// contacts-related task you need to do.
// permission denied, boo! Disable the
// functionality that depends on this permission.
                locationPermissionGranted = grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                //TODO return eliminabile?
//                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        createLocationRequest();

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // TODO servono? non c'è bisogno di Task?
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        createLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setNumUpdates(UPDATE_LIMIT);
        locationRequest.setExpirationDuration(EXPIRATION_TIME);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    updateCurrentLocation(locationResult.getLastLocation());
                }
                for (Location currentLocation : locationResult.getLocations()) {
                    updateCurrentLocation(currentLocation);
                }
            }
        };
    }

    private synchronized void updateCurrentLocation(Location location) {
        List<Town> towns = TownList.getInstance(this).getTowns();
        Collections.sort(towns, new Town.GpsDistanceComparator(location.getLatitude(), location.getLongitude()));
        currentLocation.setTown(towns.get(0));
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!locationPermissionGranted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
            }
        } else {
            locationPermissionGranted = true;
        }
    }

    private boolean checkGps() {
        return preferences.useGps() && isGpsAvailable() && locationPermissionGranted;
    }

    private boolean isGpsAvailable() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        } else {
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public SlideTitles getSlideTitles() {
        return collectionPagerAdapter.getSlideTitles();
    }

    @Override
    public String getTitle(int page) {
        return collectionPagerAdapter.getSlideTitles().getSlideTitle(page);
    }

    @Override
    public void setTitles(String[] titles) {
        collectionPagerAdapter.getSlideTitles().setTitles(titles);
    }

    @Override
    public void setTitle(String title, int page) {
        collectionPagerAdapter.getSlideTitles().setSlideTitle(title, page);
    }

    @Override
    public void keepFragments(int page) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < PAGES; i++) {
            if (i != page) {
                Fragment fragment = fragmentManager.findFragmentByTag(Integer.toString(i));
                if (fragment != null) {
                    fragmentTransaction.remove(fragment);
                }
            }
        }
        fragmentTransaction.commit();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof CurrentLocation) {
            String title;
            if (arg != null) {
                title = arg.toString();
            } else {
                title = defaultTitle;
            }
            actionBar.setTitle(title);
        }
    }

//    private void changeLocale(String languageCode) {
//        Resources res = this.getResources();
//        // Change locale settings in the app.
//        DisplayMetrics dm = res.getDisplayMetrics();
//        android.content.res.Configuration conf = res.getConfiguration();
//        conf.setLocale(new Locale(languageCode.toLowerCase())); // API 17+ only.
//        // Use conf.locale = new Locale(...) if targeting lower versions
//        res.updateConfiguration(conf, dm);
//    }

    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter implements Observer {

        private SlideTitles slideTitles;

        public CollectionPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            slideTitles = new SlideTitles(context, PAGES);
            slideTitles.addObserver(this);
        }

        public void stopObserving() {
            slideTitles.deleteObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment;
            Bundle args = new Bundle();

            switch (i) {
                case 0:
                    fragment = new EvolutionFragment();
                    args.putInt(MeteogrammaFragment.PAGE_NUMBER, i);
                    args.putInt(MeteogrammaFragment.PAGES, PAGES);
                    fragment.setArguments(args);
                    break;
                default:
                    fragment = new MeteogrammaFragment();
                    args.putInt(MeteogrammaFragment.PAGE_NUMBER, i);
                    args.putInt(MeteogrammaFragment.PAGES, PAGES);
                    fragment.setArguments(args);
            }


            return fragment;
        }

        @Override
        public int getCount() {
            return PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return slideTitles.getSlideTitle(position);
        }

        public SlideTitles getSlideTitles() {
            return slideTitles;
        }

        public void setSlideTitles(SlideTitles slideTitles) {
            this.slideTitles = slideTitles;
        }

    }
}
