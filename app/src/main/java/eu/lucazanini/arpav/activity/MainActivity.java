package eu.lucazanini.arpav.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.fragment.MeteogrammaFragment;
import eu.lucazanini.arpav.fragment.PreviewFragment;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.GoogleLocator;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.SlideTitles;
import eu.lucazanini.arpav.preference.Preferences;
import eu.lucazanini.arpav.preference.UserPreferences;
import timber.log.Timber;

import static eu.lucazanini.arpav.fragment.MeteogrammaFragment.REQUEST_CODE;

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
    private boolean locationPermissionGranted = false;
    protected @BindView(R.id.pager) ViewPager pager;
    protected @BindView(R.id.mainToolbar) Toolbar mainToolbar;
    protected @BindString(R.string.action_title) String defaultTitle;
    private CollectionPagerAdapter collectionPagerAdapter;
    private CurrentLocation currentLocation;
    private ActionBar actionBar;
    private GoogleLocator googleLocator;
    private Town town = null;
    private Preferences preferences;
    private FusedLocationProviderClient mFusedLocationClient;
    private final static int REQUEST_CHECK_SETTINGS = 1;

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

        preferences = new UserPreferences(this);
        checkPermission();

        Timber.d("isGpsAvailable() %s", isGpsAvailable());
        Timber.d("locationPermissionGranted %s", locationPermissionGranted);
        Timber.d("preferences.useGpsuseGps() %s", preferences.useGps());

        if (isGpsAvailable() && locationPermissionGranted && preferences.useGps()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Timber.d("TEST for new location");
                                updateCurrentLocation(location);

                            }
                        }
                    });

            createLocationRequest();

            googleLocator = new GoogleLocator(this);
            googleLocator.connect();
        }

        Timber.d("adding observer");
        currentLocation = CurrentLocation.getInstance(this);
        currentLocation.addObserver(this);

        // set the app title the first time
        if (currentLocation.isDefined()) {
            actionBar.setTitle(currentLocation.getTown().getName());
        } else {
            actionBar.setTitle(defaultTitle);
        }
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

// ...

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
//                Timber.d("TEST found new location")
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private synchronized void updateCurrentLocation(Location location) {
        List<Town> towns = TownList.getInstance(this).getTowns();

        Collections.sort(towns, new Town.GpsDistanceComparator(location.getLatitude(), location.getLongitude()));

        currentLocation = CurrentLocation.getInstance(this);
        currentLocation.setTown(towns.get(0));
    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                Timber.d("Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE):
                Timber.d("Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP):
                Timber.d("Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Timber.d("Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Timber.d("Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
*/

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
        Town town = null;
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = SearchableActivity.getIntent(this);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            case R.id.action_find_location:
                if (isGpsAvailable() && locationPermissionGranted) {
                    googleLocator.requestUpdates();
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
    protected void onStart() {
        super.onStart();
//        googleLocator = new GoogleLocator(this);
//        googleLocator.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        googleLocator.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
        startLocationUpdates();
//        if (isGpsAvailable() && locationPermissionGranted && preferences.useGps()) {
//            Timber.d("calling GoogleLocator");
//            googleLocator.requestUpdates();
//        }
    }

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    locationPermissionGranted = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    locationPermissionGranted = false;
                }
                //TODO return eliminabile?
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleLocator != null) {
            googleLocator.disconnect();
        }
        currentLocation.deleteObserver(this);
        collectionPagerAdapter.stopObserving();
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

    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter implements Observer {

        private SlideTitles slideTitles;

        public CollectionPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            slideTitles = new SlideTitles(context, PAGES);
            slideTitles.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChanged();
        }

        public void stopObserving() {
            slideTitles.deleteObserver(this);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment fragment;
            Bundle args = new Bundle();

            switch (i) {
                case 0:
                    fragment = new PreviewFragment();
//                    Bundle args = new Bundle();
                    args.putInt(MeteogrammaFragment.PAGE_NUMBER, i);
                    args.putInt(MeteogrammaFragment.PAGES, PAGES);
                    fragment.setArguments(args);
                    break;
                default:
//                  MeteogrammaFragment fragment = new MeteogrammaFragment();
                    fragment = new MeteogrammaFragment();
//                    Bundle args = new Bundle();
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
