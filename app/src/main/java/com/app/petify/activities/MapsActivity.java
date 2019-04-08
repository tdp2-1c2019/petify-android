package com.app.petify.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.app.petify.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Address origin;
    private Address destination;
    private Polyline trip;

    private EditText mOriginAdress;
    private EditText mDestinationAdress;
    private Geocoder geocoder;

    private FusedLocationProviderClient fusedLocationClient;
    private int LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        geocoder = new Geocoder(this);

        mOriginAdress = findViewById(R.id.origin_address);
        mDestinationAdress = findViewById(R.id.destination_address);

        // TODO hacer estas cosas de manera asincronica
        mOriginAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cambia lo escrito por el usuario
                try {
                    origin = geocoder.getFromLocationName(s.toString(), 1).get(0);
                    mOriginAdress.setError(null);
                    loadTrip();
                } catch (Exception e) {
                    mOriginAdress.setError("Direccion invalida");
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mDestinationAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cambia lo escrito por el usuario
                try {
                    destination = geocoder.getFromLocationName(s.toString(), 1).get(0);
                    mDestinationAdress.setError(null);
                    loadTrip();
                } catch (Exception e) {
                    mDestinationAdress.setError("Direccion invalida");
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadTrip() {
        if (origin != null && destination != null) {
            final LatLng originLatLng = new LatLng(origin.getLatitude(), origin.getLongitude());
            final LatLng destinationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

            // Ponemos los markers sacando lo anterior
            mMap.clear();
            if (trip != null) trip.remove();
            mMap.addMarker(new MarkerOptions().position(originLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(destinationLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Dibujamos el camino
            GoogleDirection.withServerKey("AIzaSyB3DfG7c86Bt8RNSyiUIoctokes9zB-4Yc")
                    .from(originLatLng).to(destinationLatLng)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.BLUE);
                                // Dibujamos el recorrido
                                trip = mMap.addPolyline(polylineOptions);
                                // Zoomeamos el recorrido
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(originLatLng);
                                builder.include(destinationLatLng);
                                int padding = 300;
                                LatLngBounds bounds = builder.build();
                                final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mMap.animateCamera(cu);
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                        }
                    });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Check for Location permissions
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // Obtenemos la ubicacion del usuario y lo zoomeamos ahi inicialmente
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(current)
                            .zoom(17)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });


    }
}