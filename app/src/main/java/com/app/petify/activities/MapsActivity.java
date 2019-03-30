package com.app.petify.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.app.petify.services.LocationService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    Marker marker;
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double lat = Double.parseDouble(intent.getStringExtra("latlon").split(",")[0]);
            Double lon = Double.parseDouble(intent.getStringExtra("latlon").split(",")[1]);
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startService(new Intent(this, LocationService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, new IntentFilter("latlon"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocationName("Estacion de ituzaingo", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LatLng to;
        if (addresses.size() > 0) {
            to = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            LatLng mid = new LatLng((current.latitude + to.latitude) * 0.5, (current.longitude + to.longitude) * 0.5);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mid));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mid, 15);
            mMap.animateCamera(cameraUpdate);
            mMap.addMarker(new MarkerOptions().position(to));
            GoogleDirection.withServerKey("AIzaSyB3DfG7c86Bt8RNSyiUIoctokes9zB-4Yc")
                    .from(current)
                    .to(to)
                    .avoid(AvoidType.FERRIES)
                    .avoid(AvoidType.HIGHWAYS)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.BLACK);
                                mMap.addPolyline(polylineOptions);
                            } else {
                                // Do something
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            // Do something
                        }
                    });
        }

        marker = mMap.addMarker(new MarkerOptions().position(current).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
    }
}
