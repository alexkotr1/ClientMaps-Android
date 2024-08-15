package com.alexk.clientmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alexk.clientmap.LocationUtilitiesListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationUtilities {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int GPS_REQUEST_CODE = 2000;
    private final Context context;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private com.alexk.clientmap.LocationUtilitiesListener listener;

    public LocationUtilities(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void requestLocation() {
        if (checkLocationPermission()) {
            if (isGpsEnabled()) {
                getCurrentLocation();
            } else {
                promptToEnableGps();
            }
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void promptToEnableGps() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, GPS_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GPS_REQUEST_CODE) {
            if (isGpsEnabled()) {
                getCurrentLocation();
            } else promptToEnableGps();
        }
    }

    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000); // 5 seconds
            locationRequest.setFastestInterval(2000); // 2 seconds

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        long locationTime = location.getTime();
                        long elapsedTimeNanos = location.getElapsedRealtimeNanos();
                        long elapsedTimeMillis = (SystemClock.elapsedRealtimeNanos() - elapsedTimeNanos) / 1000000;
                        if (elapsedTimeMillis < 10000) {
                            onLocationReceived(location, locationTime);
                        }
                    }
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            };

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListener(LocationUtilitiesListener listener) {
        this.listener = listener;
    }

    private void onLocationReceived(Location location, long locationTime) {
        Date date = new Date(locationTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = dateFormat.format(date);
        Log.d("Location", "Location: " + location.toString() + ", Time: " + formattedTime);
        listener.onLocationReceived(location);
    }
}
