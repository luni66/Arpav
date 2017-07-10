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

/**
 * Handles google api to get the location
 *
 */
public class GoogleLocator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int UPDATE_NUMBER = 1;
    private static final int EXPIRATION_TIME = 30000;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;
    protected LocationRequest locationRequest;
    protected Location upadatedLocation;
    private Context context;
    private CurrentLocation currentLocation;

    public GoogleLocator(Context context) {
        buildGoogleApiClient(context);
        this.context = context;
    }

    public void requestUpdates() {
        if (googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

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
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    private void updateCurrentLocation(Location location) {
        List<Town> towns = TownList.getInstance(context).getTowns();

        Collections.sort(towns, new Town.GpsDistanceComparator(location.getLatitude(), location.getLongitude()));

        currentLocation = CurrentLocation.getInstance(context);
        currentLocation.setTown(towns.get(0));
    }
}
