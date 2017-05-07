package eu.lucazanini.arpav.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GoogleLocator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long UPDATE_INTERVAL = 60 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 10 * 1000;
    private static final int UPDATE_NUMBER = 1;
    private static final int EXPIRATION_TIME = 30000;
    private static boolean locationPermissionGranted = false;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;
    protected LocationRequest locationRequest;
    protected Location upadatedLocation;
    private Context context;
    private CurrentLocation currentLocation;

    public GoogleLocator(Context context) {
        Timber.d("locator constructor");
        buildGoogleApiClient(context);
        this.context = context;
    }

    public void requestUpdates() {
        Timber.d("connection is " + googleApiClient.isConnected());
        if (googleApiClient.isConnected()) {
            Timber.d("connected");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                updateCurrentLocation(lastLocation);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void removeUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @DebugLog
    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        requestUpdates();
/*        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(lastLocation!=null){
        updateCurrentLocation(lastLocation);}*/
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @DebugLog
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        upadatedLocation = location;
        updateCurrentLocation(location);
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setNumUpdates(UPDATE_NUMBER);
        locationRequest.setExpirationDuration(EXPIRATION_TIME);
//        locationRequest.setInterval(UPDATE_INTERVAL);
//        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
    }

    @DebugLog
    public void connect() {
        googleApiClient.connect();
        Timber.d(" method connection is " + googleApiClient.isConnected());
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    @DebugLog
    private void updateCurrentLocation(Location location) {
        List<Town> towns = TownList.getInstance(context).getTowns();

        Collections.sort(towns, new Town.GpsDistanceComparator(location.getLatitude(), location.getLongitude()));

        currentLocation = CurrentLocation.getInstance();
        currentLocation.setTown(towns.get(0));
    }
}
