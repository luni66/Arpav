package eu.lucazanini.arpav.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.fragment.MeteogrammaFragment;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.location.GoogleLocator;
import eu.lucazanini.arpav.location.Town;
import eu.lucazanini.arpav.location.TownList;
import eu.lucazanini.arpav.model.SlideTitles;
import timber.log.Timber;

import static eu.lucazanini.arpav.fragment.MeteogrammaFragment.REQUEST_CODE;

// http://stackoverflow.com/questions/23133912/android-viewpager-update-off-screen-but-cached-fragments-in-viewpager

// https://guides.codepath.com/android/ViewPager-with-FragmentPagerAdapter

//public class MainActivity extends AppCompatActivity implements TitlesCallBack, Observer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
public class MainActivity extends AppCompatActivity implements TitlesCallBack, Observer {

    private static final int PAGES = 7;
    private static final int PAGES_LIMIT = 7;
    private final static int LOCATION_REQUEST = 1;
    private static boolean locationPermissionGranted = false;
    protected @BindView(R.id.pager) ViewPager pager;
    protected @BindView(R.id.my_toolbar) Toolbar myToolbar;
    protected @BindString(R.string.action_title) String defaultTitle;
    private CollectionPagerAdapter collectionPagerAdapter;
    private CurrentLocation currentLocation;
    private ActionBar actionBar;
    private GoogleLocator googleLocator;
//    private Town town=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

        pager.setOffscreenPageLimit(PAGES_LIMIT);
        pager.setAdapter(collectionPagerAdapter);

//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();
//        if(actionBar!=null) {
//            actionBar.setDisplayShowHomeEnabled(true);
//        }

        currentLocation = CurrentLocation.getInstance();

//       town = currentLocation.getTown();

//        if (town == null) {
//            String townName = getPrefsLocation();
//            town = TownList.getInstance(context).getTown(townName);
//            if (town != null) {
//                upadatedLocation.setTown(town);
//            }
//        }

        if (currentLocation.isDefined()) {
            actionBar.setTitle(currentLocation.getTown().getName());
        } else {
            actionBar.setTitle(defaultTitle);
        }

        currentLocation.addObserver(this);

//        checkPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String town = data.getStringExtra(SearchableActivity.TOWN_NAME);
                currentLocation.setTown(town, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.meteogramma_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Town town = null;
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.action_search:
                Intent intent = SearchableActivity.getIntent(this);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            case R.id.action_find_location:
                if ( isGpsAvailable() && locationPermissionGranted) {
                    googleLocator.requestUpdates();
                }

                return true;
            case R.id.action_save_location:
town = currentLocation.getTown();
                if (town != null) {
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.current_location), town.getName());
                    editor.commit();
                }
                return true;
            case R.id.action_home:
//                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
//                String townName = sharedPref.getString(getString(R.string.current_location), "");
//                Town town = TownList.getInstance(this).getTown(townName);
                town = currentLocation.getTown();
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
        googleLocator = new GoogleLocator(this);
        googleLocator.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleLocator.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
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
        currentLocation.deleteObserver(this);
        collectionPagerAdapter.stopObserving();
    }

    @Override
    public SlideTitles getTitles() {
        return SLIDE_TITLES;
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

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
            SLIDE_TITLES.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChanged();
        }

        public void stopObserving() {
            SLIDE_TITLES.deleteObserver(this);
        }

        @Override
        public Fragment getItem(int i) {

            MeteogrammaFragment fragment = new MeteogrammaFragment();
            Bundle args = new Bundle();
            args.putInt(MeteogrammaFragment.NUMBER_PAGE, i);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return SLIDE_TITLES.getSlideTitle(position);
        }
    }
}
