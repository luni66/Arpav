package eu.lucazanini.arpav.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleLocator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long UPDATE_INTERVAL = 60 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 10 * 1000;
    private static final int UPDATE_NUMBER = 1;
    private static final int EXPIRATION_TIME = 30000;
    private static boolean locationPermissionGranted = false;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;
    protected LocationRequest locationRequest;
    protected Location currentLocation;
    private Context context;

    public GoogleLocator(Context context) {
        buildGoogleApiClient(context);
        this.context = context;
    }

    public Location requestUpdates() {
        if (googleApiClient.isConnected()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                locationPermissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (!locationPermissionGranted) {
                    return null;
//                    if (!locationPermissionRequested) {
//                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
//                        locationPermissionRequested = true;
//                        requestLocation();
//                    }
                }
            } else {
                locationPermissionGranted = true;
            }
            if (locationPermissionGranted) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
            }
        }
        return currentLocation;
    }

    public void removeUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        locationRequest.setNumUpdates(UPDATE_NUMBER);
//        locationRequest.setExpirationDuration(EXPIRATION_TIME);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }
}
