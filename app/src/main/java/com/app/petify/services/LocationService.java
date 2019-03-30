package com.app.petify.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {
    private DatabaseReference db;
    public LocalBroadcastManager lbm;
    public LocationManager locationManager;
    public LL listener;
    public Location previousBestLocation = null;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseDatabase.getInstance().getReference();
        lbm = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new LL();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        return START_STICKY;
    }

    public class LL implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();
            Intent i = new Intent("latlon");
            i.putExtra("latlon", lat+","+lon);
            lbm.sendBroadcast(i);
            db.child("ubicacion").child("lat").setValue(lat);
            db.child("ubicacion").child("lon").setValue(lon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
